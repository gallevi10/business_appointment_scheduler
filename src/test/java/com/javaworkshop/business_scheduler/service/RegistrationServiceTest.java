package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.Customer;
import com.javaworkshop.business_scheduler.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest
class RegistrationServiceTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CustomerService customerService;

    @Autowired
    private RegistrationService registrationService;

    private static String username, password, email, phone, firstName, lastName;

    @BeforeAll
    static void setUp() {
        username = "username";
        password = "password123";
        email = "email@someservice.com";
        phone = "05411111111";
        firstName = "First";
        lastName = "Last";
    }

    @DisplayName("Invalid User Registration - Username Conflict")
    @Test
    void invalidUserRegistrationUsernameConflict() {
        doThrow(new RuntimeException())
                .when(userService)
                .validateUser(username, password, password);

        assertThrows(RuntimeException.class, () ->
            registrationService.registerNewCustomer(username, password, password,
                    email, phone, firstName, lastName)
        , "Expected validateUser to throw an exception for existing username");

    }

    @DisplayName("Invalid User Registration - Password Confirmation Mismatch")
    @Test
    void invalidUserRegistrationPasswordConfirmationMismatch() {

        String wrongPassword = "wrongPassword123";

        doThrow(new RuntimeException())
                .when(userService)
                .validateUser(username, password, wrongPassword);

        assertThrows(RuntimeException.class, () ->
                        registrationService.registerNewCustomer(username, password, password,
                                email, phone, firstName, lastName)
                , "Expected validateUser to throw an exception for password confirmation mismatch");

    }

    @DisplayName("Invalid User Registration - Customer Already Exists With Different Name")
    @Test
    void invalidUserRegistrationCustomerAlreadyExistsWithDifferentName() {

        Customer existingCustomer = new Customer(UUID.randomUUID(), null, firstName, lastName, email, phone);
        String wrongFirstName = "WrongFirst";
        String wrongLastName = "WrongLast";

        when(customerService.findByEmailAndPhone(email, phone))
                .thenReturn(existingCustomer);

        when(customerService.getValidCustomer(existingCustomer, email, phone, wrongFirstName, lastName, username))
                .thenThrow(new RuntimeException());

        when(customerService.getValidCustomer(existingCustomer, email, phone, firstName, wrongLastName, username))
                .thenThrow(new RuntimeException());

        when(customerService.getValidCustomer(existingCustomer, email, phone, wrongFirstName, wrongLastName, username))
                .thenThrow(new RuntimeException());

        assertAll(
            () -> assertThrows(RuntimeException.class, () ->
                    registrationService.registerNewCustomer(username, password, password,
                            email, phone, wrongFirstName, lastName),
                    "Expected getValidCustomer to throw an exception for existing customer with different first name"
            ),
            () -> assertThrows(RuntimeException.class, () ->
                    registrationService.registerNewCustomer(username, password, password,
                            email, phone, firstName, wrongLastName),
                    "Expected getValidCustomer to throw an exception for existing customer with different last name"
            ),
            () -> assertThrows(RuntimeException.class, () ->
                    registrationService.registerNewCustomer(username, password, password,
                            email, phone, wrongFirstName, wrongLastName),
                    "Expected getValidCustomer to throw an exception for existing customer with different first and last names"
            )
        );
    }

    @DisplayName("Invalid User Registration - Customer Already Exists With Different Username")
    @Test
    void invalidUserRegistrationCustomerAlreadyExistsWithDifferentUsername() {

        User existingUser = new User(username, password, "ROLE_CUSTOMER", true);
        Customer existingCustomer = new Customer(UUID.randomUUID(), existingUser,
                firstName, lastName, email, phone);

        String wrongUsername = "WrongUsername";

        when(customerService.findByEmailAndPhone(email, phone))
                .thenReturn(existingCustomer);

        when(customerService.getValidCustomer(existingCustomer, email, phone, firstName, lastName, wrongUsername))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () ->
            registrationService.registerNewCustomer(wrongUsername, password, password,
                    email, phone, firstName, lastName),
    "Expected getValidCustomer to throw an exception for existing customer with different username"
        );
    }

    @DisplayName("Successful User Registration When Customer Exists")
    @Test
    void successfulUserRegistrationWhenCustomerExists() {

        Customer existingCustomer = new Customer(UUID.randomUUID(), null,
                firstName, lastName, email, phone);
        User newUser = new User(username, password,
                "ROLE_CUSTOMER", true);

        when(customerService.findByEmailAndPhone(email, phone))
                .thenReturn(existingCustomer);

        when(customerService.getValidCustomer(existingCustomer, email, phone, firstName, lastName, username))
                .thenReturn(existingCustomer);

        when(userService.save(newUser)).thenReturn(newUser);
        existingCustomer.setUser(newUser);
        when(customerService.save(existingCustomer)).thenReturn(existingCustomer);

        assertDoesNotThrow(() ->
            registrationService.registerNewCustomer(username, password, password,
                    email, phone, firstName, lastName),
            "Expected no exception during successful user registration when customer already exists"
        );

    }

    @DisplayName("Successful User Registration When Customer Does Not Exist")
    @Test
    void successfulUserRegistrationWhenCustomerDoesNotExist() {

        User newUser = new User(username, password, "ROLE_CUSTOMER", true);
        Customer newCustomer = new Customer(null, firstName, lastName, email, phone);

        when(customerService.findByEmailAndPhone(email, phone))
                .thenReturn(null);

        when(customerService.getValidCustomer(null, email, phone, firstName, lastName, username))
                .thenReturn(newCustomer);

        when(userService.save(newUser)).thenReturn(newUser);
        newCustomer.setUser(newUser);
        when(customerService.save(newCustomer)).thenReturn(newCustomer);

        assertDoesNotThrow(() ->
            registrationService.registerNewCustomer(username, password, password,
                    email, phone, firstName, lastName),
    "Expected no exception during successful user registration when customer does not exist"
        );

    }
}