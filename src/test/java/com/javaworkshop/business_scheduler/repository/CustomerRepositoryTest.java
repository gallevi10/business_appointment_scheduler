package com.javaworkshop.business_scheduler.repository;

import com.javaworkshop.business_scheduler.model.Customer;
import com.javaworkshop.business_scheduler.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    private Customer firstCustomer, secondCustomer;

    @BeforeEach
    void setUp() {
        User user = new User("user", "password", "ROLE_CUSTOMER", true);
        firstCustomer = new Customer(user, "David", "Cohen", "david@server.com", "0541111111");
        secondCustomer = new Customer(null, "Moshe", "Levi", "moshe@server.com", "0542222222");
        userRepository.save(user);
        customerRepository.save(firstCustomer);
        customerRepository.save(secondCustomer);
    }

    @AfterEach
    void tearDown() {
        customerRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("Find Exists Customer By Email And Phone")
    @Test
    void findExistsCustomerByEmailAndPhone() {

        Customer firstExists = customerRepository
            .findByEmailAndPhone("david@server.com", "0541111111")
            .orElse(null);

        Customer secondExists = customerRepository
            .findByEmailAndPhone("moshe@server.com", "0542222222")
            .orElse(null);

        assertAll(
            () -> assertNotNull(firstExists, "First customer should exist"),
            () -> assertNotNull(secondExists, "Second customer should exist"),
            () -> assertEquals(firstCustomer, firstExists, "First customer should match"),
            () -> assertEquals(secondCustomer, secondExists, "Second customer should match")
        );

    }

    @DisplayName("Find Not Exists Customer By Email And Phone")
    @Test
    void findNotExistsCustomerByEmailAndPhone() {

        Customer exists = customerRepository
            .findByEmailAndPhone("Avi@server.com", "0543333333")
            .orElse(null);

        assertNull(exists, "The customer should not exist");

    }

    @DisplayName("Find Exists Customer By Username")
    @Test
    void findExistsCustomerByUserUsername() {
        Customer exists = customerRepository
            .findByUserUsername("user")
            .orElse(null);

        assertAll(
            () -> assertNotNull(exists, "Customer should exist"),
            () -> assertEquals(firstCustomer, exists, "Customer should match")
        );
    }

    @DisplayName("Find Not Exists Customer By Username")
    @Test
    void findNotExistsCustomerByUserUsername() {
        Customer exists = customerRepository
            .findByUserUsername("nonexistent")
            .orElse(null);

        assertNull(exists, "Customer should not exist");
    }

    @DisplayName("Exists Customer by Its Own Email Excluding Itself")
    @Test
    void ExistsCustomerByItsOwnEmailExcludingItself() {
        boolean exists = customerRepository.existsByEmail(firstCustomer, firstCustomer.getEmail());

        assertFalse(exists, "Should be false since we are checking against the same customer");
    }

    @DisplayName("Exists Customer by Another Customer's Email Excluding Itself")
    @Test
    void ExistsCustomerByAnotherCustomerEmailExcludingItself() {
        boolean exists = customerRepository.existsByEmail(firstCustomer, secondCustomer.getEmail());

        assertTrue(exists, "Should be true since the email belongs to another customer");
    }

    @DisplayName("Not Exists Customer Email Excluding Itself")
    @Test
    void NotExistsCustomerEmailExcludingItself() {
        boolean exists = customerRepository.existsByEmail(firstCustomer, "nonexistent@server.com");

        assertFalse(exists, "Should be false since the email does not belong to any customer");
    }

    @DisplayName("Exists Customer by Email With No Exclusion")
    @Test
    void ExistsCustomerByEmailWithNoExclusion() {
        boolean exists = customerRepository.existsByEmail(null, firstCustomer.getEmail());

        assertTrue(exists, "Should be true since the email belongs to an existing customer");
    }

    @DisplayName("Not Exists Customer by Email With No Exclusion")
    @Test
    void notExistsCustomerByEmailWithNoExclusion() {
        boolean exists = customerRepository.existsByEmail(null, "nonexistent@server.com");

        assertFalse(exists, "Should be false since the email does not belong to any customer");
    }

    @DisplayName("Exists Customer by Its Own Phone Excluding Itself")
    @Test
    void ExistsCustomerByItsOwnPhoneExcludingItself() {
        boolean exists = customerRepository.existsByPhone(firstCustomer, firstCustomer.getPhone());

        assertFalse(exists, "Should be false since we are checking against the same customer");
    }

    @DisplayName("Exists Customer by Another Customer's Phone Excluding Itself")
    @Test
    void ExistsCustomerByAnotherCustomerPhoneExcludingItself() {
        boolean exists = customerRepository.existsByPhone(firstCustomer, secondCustomer.getPhone());

        assertTrue(exists, "Should be true since the phone belongs to another customer");
    }

    @DisplayName("Not Exists Customer Phone Excluding Itself")
    @Test
    void NotExistsCustomerPhoneExcludingItself() {
        boolean exists = customerRepository.existsByPhone(firstCustomer, "0543333333");

        assertFalse(exists, "Should be false since the phone does not belong to any customer");
    }

    @DisplayName("Exists Customer by Phone With No Exclusion")
    @Test
    void ExistsCustomerByPhoneWithNoExclusion() {
        boolean exists = customerRepository.existsByPhone(null, firstCustomer.getPhone());

        assertTrue(exists, "Should be true since the phone belongs to an existing customer");
    }

    @DisplayName("Not Exists Customer by Phone With No Exclusion")
    @Test
    void notExistsCustomerByPhoneWithNoExclusion() {
        boolean exists = customerRepository.existsByPhone(null, "0543333333");

        assertFalse(exists, "Should be false since the phone does not belong to any customer");
    }
}