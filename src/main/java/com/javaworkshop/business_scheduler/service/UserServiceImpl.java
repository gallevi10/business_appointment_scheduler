package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.User;
import com.javaworkshop.business_scheduler.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// This class implements the UserService interface providing methods
// for managing users in the business scheduler application.
@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(UUID id) {
        Optional<User> userOptional = userRepository.findById(id);
        return userOptional.orElse(null);
    }

    @Override
    public User findByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        return userOptional.orElse(null);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }

    @Override
    public void validateUser(String username, String password, String confirmPassword) {

        if (usernameExists(username)) {
            throw new RuntimeException("error.user.username.conflict");
        }
        if (!password.equals(confirmPassword)) {
            throw new RuntimeException("error.user.password.confirmation.mismatch");
        }

    }

    @Override
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public void changePassword(User user, String oldPassword, String newPassword, String confirmNewPassword) {

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("error.user.old.password.incorrect");
        }

        if (!newPassword.equals(confirmNewPassword)) {
            throw new RuntimeException("error.user.password.confirmation.mismatch");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public synchronized void addNewOwnerUser(String username, String password, String confirmPassword) {

        validateUser(username, password, confirmPassword);
        User user = new User(username, passwordEncoder.encode(password), "ROLE_OWNER", true);
        save(user);

    }
}
