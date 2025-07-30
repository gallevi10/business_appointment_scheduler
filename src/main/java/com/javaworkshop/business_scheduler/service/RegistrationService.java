package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.Customer;
import com.javaworkshop.business_scheduler.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// This class handles the registration of new customers.
@Service
public class RegistrationService {

    private final UserService userService;
    private final CustomerService customerService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegistrationService(UserService userService,
                               CustomerService customerService,
                               PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.customerService = customerService;
        this.passwordEncoder = passwordEncoder;
    }

    // registers a new customer with the provided details.
    // The method is annotated with @Transactional to ensure that the registration operation is atomic.
    @Transactional
    public void registerNewCustomer(String username, String password, String confirmPassword,
                                    String email, String phone, String firstName, String lastName) {

        // user details validation
        userService.validateUser(username, password, confirmPassword);
        User user = new User(username, passwordEncoder.encode(password), "ROLE_CUSTOMER", true);

        // customer details validation
        Customer existingCustomer = customerService.findByEmailAndPhone(email, phone);
        Customer customerToSave = customerService.getValidCustomer(
                existingCustomer, email, phone,
                firstName, lastName, username
        );

        // after all validations, save the user and customer
        customerToSave.setUser(user);
        userService.save(user);
        customerService.save(customerToSave);
    }

}
