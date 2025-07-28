package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.Customer;
import com.javaworkshop.business_scheduler.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// This class implements the CustomerService interface providing methods
// for managing customers in the business scheduler application.
@Service
public class CustomerServiceImpl implements CustomerService{

    private CustomerRepository customerRepository;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Override
    public Customer findById(UUID id) {
        Optional<Customer> customerOptional = customerRepository.findById(id);
        return customerOptional.orElse(null);
    }

    @Override
    public Customer findByEmailAndPhone(String email, String phone) {
        Optional<Customer> customerOptional = customerRepository.findByEmailAndPhone(email, phone);
        return customerOptional.orElse(null);
    }

    @Override
    public Customer findByUsername(String username) {
        Optional<Customer> customerOptional = customerRepository.findByUserUsername(username);
        return customerOptional.orElse(null);
    }

    @Override
    public UUID findIdByUsername(String username) {
        Optional<Customer> customerIdOptional = customerRepository.findByUserUsername(username);

        UUID customerId = null;

        if (customerIdOptional.isPresent()) {
            customerId = customerIdOptional.get().getId();
        }

        return customerId;
    }

    @Override
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public void deleteById(UUID id) {
        customerRepository.deleteById(id);
    }

    @Override
    public Customer getValidCustomer(Customer existingCustomer, String email, String phone,
                                     String firstName, String lastName, String username) {

        validateEmailOrPhone(existingCustomer, email, phone);

        Customer validCustomer;
        if (existingCustomer == null) {
            validCustomer = new Customer(null, firstName, lastName, email, phone);
        }
        else if (!existingCustomer.getFirstName().equals(firstName)
                || !existingCustomer.getLastName().equals(lastName)) { // existing customer has different name
            throw new RuntimeException("error.customer.email.and.phone.conflict");
        }
        else if (username != null && existingCustomer.getUser() != null
                && !existingCustomer.getUser().getUsername().equals(username)) { // existing customer has different username
            throw new RuntimeException("error.customer.username.conflict");
        }
        else {
            validCustomer = existingCustomer;
        }

        return validCustomer;
    }

    private void validateEmailOrPhone(Customer customer, String email, String phone) {
        if (customerRepository.existsByEmail(customer, email)) {
            throw new RuntimeException("error.customer.email.conflict");
        }
        if (customerRepository.existsByPhone(customer, phone)) {
            throw new RuntimeException("error.customer.phone.conflict");
        }
    }

    @Override
    public void updateCustomerDetails(Customer customer, String email, String phone, String firstName, String lastName) {
        validateEmailOrPhone(customer, email, phone);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        save(customer);
    }

}
