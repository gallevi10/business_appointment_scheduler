package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.User;

import java.util.List;
import java.util.UUID;

// This interface defines the contract for user-related operations in the business scheduler application.
public interface UserService {

    List<User> findAll();

    User findById(UUID id);

    User findByUsername(String username);

    User save(User user);

    void deleteById(UUID id);

    // validates the user credentials and throws an exception if invalid
    void validateUser(String username, String password, String confirmPassword);

    boolean usernameExists(String username);

    // changes the password for the given user
    void changePassword(User user, String oldPassword, String newPassword, String confirmNewPassword);

    // adds a new owner user with the provided credentials
    void addNewOwnerUser(String username, String password, String confirmPassword);
}
