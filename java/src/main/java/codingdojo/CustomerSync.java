package codingdojo;

import java.util.List;
import java.util.Objects;

public class CustomerSync {

    private final CustomerDataAccess customerDataAccess;
    private final CustomerMatchStrategy personMatchStrategy;
    private final CustomerMatchStrategy companyMatchStrategy;

    public CustomerSync(CustomerDataLayer customerDataLayer) {
        this(new CustomerDataAccess(customerDataLayer));
    }

    public CustomerSync(CustomerDataAccess db) {
        this.customerDataAccess = db;
        this.personMatchStrategy = new PersonMatchStrategy();
        this.companyMatchStrategy = new CompanyMatchStrategy();
    }

    public boolean syncWithDataLayer(ExternalCustomer externalCustomer) {

        NormalizedCustomer normalizedCustomer = new NormalizedCustomer(externalCustomer);

        CustomerMatchStrategy customerMatchStrategy;
        if (normalizedCustomer.isCompany()) {
             customerMatchStrategy = this.companyMatchStrategy;
        } else {
            customerMatchStrategy = this.personMatchStrategy;
        }

        CustomerMatches customerMatches = customerMatchStrategy.load(normalizedCustomer, this.customerDataAccess);
        Customer customer = customerMatches.getCustomer();

        if (customer == null) {
            customer = new Customer();
            customer.setExternalId(normalizedCustomer.getExternalId());
            customer.setMasterExternalId(normalizedCustomer.getExternalId());
        }

        populateFields(normalizedCustomer, customer);

        boolean created = false;
        if (customer.getInternalId() == null) {
            customer = createCustomer(customer);
            created = true;
        } else {
            updateCustomer(customer);
        }
        updateContactInfo(normalizedCustomer, customer);

        if (customerMatches.hasDuplicates()) {
            for (Customer duplicate : customerMatches.getDuplicates()) {
                updateDuplicate(normalizedCustomer, duplicate);
            }
        }

        updateRelations(normalizedCustomer, customer);
        updatePreferredStore(normalizedCustomer, customer);

        return created;
    }

    private void updateRelations(NormalizedCustomer normalizedCustomer, Customer customer) {
        List<ShoppingList> consumerShoppingLists = normalizedCustomer.getShoppingLists();
        for (ShoppingList consumerShoppingList : consumerShoppingLists) {
            this.customerDataAccess.updateShoppingList(customer, consumerShoppingList);
        }
    }

    private Customer updateCustomer(Customer customer) {
        return this.customerDataAccess.updateCustomerRecord(customer);
    }

    private void updateDuplicate(NormalizedCustomer normalizedCustomer, Customer duplicate) {
        if (duplicate == null) {
            duplicate = new Customer();
            duplicate.setExternalId(normalizedCustomer.getExternalId());
            duplicate.setMasterExternalId(normalizedCustomer.getExternalId());
        }

        duplicate.setName(normalizedCustomer.getName());

        if (duplicate.getInternalId() == null) {
            createCustomer(duplicate);
        } else {
            updateCustomer(duplicate);
        }
    }

    private void updatePreferredStore(NormalizedCustomer normalizedCustomer, Customer customer) {
        customer.setPreferredStore(normalizedCustomer.getPreferredStore());
    }

    private Customer createCustomer(Customer customer) {
        return this.customerDataAccess.createCustomerRecord(customer);
    }

    private void populateFields(NormalizedCustomer normalizedCustomer, Customer customer) {
        customer.setName(normalizedCustomer.getName());
        if (normalizedCustomer.isCompany()) {
            customer.setCompanyNumber(normalizedCustomer.getCompanyNumber());
            customer.setCustomerType(CustomerType.COMPANY);
        } else {
            customer.setCustomerType(CustomerType.PERSON);
            Integer externalPoints = normalizedCustomer.getBonusPointsBalance();
            if (!Objects.equals(externalPoints, customer.getBonusPointsBalance())) {
                customer.setBonusPointsBalance(externalPoints);
            }
        }
    }

    private void updateContactInfo(NormalizedCustomer normalizedCustomer, Customer customer) {
        customer.setAddress(normalizedCustomer.getPostalAddress());
    }

}
