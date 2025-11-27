package codingdojo;

public interface CustomerRepo {
    CustomerMatches loadCompanyCustomer(String externalId, String companyNumber);
    CustomerMatches loadPersonCustomer(String externalId);
    Customer updateCustomerRecord(Customer customer);
    Customer createCustomerRecord(Customer customer);
    void updateShoppingList(Customer customer, ShoppingList consumerShoppingList);
}
