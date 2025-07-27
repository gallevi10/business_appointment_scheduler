package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.Customer;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    List<Customer> findAll();

    Customer findById(UUID id);

    Customer findByEmailAndPhone(String email, String phone);

    Customer findByUsername(String username);

    UUID findIdByUsername(String username);

    Customer save(Customer customer);

    void deleteById(UUID id);

    Customer getValidCustomer(Customer existingCustomer, String email, String phone,
                              String firstName, String lastName, String username);

    void updateCustomerDetails(Customer customer, String email, String phone,
                                      String firstName, String lastName);

}
