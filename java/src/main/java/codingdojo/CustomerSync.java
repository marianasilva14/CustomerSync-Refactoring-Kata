package codingdojo;

import java.util.List;
import java.util.Objects;

public class CustomerSync {

    private final CustomerDataAccess customerDataAccess;

    public CustomerSync(CustomerDataLayer customerDataLayer) {
        this(new CustomerDataAccess(customerDataLayer));
    }

    public CustomerSync(CustomerDataAccess db) {
        this.customerDataAccess = db;
    }

    public boolean syncWithDataLayer(ExternalCustomer externalCustomer) {

        NormalizedCustomer normalizedCustomer = new NormalizedCustomer(externalCustomer);
        CustomerMatches customerMatches;
        if (normalizedCustomer.isCompany()) {
            customerMatches = loadCompany(normalizedCustomer);
        } else {
            customerMatches = loadPerson(normalizedCustomer);
        }
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

    public CustomerMatches loadCompany(NormalizedCustomer normalizedCustomer) {

        final String externalId = normalizedCustomer.getExternalId();
        final String companyNumber = normalizedCustomer.getCompanyNumber();

        CustomerMatches customerMatches = customerDataAccess.loadCompanyCustomer(externalId, companyNumber);

        if (customerMatches.getCustomer() != null && !CustomerType.COMPANY.equals(customerMatches.getCustomer().getCustomerType())) {
            throw new ConflictException("Existing customer for externalCustomer " + externalId + " already exists and is not a company");
        }

        if ("ExternalId".equals(customerMatches.getMatchTerm())) {
            String customerCompanyNumber = customerMatches.getCustomer().getCompanyNumber();
            if (!companyNumber.equals(customerCompanyNumber)) {
                customerMatches.getCustomer().setMasterExternalId(null);
                customerMatches.addDuplicate(customerMatches.getCustomer());
                customerMatches.setCustomer(null);
                customerMatches.setMatchTerm(null);
            }
        } else if ("CompanyNumber".equals(customerMatches.getMatchTerm())) {
            String customerExternalId = customerMatches.getCustomer().getExternalId();
            if (customerExternalId != null && !externalId.equals(customerExternalId)) {
                throw new ConflictException("Existing customer for externalCustomer " + companyNumber + " doesn't match external id " + externalId + " instead found " + customerExternalId );
            }
            Customer customer = customerMatches.getCustomer();
            customer.setExternalId(externalId);
            customer.setMasterExternalId(externalId);
            customerMatches.addDuplicate(null);
        }

        return customerMatches;
    }

    public CustomerMatches loadPerson(NormalizedCustomer normalizedCustomer) {
        final String externalId = normalizedCustomer.getExternalId();

        CustomerMatches customerMatches = customerDataAccess.loadPersonCustomer(externalId);

        if (customerMatches.getCustomer() != null) {
            if (!CustomerType.PERSON.equals(customerMatches.getCustomer().getCustomerType())) {
                throw new ConflictException("Existing customer for externalCustomer " + externalId + " already exists and is not a person");
            }

            if (!"ExternalId".equals(customerMatches.getMatchTerm())) {
                Customer customer = customerMatches.getCustomer();
                customer.setExternalId(externalId);
                customer.setMasterExternalId(externalId);
            }
        }

        return customerMatches;
    }
}
