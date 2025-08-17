package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.Appointment;
import com.javaworkshop.business_scheduler.model.Customer;
import com.javaworkshop.business_scheduler.model.User;
import com.javaworkshop.business_scheduler.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class CustomerServiceTest {

    @MockitoBean
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerService customerService;

    private Customer firstCustomer, secondCustomer, thirdCustomer;

    private Appointment firstAppointment, secondAppointment, thirdAppointment;

    @BeforeEach
    void setUp() {
        firstCustomer = new Customer(UUID.randomUUID(), null,
            "First", "First", "first@someservice.com", "0541111111");
        secondCustomer = new Customer(UUID.randomUUID(), null,
            "Second", "Second", "second@someservice.com", "0542222222");
        User user = new User(UUID.randomUUID(), "user",
            "password123", "ROLE_CUSTOMER", true);
        thirdCustomer = new Customer(UUID.randomUUID(), user,
            "Third", "Third", "third@someservice.com", "0543333333");

    }

    @DisplayName("Find All Customers")
    @Test
    void findAllCustomers() {
        List<Customer> expected = List.of(firstCustomer, secondCustomer, thirdCustomer);

        when(customerService.findAll()).thenReturn(expected);

        assertIterableEquals(expected, customerService.findAll());

        verify(customerRepository).findAll();
    }

    @DisplayName("Find Customer By ID")
    @Test
    void findCustomerById() {
        List<Customer> customers = List.of(firstCustomer, secondCustomer, thirdCustomer);

        customers.forEach(customer -> {
                when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
                assertEquals(customer, customerService.findById(customer.getId()));
                verify(customerRepository).findById(customer.getId());
            }
        );

        UUID notExistingId = UUID.randomUUID();
        when(customerRepository.findById(notExistingId)).thenReturn(Optional.empty());
        assertNull(customerService.findById(notExistingId),
            "Expected null when customer id does not exist");
        verify(customerRepository).findById(notExistingId);
    }

    @DisplayName("Find Customer By Email And Phone")
    @Test
    void findCustomerByEmailAndPhone() {
        List<Customer> customers = List.of(firstCustomer, secondCustomer, thirdCustomer);

        customers.forEach(customer -> {
                when(customerRepository.findByEmailAndPhone(customer.getEmail(), customer.getPhone()))
                    .thenReturn(Optional.of(customer));
                assertEquals(customer, customerService.findByEmailAndPhone(
                    customer.getEmail(), customer.getPhone())
                );
                verify(customerRepository).findByEmailAndPhone(customer.getEmail(), customer.getPhone());
            }
        );

        String nonExistentEmail = "nonexistent@someserver.com";
        String nonExistentPhone = "0549999999";
        when(customerRepository.findByEmailAndPhone(nonExistentEmail, nonExistentPhone))
            .thenReturn(Optional.empty());
        assertNull(customerService.findByEmailAndPhone(nonExistentEmail, nonExistentPhone),
            "Expected null when email and phone do not match any customer");
        verify(customerRepository).findByEmailAndPhone(nonExistentEmail, nonExistentPhone);
    }

    @DisplayName("Find Customer By Username")
    @Test
    void findCustomerByUsername() {

        when(customerRepository.findByUserUsername(thirdCustomer.getUser().getUsername()))
            .thenReturn(Optional.of(thirdCustomer));
        assertEquals(thirdCustomer, customerService.findByUsername(
            thirdCustomer.getUser().getUsername())
        );
        verify(customerRepository).findByUserUsername(
            thirdCustomer.getUser().getUsername()
        );

        String nonExistentUsername = "nonexistentUser";
        when(customerRepository.findByUserUsername(nonExistentUsername))
            .thenReturn(Optional.empty());
        assertNull(customerService.findByUsername(nonExistentUsername),
            "Expected null when username does not match any customer");
        verify(customerRepository).findByUserUsername(nonExistentUsername);
    }

    @DisplayName("Find Customer ID By Username")
    @Test
    void findCustomerIdByUsername() {
        when(customerRepository.findByUserUsername(thirdCustomer.getUser().getUsername()))
            .thenReturn(Optional.of(thirdCustomer));
        assertEquals(thirdCustomer.getId(), customerService.findIdByUsername(
            thirdCustomer.getUser().getUsername())
        );
        verify(customerRepository).findByUserUsername(
            thirdCustomer.getUser().getUsername()
        );

        String nonExistentUsername = "nonexistentUser";
        when(customerRepository.findByUserUsername(nonExistentUsername))
            .thenReturn(Optional.empty());
        assertNull(customerService.findIdByUsername(nonExistentUsername),
            "Expected null when username does not match any customer");
        verify(customerRepository).findByUserUsername(nonExistentUsername);
    }

    @DisplayName("Save Customer")
    @Test
    void saveCustomer() {
        List<Customer> customers = List.of(firstCustomer, secondCustomer, thirdCustomer);

        customers.forEach(customer -> {
            when(customerRepository.save(customer)).thenReturn(customer);
            assertEquals(customer, customerService.save(customer),
                "Expected saved customer to match the original");
            verify(customerRepository).save(customer);
        });
    }

    @DisplayName("Delete Customer By ID")
    @Test
    void deleteCustomerById() {
        UUID id = UUID.randomUUID();

        customerService.deleteById(id);

        verify(customerRepository).deleteById(id);
    }

    @DisplayName("Exception on Get Valid Customer - Email or Phone Conflict")
    @Test
    void exceptionOnGetValidCustomerEmailOrPhoneConflict() {
        String existingEmail = "existing@someserver.com";
        String existingPhone = "0541111111";
        String nonExistentEmail = "nonexistent@someserver.com";
        String nonExistentPhone = "0549999999";

        when(customerRepository.existsByEmail(null, existingEmail))
            .thenThrow(new RuntimeException());

        when(customerRepository.existsByPhone(null, existingPhone))
            .thenThrow(new RuntimeException());

        when(customerRepository.existsByEmail(null, nonExistentEmail))
            .thenReturn(false);

        when(customerRepository.existsByPhone(null, nonExistentPhone))
            .thenReturn(false);

        assertAll(
            () -> assertThrows(RuntimeException.class,
                () -> customerService.getValidCustomer(
                    null, existingEmail, existingPhone,
                    "New", "Customer", null)
                , "Expected exception for existing email and phone"),
            () -> assertThrows(RuntimeException.class,
                () -> customerService.getValidCustomer(
                    null, nonExistentEmail, existingPhone,
                    "New", "Customer", null)
                , "Expected exception for existing phone"),
            () -> assertThrows(RuntimeException.class,
                () -> customerService.getValidCustomer(
                    null, existingEmail, nonExistentPhone,
                    "New", "Customer", null)
                , "Expected exception for existing email")
        );
        verify(customerRepository, times(2)).existsByEmail(null, existingEmail);
        verify(customerRepository).existsByPhone(null, existingPhone);
        verify(customerRepository).existsByEmail(null, nonExistentEmail);
        verify(customerRepository, never()).existsByPhone(null, nonExistentPhone);
    }

    @DisplayName("Exception on Get Valid Customer - Existing Customer With Different Name")
    @Test
    void exceptionOnGetValidCustomerExistingCustomerWithDifferentName() {
        Customer existingCustomer = firstCustomer;
        String notMatchingFirstName = "NotMatchingFirstName";
        String notMatchingLastName = "NotMatchingLastName";

        when(customerRepository.existsByEmail(existingCustomer, existingCustomer.getEmail()))
            .thenReturn(false);
        when(customerRepository.existsByPhone(existingCustomer, existingCustomer.getPhone()))
            .thenReturn(false);

        assertAll(
            () -> assertThrows(RuntimeException.class,
                () -> customerService.getValidCustomer(
                    existingCustomer, existingCustomer.getEmail(),
                    existingCustomer.getPhone(), notMatchingFirstName,
                    notMatchingLastName, null)
                , "Expected exception for existing customer with completely different name"),
            () -> assertThrows(RuntimeException.class,
                () -> customerService.getValidCustomer(
                    existingCustomer, existingCustomer.getEmail(),
                    existingCustomer.getPhone(), existingCustomer.getFirstName(),
                    notMatchingLastName, null)
                , "Expected exception for existing customer with different last name"),
            () -> assertThrows(RuntimeException.class,
                () -> customerService.getValidCustomer(
                    existingCustomer, existingCustomer.getEmail(),
                    existingCustomer.getPhone(), notMatchingFirstName,
                    existingCustomer.getLastName(), null)
                , "Expected exception for existing customer with different first name")
        );
        verify(customerRepository, times(3))
            .existsByEmail(existingCustomer, existingCustomer.getEmail());
        verify(customerRepository, times(3))
            .existsByPhone(existingCustomer, existingCustomer.getPhone());
    }

    @DisplayName("Exception on Get Valid Customer - Existing Customer With Different Username")
    @Test
    void exceptionOnGetValidCustomerExistingCustomerWithDifferentUsername() {
        Customer existingCustomer = thirdCustomer;
        String notMatchingUsername = "NotMatchingUsername";

        when(customerRepository.existsByEmail(existingCustomer, existingCustomer.getEmail()))
            .thenReturn(false);
        when(customerRepository.existsByPhone(existingCustomer, existingCustomer.getPhone()))
            .thenReturn(false);

        assertThrows(RuntimeException.class,
            () -> customerService.getValidCustomer(
                existingCustomer, existingCustomer.getEmail(),
                existingCustomer.getPhone(), existingCustomer.getFirstName(),
                existingCustomer.getLastName(), notMatchingUsername),
            "Expected exception for existing customer with different username"
        );

        verify(customerRepository).existsByEmail(existingCustomer, existingCustomer.getEmail());
        verify(customerRepository).existsByPhone(existingCustomer, existingCustomer.getPhone());
    }

    @DisplayName("Success on Get Valid Customer - Existing Customer")
    @Test
    void successOnGetValidCustomerExistingCustomer() {
        List<Customer> customers = List.of(firstCustomer, secondCustomer, thirdCustomer);

        customers.forEach(existingCustomer -> {
            when(customerRepository.existsByEmail(existingCustomer, existingCustomer.getEmail()))
                .thenReturn(false);
            when(customerRepository.existsByPhone(existingCustomer, existingCustomer.getPhone()))
                .thenReturn(false);

            assertDoesNotThrow(() -> customerService.getValidCustomer(
                existingCustomer, existingCustomer.getEmail(),
                existingCustomer.getPhone(), existingCustomer.getFirstName(),
                existingCustomer.getLastName(),
                existingCustomer.getUser() != null ? existingCustomer.getUser().getUsername() : null
            ), "Expected no exception for existing customer with matching details");
            verify(customerRepository).existsByEmail(existingCustomer, existingCustomer.getEmail());
            verify(customerRepository).existsByPhone(existingCustomer, existingCustomer.getPhone());
        });
    }

    @DisplayName("Success on Get Valid Customer - New Customer")
    @Test
    void successOnGetValidCustomerNewCustomer() {
        Customer newCustomer = new Customer(
            UUID.randomUUID(),
            null,
            "New",
            "Customer",
            "newcustomer@someserver.com",
            "0544444444"
        );

        when(customerRepository.existsByEmail(null, newCustomer.getEmail()))
            .thenReturn(false);
        when(customerRepository.existsByPhone(null, newCustomer.getPhone()))
            .thenReturn(false);

        assertDoesNotThrow(() -> customerService.getValidCustomer(
            null, newCustomer.getEmail(),
            newCustomer.getPhone(), newCustomer.getFirstName(),
            newCustomer.getLastName(), null
        ), "Expected no exception for new customer with unique details");

        verify(customerRepository).existsByEmail(null, newCustomer.getEmail());
        verify(customerRepository).existsByPhone(null, newCustomer.getPhone());

    }

    @DisplayName("Exception on Update Customer Details - Email or Phone Conflict")
    @Test
    void exceptionOnUpdateCustomerDetailsEmailOrPhoneConflict() {

        List<Customer> customers = List.of(firstCustomer, secondCustomer, thirdCustomer);
        String existingEmail = "existing@someserver.com";
        String existingPhone = "0541111111";
        String nonExistentEmail = "nonexistent@someserver.com";
        String nonExistentPhone = "0549999999";

        customers.forEach(customer -> {
            when(customerRepository.existsByEmail(customer, existingEmail))
                .thenThrow(new RuntimeException());

            when(customerRepository.existsByPhone(customer, existingPhone))
                .thenThrow(new RuntimeException());
            when(customerRepository.existsByEmail(customer, nonExistentEmail))
                .thenReturn(false);
            when(customerRepository.existsByPhone(customer, nonExistentPhone))
                .thenReturn(false);
            assertAll(
                () -> assertThrows(RuntimeException.class,
                    () -> customerService.updateCustomerDetails(
                        customer, existingEmail, existingPhone,
                        customer.getFirstName(), customer.getLastName())
                    , "Expected exception for existing email and phone"),
                () -> assertThrows(RuntimeException.class,
                    () -> customerService.updateCustomerDetails(
                        customer, nonExistentEmail, existingPhone,
                        customer.getFirstName(), customer.getLastName())
                    , "Expected exception for existing phone"),
                () -> assertThrows(RuntimeException.class,
                    () -> customerService.updateCustomerDetails(
                        customer, existingEmail, nonExistentPhone,
                        customer.getFirstName(), customer.getLastName())
                    , "Expected exception for existing email")
            );
            verify(customerRepository, times(2)).existsByEmail(customer, existingEmail);
            verify(customerRepository).existsByPhone(customer, existingPhone);
            verify(customerRepository).existsByEmail(customer, nonExistentEmail);
            verify(customerRepository, never()).existsByPhone(customer, nonExistentPhone);
        });
    }

    @DisplayName("Success on Update Customer Details - Only First Name and Last Name Change")
    @Test
    void successOnUpdateCustomerDetailsOnlyFirstAndLastName() {

        Customer customerToUpdate = firstCustomer;
        String newFirstName = "UpdatedFirstName";
        String newLastName = "UpdatedLastName";

        when(customerRepository.existsByEmail(customerToUpdate, firstCustomer.getEmail()))
            .thenReturn(false);
        when(customerRepository.existsByPhone(customerToUpdate, firstCustomer.getPhone()))
            .thenReturn(false);

        assertDoesNotThrow(() -> customerService.updateCustomerDetails(
            customerToUpdate, customerToUpdate.getEmail(),
            customerToUpdate.getPhone(), newFirstName, newLastName
        ), "Expected no exception for existing customer with matching details");
        verify(customerRepository).existsByEmail(customerToUpdate, customerToUpdate.getEmail());
        verify(customerRepository).existsByPhone(customerToUpdate, customerToUpdate.getPhone());
    }

    @DisplayName("Success on Update Customer Details - Change All Details")
    @Test
    void successOnUpdateCustomerDetailsChangeAllDetails() {

        Customer customerToUpdate = firstCustomer;
        String newEmail = "new@someserver.com";
        String newPhone = "0545555555";
        String newFirstName = "UpdatedFirstName";
        String newLastName = "UpdatedLastName";

        when(customerRepository.existsByEmail(customerToUpdate, newEmail))
            .thenReturn(false);
        when(customerRepository.existsByPhone(customerToUpdate, newPhone))
            .thenReturn(false);

        assertDoesNotThrow(() -> customerService.updateCustomerDetails(
            customerToUpdate, newEmail, newPhone, newFirstName, newLastName
        ), "Expected no exception for updating customer details with unique email and phone");
        verify(customerRepository).existsByEmail(customerToUpdate, newEmail);
        verify(customerRepository).existsByPhone(customerToUpdate, newPhone);
    }
}