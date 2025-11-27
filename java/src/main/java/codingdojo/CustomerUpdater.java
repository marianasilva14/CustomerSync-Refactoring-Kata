package codingdojo;

import java.util.List;
import java.util.Objects;

public class CustomerUpdater {

    private final CustomerRepo repository;

    public CustomerUpdater(CustomerRepo repository) {
        this.repository = repository;
    }

    public Customer prepareCustomerForSync(CustomerMatches customerMatches, NormalizedCustomer normalizedCustomer) {
        Customer customer = customerMatches.getCustomer();

        if (customer == null) {
            customer = new Customer();
            customer.setExternalId(normalizedCustomer.getExternalId());
            customer.setMasterExternalId(normalizedCustomer.getExternalId());
        }

        populateFields(normalizedCustomer, customer);

        return customer;
    }

    public void updatePreferredStore(NormalizedCustomer normalizedCustomer, Customer customer) {
        customer.setPreferredStore(normalizedCustomer.getPreferredStore());
    }

    public void populateFields(NormalizedCustomer normalizedCustomer, Customer customer) {
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

    public void updateContactInfo(NormalizedCustomer normalizedCustomer, Customer customer) {
        customer.setAddress(normalizedCustomer.getPostalAddress());
    }

    public void updateRelations(NormalizedCustomer normalizedCustomer, Customer customer) {
        List<ShoppingList> consumerShoppingLists = normalizedCustomer.getShoppingLists();
        for (ShoppingList consumerShoppingList : consumerShoppingLists) {
            repository.updateShoppingList(customer, consumerShoppingList);
        }
    }

    public void updateDuplicate(NormalizedCustomer normalizedCustomer, Customer duplicate) {
        if (duplicate == null) {
            duplicate = new Customer();
            duplicate.setExternalId(normalizedCustomer.getExternalId());
            duplicate.setMasterExternalId(normalizedCustomer.getExternalId());
        }

        duplicate.setName(normalizedCustomer.getName());

        if (duplicate.getInternalId() == null) {
            repository.createCustomerRecord(duplicate);
        } else {
            repository.updateCustomerRecord(duplicate);
        }
    }

    public void updateDuplicates(NormalizedCustomer normalizedCustomer, CustomerMatches customerMatches) {
        if (customerMatches.hasDuplicates()) {
            for (Customer duplicate : customerMatches.getDuplicates()) {
                updateDuplicate(normalizedCustomer, duplicate);
            }
        }
    }

}
