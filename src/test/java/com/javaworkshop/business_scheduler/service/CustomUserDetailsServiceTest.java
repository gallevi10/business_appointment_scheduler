package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.User;
import com.javaworkshop.business_scheduler.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class CustomUserDetailsServiceTest {

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @DisplayName("Exception On Trying To Load Non-Existent User By Username")
    @Test
    void exceptionOnTryingToLoadNonExistentUserByUsername() {
        String nonExistentUsername = "nonExistentUser";

        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername(nonExistentUsername)
        );
    }

    @DisplayName("Successfully Load User By Username")
    @Test
    void successfullyLoadUserByUsername() {
        String existingUsername = "existingUser";
        String password = "password";
        String role = "ROLE_SOMEROLE";

        User user = new User(existingUsername, password, role, true);

        when(userRepository.findByUsername(existingUsername)).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(existingUsername);

        assertAll(
            () -> assertNotNull(userDetails, "UserDetails should not be null"),
            () -> assertEquals(existingUsername, userDetails.getUsername(), "Username should match"),
            () -> assertEquals(password, userDetails.getPassword(), "Password should match"),
            () -> assertTrue(userDetails.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals(role)),
                "User should have the correct role"
            )
        );
    }
}