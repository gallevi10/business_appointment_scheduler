package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.config.DefaultInitializer;
import com.javaworkshop.business_scheduler.model.User;
import com.javaworkshop.business_scheduler.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceTest {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private DefaultInitializer defaultInitializer;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    private User firstUser, secondUser, thirdUser;

    @BeforeEach
    void setUp() {
        firstUser = new User(UUID.randomUUID(), "firstUser",
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

        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        assertNull(userService.findById(nonExistentId),
            "User should not be found for non-existent id: " + nonExistentId);
        verify(userRepository).findById(nonExistentId);

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
        // existing username case
        List<User> users = List.of(firstUser, secondUser, thirdUser);

        users.forEach(
            user -> {
                when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);
                Exception exception = assertThrows(RuntimeException.class, () ->
                        userService.validateUser(user.getUsername(), "", ""),
                    "Should throw an exception for existing username");
                assertEquals("error.user.username.conflict", exception.getMessage(),
                    "Exception message should match");
                verify(userRepository).existsByUsername(user.getUsername());
            }
        );

        // password mismatch case
        when(userRepository.existsByUsername("")).thenReturn(false);
        String password = "123456789";
        String wrongConfirmPassword = "111111111";

        Exception exception = assertThrows(RuntimeException.class, () ->
                userService.validateUser("", password, wrongConfirmPassword),
            "Should throw an exception for password confirmation mismatch");
        assertEquals("error.user.password.confirmation.mismatch", exception.getMessage(),
            "Exception message should match");
    }

    @DisplayName("Valid Inputs For User Validation")
    @Test
    void validInputsForUserValidation() {
        // non-existent username case
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
        Exception incorrectOldPasswordException = assertThrows(RuntimeException.class, () ->
                userService.changePassword(firstUser, wrongPassword, newPassword, newPassword),
            "Should throw an exception for incorrect old password");

        assertEquals("error.user.old.password.incorrect",
            incorrectOldPasswordException.getMessage(),
            "Exception message should match");

        // new password mismatch case
        Exception newPasswordMismatchException = assertThrows(RuntimeException.class, () ->
                userService.changePassword(firstUser, oldPassword, wrongPassword, newPassword),
            "Should throw an exception for new password mismatch");

        assertEquals("error.user.password.confirmation.mismatch",
            newPasswordMismatchException.getMessage(),
            "Exception message should match");

        assertTrue(passwordEncoder.matches(oldPassword, firstUser.getPassword()),
            "Password should remain unchanged");

        verify(userRepository, times(0)).save(any(User.class));

    }

    @DisplayName("Valid Inputs for Change Password")
    @Test
    void validInputsForChangePassword() {

        User userToChangePassword = firstUser;
        String oldPassword = "111111111";
        String newPassword = "123456789";

        when(userRepository.save(userToChangePassword))
            .thenReturn(userToChangePassword);

        assertDoesNotThrow(() ->
                userService.changePassword(userToChangePassword, oldPassword, newPassword, newPassword),
            "Should not throw an exception on valid inputs");

        assertTrue(passwordEncoder.matches(newPassword, userToChangePassword.getPassword()),
            "Password should be updated to the new password");

        verify(userRepository).save(userToChangePassword);
    }

    @DisplayName("Valid Case For Add New Owner User")
    @Test
    void validCaseForAddNewOwnerUser() {
        String newOwnerUsername = "newOwnerUsername";
        String newOwnerPassword = "newOwnerPassword";

        when(userRepository.save(any(User.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> userService.addNewOwnerUser(newOwnerUsername, newOwnerPassword, newOwnerPassword),
            "Should not throw an exception on valid inputs");

        verify(userRepository).save(any(User.class));
    }

}