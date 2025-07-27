package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<User> findAll();

    User findById(UUID id);

    User findByUsername(String username);

    User save(User user);

    void deleteById(UUID id);

    void validateUser(String username, String password, String confirmPassword);

    boolean usernameExists(String username);

    void changePassword(User user, String oldPassword, String newPassword, String confirmNewPassword);

    void addNewOwnerUser(String username, String password, String confirmPassword);
}
