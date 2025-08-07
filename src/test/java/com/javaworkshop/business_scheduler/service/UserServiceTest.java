package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.User;
import com.javaworkshop.business_scheduler.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private User firstUser, secondUser, thirdUser;

    @BeforeEach
    void setUp() {
        firstUser = new User(UUID.randomUUID(),"firstUser",
                passwordEncoder.encode("111111111"), "ROLE_CUSTOMER", true);
        secondUser = new User(UUID.randomUUID(), "secondUser",
                passwordEncoder.encode("222222222"), "ROLE_CUSTOMER", true);
        thirdUser = new User(UUID.randomUUID(), "thirdUser",
                passwordEncoder.encode("333333333"), "ROLE_OWNER", true);
    }

    @DisplayName("Find All Users")
    @Test
    void findAllUsers() {
        List<User> expected = List.of(firstUser, secondUser, thirdUser);

        when(userRepository.findAll()).thenReturn(expected);

        List<User> foundUsers = userService.findAll();

        assertAll(
                () -> assertNotNull(foundUsers, "User list should not be null"),
                () -> assertEquals(3, foundUsers.size(), "User list size should be 3"),
                () -> assertIterableEquals(expected, foundUsers, "User lists should be equal")
        );

        verify(userRepository).findAll();
    }

    @DisplayName("Find User by ID")
    @Test
    void findUserById() {
        Map<UUID, Optional<User>> userMap = Map.of(
                firstUser.getId(), Optional.of(firstUser),
                secondUser.getId(), Optional.of(secondUser),
                thirdUser.getId(), Optional.of(thirdUser)
        );

        userMap.forEach(
            (id, optionalUser) -> {
                when(userRepository.findById(id)).thenReturn(optionalUser);
                assertEquals(optionalUser.orElse(null), userService.findById(id),
                        "User should be found by id: " + id);
                verify(userRepository).findById(id);
            }
        );

    }

    @DisplayName("Find User by Username")
    @Test
    void findByUsername() {
        Map<String, Optional<User>> userMap = Map.of(
                firstUser.getUsername(), Optional.of(firstUser),
                secondUser.getUsername(), Optional.of(secondUser),
                thirdUser.getUsername(), Optional.of(thirdUser)
        );

        userMap.forEach(
                (username, optionalUser) -> {
                    when(userRepository.findByUsername(username)).thenReturn(optionalUser);
                    assertEquals(optionalUser.orElse(null), userService.findByUsername(username),
                            "User should be found by username: " + username);
                    verify(userRepository).findByUsername(username);
                }
        );

    }

    @DisplayName("Save a User")
    @Test
    void save() {
        List<User> users = List.of(firstUser, secondUser, thirdUser);

        users.forEach(
                user -> {
                    when(userRepository.save(user)).thenReturn(user);
                    assertEquals(userService.save(user), user, "Should be equal to: " + user);
                    verify(userRepository).save(user);
                }
        );

    }

    @DisplayName("Delete a User By Id")
    @Test
    void deleteById() {
        UUID id = UUID.randomUUID();

        userService.deleteById(id);

        verify(userRepository).deleteById(id);
    }

    @DisplayName("Invalid Inputs For User Validation")
    @Test
    void invalidInputsForUserValidation() {
        // exists username case
        List<User> users = List.of(firstUser, secondUser, thirdUser);

        users.forEach(
            user -> {
                when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);
                assertThrows(RuntimeException.class, () ->
                        userService.validateUser(user.getUsername(), "", ""),
                        "Should throw an exception for existing username");
                verify(userRepository).existsByUsername(user.getUsername());
            }
        );

        // password mismatch case
        when(userRepository.existsByUsername("")).thenReturn(false);
        String password = "123456789";
        String wrongConfirmPassword = "111111111";

        assertThrows(RuntimeException.class, () ->
                userService.validateUser("", password, wrongConfirmPassword),
                "Should throw an exception for password confirmation mismatch");
    }

    @DisplayName("Valid Inputs For User Validation")
    @Test
    void validInputsForUserValidation() {
        // nonexistent username case
        String username = "nonexistentusername";
        when(userRepository.existsByUsername(username)).thenReturn(false);
        assertDoesNotThrow(() -> userService.validateUser(username, "", ""),
                "Should not throw an exception for nonexistent username");
        verify(userRepository).existsByUsername(username);

        // password match case
        when(userRepository.existsByUsername("")).thenReturn(false);
        String password = "123456789";
        assertDoesNotThrow(() -> userService.validateUser("", password, password));
    }

    @DisplayName("Username Exists And Not Exists")
    @Test
    void usernameExists() {
        List<User> users = List.of(firstUser, secondUser, thirdUser);

        // exists case
        users.forEach(
            user -> {
                when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);
                assertTrue(userService.usernameExists(user.getUsername()),
                        "Username should exist: " + user.getUsername());
                verify(userRepository).existsByUsername(user.getUsername());
            }
        );


        // nonexistent case
        String username = "nonexistentusername";
        when(userRepository.existsByUsername(username)).thenReturn(false);
        assertFalse(userService.usernameExists(username),
                "Username should not exist");
        verify(userRepository).existsByUsername(username);
    }

    @DisplayName("Invalid Inputs for Change Password")
    @Test
    void invalidInputsForChangePassword() {

        String oldPassword = "111111111";
        String wrongPassword = "111111112";
        String newPassword = "123456789";

        // old password mismatch case
        assertThrows(RuntimeException.class, () ->
                userService.changePassword(firstUser, wrongPassword, newPassword,  newPassword),
                "Should throw an exception for incorrect old password");

        // new password mismatch case
        assertThrows(RuntimeException.class, () ->
                userService.changePassword(firstUser, oldPassword, wrongPassword,  newPassword),
                "Should throw an exception for new password mismatch");

    }

    @DisplayName("Valid Inputs for Change Password")
    @Test
    void validInputsForChangePassword() {

        String oldPassword = "111111111";
        String newPassword = "123456789";
        User newFirstUser = new User(
                firstUser.getId(),
                firstUser.getUsername(),
                passwordEncoder.encode(newPassword),
                "ROLE_CUSTOMER",
                true
        );

        when(userRepository.save(newFirstUser)).thenReturn(newFirstUser);


        assertDoesNotThrow(() ->
                userService.changePassword(firstUser, oldPassword, newPassword,  newPassword),
                "Should not throw an exception on valid inputs");

        assertEquals(userService.save(newFirstUser), newFirstUser, "Should be equal to: " + newFirstUser);

        verify(userRepository).save(newFirstUser);
    }

    @DisplayName("Valid Case For Add New Owner User")
    @Test
    void validCaseForAddNewOwnerUser() {
        String newOwnerUsername = "newOwnerUsername";
        String newOwnerPassword = "newOwnerPassword";
        User newOwnerUser = new User(
                UUID.randomUUID(),
                newOwnerUsername,
                passwordEncoder.encode(newOwnerPassword),
                "ROLE_OWNER",
                true
        );

        when(userRepository.save(newOwnerUser)).thenReturn(newOwnerUser);

        assertDoesNotThrow(() -> userService.addNewOwnerUser(newOwnerUsername, newOwnerPassword, newOwnerPassword),
                "Should not throw an exception on valid inputs");

        assertEquals(userService.save(newOwnerUser), newOwnerUser, "Should be equal to: " + newOwnerUser);

        verify(userRepository).save(newOwnerUser);
    }

}