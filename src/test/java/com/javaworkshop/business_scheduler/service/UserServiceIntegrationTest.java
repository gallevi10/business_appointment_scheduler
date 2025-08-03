package com.javaworkshop.business_scheduler.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @DisplayName("Asynchronous Case For Add New Owner User")
    @Test
    void asynchronousCaseForAddNewOwnerUser() {
        String newOwnerUsername = "newOwnerUsername";
        String newOwnerPassword = "newOwnerPassword";

        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        Runnable task = () -> {
            try {
                userService.addNewOwnerUser(newOwnerUsername, newOwnerPassword, newOwnerPassword);
            } catch (RuntimeException e) {
                exceptions.add(e);
            }
        };

        final int threadCount = 5;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(task);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            }
        }

        assertTrue(userService.usernameExists(newOwnerUsername),
                "Username should exist after one of the threads successfully added it");

        assertEquals(threadCount - 1, exceptions.size(),
                "Should throw an exception for existing username in " + (threadCount - 1) + " threads");



    }
}
