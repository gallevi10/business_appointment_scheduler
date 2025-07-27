package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.Customer;
import com.javaworkshop.business_scheduler.model.User;
import com.javaworkshop.business_scheduler.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

        Customer customer = null;

        if (customerOptional.isPresent()) {
            customer = customerOptional.get();
        } else {
            throw new RuntimeException("Did not find customer id - " + id);
        }

        return customer;
    }

    @Override
    public Customer findByEmailAndPhone(String email, String phone) {

        Optional<Customer> customerOptional = customerRepository.findByEmailAndPhone(email, phone);

        Customer customer = null;

        if (customerOptional.isPresent()) {
            customer = customerOptional.get();
        }

        return customer;
    }

    @Override
    public Customer findByUsername(String username) {
        Optional<Customer> customerOptional = customerRepository.findByUserUsername(username);

        Customer customer = null;

        if (customerOptional.isPresent()) {
            customer = customerOptional.get();
        }

        return customer;
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
    public Customer validateCustomer(Customer existingCustomer, String email, String phone,
                                     String firstName, String lastName, String username, boolean isUpdate) {

        if (customerRepository.existsByEmail(existingCustomer, email)) {
            throw new RuntimeException("error.customer.email.conflict");
        }
        if (customerRepository.existsByPhone(existingCustomer, phone)) {
            throw new RuntimeException("error.customer.phone.conflict");
        }

        Customer validCustomer;
        if (existingCustomer == null) {
            validCustomer = new Customer(null, firstName, lastName, email, phone);
        }
        else if (!isUpdate && !existingCustomer.getFirstName().equals(firstName)
                || !existingCustomer.getLastName().equals(lastName)) {
            throw new RuntimeException("error.customer.email.and.phone.conflict");
        }
        else if (username != null && existingCustomer.getUser() != null
                && !existingCustomer.getUser().getUsername().equals(username)) {
            throw new RuntimeException("error.customer.username.conflict");
        }
        else {
            validCustomer = existingCustomer;
        }

        return validCustomer;
    }

    @Override
    public void updateCustomerDetails(Customer customer, String email, String phone, String firstName, String lastName) {
        validateCustomer(customer, email, phone, firstName, lastName, null, true);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        save(customer);
    }

}
