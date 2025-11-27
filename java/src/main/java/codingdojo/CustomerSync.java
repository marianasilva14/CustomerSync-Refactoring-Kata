package codingdojo;

public class CustomerSync {

    private final CustomerRepo repository;
    private final CustomerMatchStrategy personMatchStrategy;
    private final CustomerMatchStrategy companyMatchStrategy;
    private final CustomerUpdater customerUpdater;

    public CustomerSync(CustomerDataLayer customerDataLayer) {
        this(new CustomerDataAccess(customerDataLayer));
    }

    public CustomerSync(CustomerDataAccess db) {
        this.repository = new CustomerRepoImplentation(db);
        this.personMatchStrategy = new PersonMatchStrategy();
        this.companyMatchStrategy = new CompanyMatchStrategy();
        this.customerUpdater = new CustomerUpdater(repository);
    }

    public boolean syncWithDataLayer(ExternalCustomer externalCustomer) {

        NormalizedCustomer normalizedCustomer = new NormalizedCustomer(externalCustomer);

        CustomerMatchStrategy customerMatchStrategy;
        if (normalizedCustomer.isCompany()) {
             customerMatchStrategy = this.companyMatchStrategy;
        } else {
            customerMatchStrategy = this.personMatchStrategy;
        }

        CustomerMatches customerMatches = customerMatchStrategy.load(normalizedCustomer, this.repository);
        Customer customer = customerUpdater.prepareCustomerForSync(customerMatches, normalizedCustomer);

        boolean created = false;
        if (customer.getInternalId() == null) {
            customer = createCustomer(customer);
            created = true;
        } else {
            updateCustomer(customer);
        }
        customerUpdater.updateContactInfo(normalizedCustomer, customer);
        customerUpdater.updateDuplicates(normalizedCustomer, customerMatches);

        customerUpdater.updateRelations(normalizedCustomer, customer);
        customerUpdater.updatePreferredStore(normalizedCustomer, customer);

        return created;
    }

    private Customer updateCustomer(Customer customer) {
        return this.repository.updateCustomerRecord(customer);
    }

    private Customer createCustomer(Customer customer) {
        return this.repository.createCustomerRecord(customer);
    }
    
}
