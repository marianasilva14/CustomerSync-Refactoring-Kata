package codingdojo;

public class CustomerRepoImplentation implements CustomerRepo {

    private final CustomerDataAccess customerDataAccess;

    public CustomerRepoImplentation(CustomerDataAccess customerDataAccess) {
        this.customerDataAccess = customerDataAccess;
    }

    @Override
    public CustomerMatches loadCompanyCustomer(String externalId, String companyNumber) {
        return customerDataAccess.loadCompanyCustomer(externalId, companyNumber);
    }

    @Override
    public CustomerMatches loadPersonCustomer(String externalId) {
        return customerDataAccess.loadPersonCustomer(externalId);
    }

    @Override
    public Customer updateCustomerRecord(Customer customer) {
        return customerDataAccess.updateCustomerRecord(customer);
    }

    @Override
    public Customer createCustomerRecord(Customer customer) {
        return customerDataAccess.createCustomerRecord(customer);
    }

    @Override
    public void updateShoppingList(Customer customer, ShoppingList consumerShoppingList) {
        customerDataAccess.updateShoppingList(customer, consumerShoppingList);
    }
}
