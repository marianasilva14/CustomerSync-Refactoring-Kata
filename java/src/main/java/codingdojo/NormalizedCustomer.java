package codingdojo;

import java.util.List;

public class NormalizedCustomer {
    private final ExternalCustomer externalCustomer;

    public NormalizedCustomer(ExternalCustomer externalCustomer){
        this.externalCustomer = externalCustomer;
    }

    public boolean isCompany() {
        return externalCustomer.isCompany();
    }

    public boolean isPerson() {
        return !externalCustomer.isCompany();
    }

    public String getExternalId() {
        return externalCustomer.getExternalId();
    }

    public String getCompanyNumber() {
        return externalCustomer.getCompanyNumber();
    }

    public Address getPostalAddress() {
        return externalCustomer.getPostalAddress();
    }

    public String getName() {
        return externalCustomer.getName();
    }

    public String getPreferredStore() {
        return externalCustomer.getPreferredStore();
    }

    public List<ShoppingList> getShoppingLists() {
        return externalCustomer.getShoppingLists();
    }

    public Integer getBonusPointsBalance() {
        return externalCustomer.getBonusPointsBalance();
    }

}
