package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.BusinessHour;
import com.javaworkshop.business_scheduler.model.BusinessInfo;
import com.javaworkshop.business_scheduler.model.Service;
import com.javaworkshop.business_scheduler.repository.BusinessHourRepository;
import com.javaworkshop.business_scheduler.repository.CustomerRepository;
import com.javaworkshop.business_scheduler.repository.ServiceRepository;
import com.javaworkshop.business_scheduler.repository.UserRepository;
import com.javaworkshop.business_scheduler.util.EmailUtil;
import com.javaworkshop.business_scheduler.util.ImageStorageUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class ServicesAsyncTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private BusinessInfoService businessInfoService;

    @Autowired
    private BusinessHourService businessHourService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BusinessHourRepository businessHourRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private EmailUtil emailUtil;

    @MockitoBean
    private ImageStorageUtils imageStorageUtils;

    private final int threadCount = 5;

    private Thread[] threads;

    private List<Throwable> exceptions;

    @BeforeEach
    void setUp() {
        exceptions = Collections.synchronizedList(new ArrayList<>());
        threads = new Thread[threadCount];
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        serviceRepository.deleteAll();
        businessHourRepository.deleteAll();
        customerRepository.deleteAll();
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
                serviceService.removeServiceImage(serviceToRemoveImage.getId());
                Path expectedPath = Paths.get("uploads/services/" + serviceToRemoveImage.getId());
                verify(imageStorageUtils).clearFolder(expectedPath);
            } catch (RuntimeException e) {
                exceptions.add(e);
            } catch (IOException ignored) {
            } // since we are mocking the IO operation, we can ignore this exception
        };

        runAsyncTask(task);

        assertNull(serviceRepository.findById(serviceToRemoveImage.getId())
            .get().getImagePath(), "Service image path should be null after removal");

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

    @DisplayName("Asynchronous Case For Remove Background Image")
    @Test
    void asynchronousCaseForRemoveBackgroundImage() {

        // getting the business info to remove its background image (initialized in DefaultInitializer)
        BusinessInfo businessInfo = businessInfoService.getBusinessInfo();

        // setting the background path to a non-null value to simulate an existing background image
        businessInfo.setBackgroundPath("uploads/business_background/background_image.jpg");
        businessInfoService.save(businessInfo);

        Runnable task = () -> {
            try {
                businessInfoService.removeBackgroundImage();
                Path expectedPath = Paths.get("uploads/business_background");
                verify(imageStorageUtils).clearFolder(expectedPath);
            } catch (RuntimeException e) {
                exceptions.add(e);
            } catch (IOException ignored) {
            } // since we are mocking the IO operation, we can ignore this exception
        };

        runAsyncTask(task);

        assertNull(businessInfoService.getBusinessInfo().getBackgroundPath(),
            "Background image should not exist");

        assertEquals(threadCount - 1, exceptions.size(),
            "Should throw an exception for null image path in " + (threadCount - 1) + " threads");
    }

    @DisplayName("Asynchronous Case For Update Business Info")
    @Test
    void asynchronousCaseForUpdateBusinessInfo() {

        String newBusinessName = "Updated Business Name";
        String newDescription = "Updated Business Description";

        Runnable task = () -> {
            try {
                // updates the business info initialized in DefaultInitializer
                businessInfoService.updateBusinessInfo(newBusinessName, newDescription,
                    null);
            } catch (RuntimeException e) {
                exceptions.add(e);
            }
        };

        runAsyncTask(task);

        assertEquals(0, exceptions.size(),
            "Should not throw any exceptions during the update process");
    }

    @DisplayName("Asynchronous Case For Add New Business Hour")
    @Test
    void asynchronousCaseForAddNewBusinessHour() {
        byte newDayOfWeek = 0; // sunday
        LocalTime newStartTime = LocalTime.of(18, 30);
        LocalTime newEndTime = LocalTime.of(20, 30);
        boolean newIsOpen = true;

        Runnable task = () -> {
            try {
                businessHourService.addOrUpdateBusinessHour(null, newDayOfWeek,
                    newStartTime, newEndTime, newIsOpen);
            } catch (RuntimeException e) {
                exceptions.add(e);
            }
        };

        runAsyncTask(task);

        assertEquals(threadCount - 1, exceptions.size(),
            "Should throw an exception for existing service name in " + (threadCount - 1) + " threads");

    }

    @DisplayName("Asynchronous Case For Update Business Hour")
    @Test
    void asynchronousCaseForUpdateBusinessHour() {

        BusinessHour existingBusinessHour = businessHourService.save(new BusinessHour(
            (byte) 0,
            LocalTime.of(18, 30),
            LocalTime.of(19, 30),
            true
        ));

        byte newDayOfWeek = 1;
        LocalTime newStartTime = LocalTime.of(18, 30);
        LocalTime newEndTime = LocalTime.of(20, 30);
        boolean newIsOpen = true;

        Runnable task = () -> {
            try {
                businessHourService.addOrUpdateBusinessHour(existingBusinessHour.getId(),
                    newDayOfWeek, newStartTime, newEndTime, newIsOpen);
            } catch (RuntimeException e) {
                exceptions.add(e);
            }
        };

        runAsyncTask(task);

        assertEquals(0, exceptions.size(),
            "Should not throw any exceptions during the update process");
    }

    @DisplayName("Asynchronous Case For Booking An Appointment")
    @Test
    void asynchronousCaseForBookingAppointment() {
        Service service = serviceService.save(new Service(
            "Service",
            BigDecimal.valueOf(50),
            30,
            null,
            true
        ));
        String firstName = "firstName";
        String lastName = "lastName";
        String email = "gallvi10@walla.com";
        String phone = "0541234567";
        LocalDateTime startTime = LocalDateTime
            .now()
            .plusDays(1)
            .withHour(9)
            .withMinute(0);
        LocalDateTime endTime = startTime.plusMinutes(service.getDuration());

        Runnable task = () -> {
            try {
                bookingService.bookAppointment(firstName, lastName, email, phone,
                    null, service, null, startTime, endTime);
            } catch (RuntimeException e) {
                exceptions.add(e);
            }
        };

        runAsyncTask(task);

        assertEquals(threadCount - 1, exceptions.size(),
            "Should throw an exception for Overlapping appointment in " + (threadCount - 1) + " threads");
    }

    private void runAsyncTask(Runnable task) {

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
