package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.Service;
import com.javaworkshop.business_scheduler.repository.ServiceRepository;
import com.javaworkshop.business_scheduler.repository.UserRepository;
import com.javaworkshop.business_scheduler.util.ImageStorageUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
public class ServicesAsyncTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private RegistrationService registrationService;

    private final int threadCount = 5;

    private List<Throwable> exceptions;

    @BeforeEach
    void setUp() {
        exceptions = Collections.synchronizedList(new ArrayList<>());
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        serviceRepository.deleteAll();
    }

    @DisplayName("Asynchronous Case For Add New Owner User")
    @Test
    void asynchronousCaseForAddNewOwnerUser() {
        String newOwnerUsername = "newOwnerUsername";
        String newOwnerPassword = "newOwnerPassword";

        Runnable task = () -> {
            try {
                userService.addNewOwnerUser(newOwnerUsername, newOwnerPassword, newOwnerPassword);
            } catch (RuntimeException e) {
                exceptions.add(e);
            }
        };

        runAsyncTask(task);

        assertTrue(userRepository.existsByUsername(newOwnerUsername),
                "Username should exist after one of the threads successfully added it");

        assertEquals(threadCount - 1, exceptions.size(),
                "Should throw an exception for existing username in " + (threadCount - 1) + " threads");

    }

    @DisplayName("Asynchronous Case For Add New Service")
    @Test
    void asynchronousCaseForAddNewService() {
        String newServiceName = "newServiceName";
        BigDecimal newServicePrice = BigDecimal.valueOf(100);
        int newServiceDuration = 30;

        Runnable task = () -> {
            try {
                serviceService.addOrUpdateService(null, newServiceName,
                        newServicePrice, newServiceDuration, null);
            } catch (RuntimeException e) {
                exceptions.add(e);
            }
        };

        runAsyncTask(task);

        assertTrue(serviceRepository.existsByServiceName(null, newServiceName),
                "Service name should exist after one of the threads successfully added it");

        assertEquals(threadCount - 1, exceptions.size(),
                "Should throw an exception for existing service name in " + (threadCount - 1) + " threads");

    }

    @DisplayName("Asynchronous Case For Remove Service Image")
    @Test
    void asynchronousCaseForRemoveServiceImage() {

        Service serviceToRemoveImage = new Service(
                "serviceToRemoveImage",
                BigDecimal.valueOf(50),
                15,
                "path/to/image.jpg",
                true
        );
        serviceRepository.save(serviceToRemoveImage);

        Runnable task = () -> {
            try {
                try (MockedStatic<ImageStorageUtils> mockedStatic = mockStatic(ImageStorageUtils.class)) {
                    serviceService.removeServiceImage(serviceToRemoveImage.getId());
                    Path expectedPath = Paths.get("uploads/services/" + serviceToRemoveImage.getId());
                    mockedStatic.verify(() -> ImageStorageUtils.clearFolder(expectedPath));
                } catch (IOException ignored) {} // since we are mocking the IO operation, we can ignore this exception
            } catch (RuntimeException e) {
                exceptions.add(e);
            }
        };

        runAsyncTask(task);

        assertEquals(threadCount - 1, exceptions.size(),
                "Should throw an exception for null image path in " + (threadCount - 1) + " threads");
    }

    @DisplayName("Asynchronous Case For Registering New User")
    @Test
    void asynchronousCaseForRegisteringNewUser() {
        String newUserUsername = "newUserUsername";
        String newUserPassword = "newUserPassword";
        String newUserEmail = "newUserEmail@someservice.com";
        String newUserPhone = "0541234567";
        String newUserFirstName = "New";
        String newUserLastName = "User";

        Runnable task = () -> {
            try {
                registrationService.registerNewCustomer(
                        newUserUsername, newUserPassword,
                        newUserPassword, newUserEmail,
                        newUserPhone, newUserFirstName, newUserLastName
                );
            } catch (RuntimeException e) {
                exceptions.add(e);
            }
        };

        runAsyncTask(task);

        assertEquals(threadCount - 1, exceptions.size(),
                "Should throw an exception for existing user in " + (threadCount - 1) + " threads");

    }

    private void runAsyncTask(Runnable task) {
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
    }
}
