package com.javaworkshop.business_scheduler.repository;

import com.javaworkshop.business_scheduler.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("test", "123456789", "ROLE_CUSTOMER", true);
        userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @DisplayName("Find User By Username")
    @Test
    void findByUsername() {
        String username = "test";

        User foundUser = userRepository.findByUsername(username).orElse(null);

        assertNotNull(foundUser, "Found user should not be null");
        assertEquals(testUser.getUsername(), foundUser.getUsername(), "Found user should match the test user");

    }

    @DisplayName("Exists By Username")
    @Test
    void existsByUsername() {
        String username = "test";

        boolean exists = userRepository.existsByUsername(username);

        assertTrue(exists, "User should exist with the given username");
    }

    @DisplayName("Not Exists By Username")
    @Test
    void notExistsByUsername() {
        String username = "notexists";

        boolean exists = userRepository.existsByUsername(username);

        assertFalse(exists, "User should not exist with the given username");
    }
}