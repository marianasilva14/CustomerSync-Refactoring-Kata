package codingdojo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomerRepositoryTestMockito {

    AutoCloseable mocks;

    @Mock
    CustomerRepo repository;

    CustomerSync customerSync;

    @Test
    public void syncCompanyByExternalId(){
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId(externalId);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setExternalId(externalId);

        CustomerDataLayer db = mock(CustomerDataLayer.class);
        when(db.findByExternalId(externalId)).thenReturn(customer);
        CustomerSync sut = new CustomerSync(db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        // ASSERT
        assertFalse(created);
        ArgumentCaptor<Customer> argument = ArgumentCaptor.forClass(Customer.class);
        verify(db, atLeastOnce()).updateCustomerRecord(argument.capture());
        Customer updatedCustomer = argument.getValue();
        assertEquals(externalCustomer.getName(), updatedCustomer.getName());
        assertEquals(externalCustomer.getExternalId(), updatedCustomer.getExternalId());
        assertNull(updatedCustomer.getMasterExternalId());
        assertEquals(externalCustomer.getCompanyNumber(), updatedCustomer.getCompanyNumber());
        assertEquals(externalCustomer.getPostalAddress(), updatedCustomer.getAddress());
        assertEquals(externalCustomer.getShoppingLists(), updatedCustomer.getShoppingLists());
        assertEquals(CustomerType.COMPANY, updatedCustomer.getCustomerType());
        assertNull(updatedCustomer.getPreferredStore());
    }

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        customerSync = new CustomerSync(repository);
    }

    @Test
    public void syncCustomerWithRepository(){
        ExternalCustomer external = new ExternalCustomer();
        external.setExternalId("123");
        external.setName("Mary");
        external.setBonusPointsBalance(150);
        external.setShoppingLists(Arrays.asList(new ShoppingList("lipstick", "blusher")));

        Customer existing = new Customer();
        existing.setCustomerType(CustomerType.PERSON);
        existing.setInternalId("456");
        existing.setExternalId("123");
        existing.setBonusPointsBalance(75);

        CustomerMatches matches = new CustomerMatches();
        matches.setCustomer(existing);
        matches.setMatchTerm("ExternalId");
        when(repository.loadPersonCustomer("123")).thenReturn(matches);

        boolean created = customerSync.syncWithDataLayer(external);

        assertFalse(created);
        assertEquals(Integer.valueOf(150), existing.getBonusPointsBalance());
        verify(repository).loadPersonCustomer("123");
        verify(repository).updateCustomerRecord(existing);
        verify(repository, never()).createCustomerRecord(any());
    }

    private ExternalCustomer createExternalCompany() {
        ExternalCustomer externalCustomer = new ExternalCustomer();
        externalCustomer.setExternalId("12345");
        externalCustomer.setName("Acme Inc.");
        externalCustomer.setAddress(new Address("123 main st", "Helsingborg", "SE-123 45"));
        externalCustomer.setCompanyNumber("470813-8895");
        externalCustomer.setShoppingLists(Arrays.asList(new ShoppingList("lipstick", "blusher")));
        return externalCustomer;
    }

    private Customer createCustomerWithSameCompanyAs(ExternalCustomer externalCustomer) {
        Customer customer = new Customer();
        customer.setCompanyNumber(externalCustomer.getCompanyNumber());
        customer.setCustomerType(CustomerType.COMPANY);
        customer.setInternalId("45435");
        return customer;
    }

}
