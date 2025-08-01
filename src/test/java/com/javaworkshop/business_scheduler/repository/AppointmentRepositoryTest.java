package com.javaworkshop.business_scheduler.repository;

import com.javaworkshop.business_scheduler.model.Appointment;
import com.javaworkshop.business_scheduler.model.Customer;
import com.javaworkshop.business_scheduler.model.Service;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// This class tests the AppointmentRepository methods to ensure they work as expected.
@DataJpaTest
class AppointmentRepositoryTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    private Customer customer;
    private Service service;
    private Appointment firstAppointment, secondAppointment, thirdAppointment;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        // initializes the database with two appointments
        customer = new Customer(null, "David", "Cohen",
                "david@someserver.com", "0541111111");
        service = new Service("Service", BigDecimal.valueOf(50),
                30, null, true);
        now = LocalDateTime.now().withNano(0);
        firstAppointment = new Appointment(customer, service, now,
                now.plusMinutes(service.getDuration()), false);
        secondAppointment = new Appointment(customer, service,
                now.plusMinutes(service.getDuration() + 10),
                now.plusMinutes(2L * service.getDuration() + 10), false);
        thirdAppointment = new Appointment(customer, service,
                now.minusMinutes(service.getDuration() + 10),
                now.minusMinutes(10), false);
        customerRepository.save(customer);
        serviceRepository.save(service);
        appointmentRepository.saveAll(List.of(firstAppointment, secondAppointment, thirdAppointment));
    }

    @AfterEach
    void tearDown() {
        // clears the database after each test
        appointmentRepository.deleteAll();
        customerRepository.deleteAll();
        serviceRepository.deleteAll();
    }

    @DisplayName("Test Find All Appointments Ordered by Start Time")
    @Test
    void testFindByOrderByStartTime() {

        List<Appointment> expected = new ArrayList<>(List.of(firstAppointment, secondAppointment, thirdAppointment));
        expected.sort(Comparator.comparing(Appointment::getStartTime));

        List<Appointment> actual = appointmentRepository.findByOrderByStartTime();

        assertIterableEquals(expected, actual, "Expected appointments to be ordered by start time");
    }


    @DisplayName("Test Find All Uncompleted Appointments Ordered by Start Time")
    @Test
    void testFindAppointmentsByIsCompletedFalseOrderByStartTime() {
        thirdAppointment.setIsCompleted(true);
        appointmentRepository.save(thirdAppointment);

        List<Appointment> expected = new ArrayList<>(List.of(firstAppointment, secondAppointment));
        expected.sort(Comparator.comparing(Appointment::getStartTime));

        List<Appointment> actual = appointmentRepository.findAppointmentsByIsCompletedFalseOrderByStartTime();

        assertAll(
                () -> assertEquals(expected.size(), actual.size(),
                        "Expected the same number of uncompleted appointments as in the expected list"),
                () -> assertIterableEquals(expected, actual,
                        "Expected uncompleted appointments to be ordered by start time"),
                () -> assertFalse(actual.contains(thirdAppointment),
                        "Expected completed thirdAppointment to not be included in the result")
        );

    }

    @DisplayName("Test Find All Uncompleted Appointments by Customer ID Ordered by Start Time")
    @Test
    void testFindAppointmentsByCustomerIdAndIsCompletedFalseOrderByStartTime() {

        thirdAppointment.setIsCompleted(true);
        appointmentRepository.save(thirdAppointment);

        List<Appointment> expected = new ArrayList<>(List.of(firstAppointment, secondAppointment));
        expected.sort(Comparator.comparing(Appointment::getStartTime));

        List<Appointment> actual = appointmentRepository
                        .findAppointmentsByCustomerIdAndIsCompletedFalseOrderByStartTime(customer.getId())
                        .orElse(new ArrayList<>());

        assertAll(
                () -> assertEquals(expected.size(), actual.size(),
                        "Expected the same number of uncompleted appointments as in the expected list"),
                () -> assertIterableEquals(expected, actual,
                        "Expected uncompleted appointments to be ordered by start time"),
                () -> assertFalse(actual.contains(thirdAppointment),
                        "Expected completed thirdAppointment to not be included in the result")
        );

    }

    @DisplayName("Test Find Uncompleted Appointments That End Before a Specific Time")
    @Test
    void testFindByEndTimeBeforeAndIsCompletedFalse() {
        // creates a time that is after the third appointment end time
        LocalDateTime time = now;

        List<Appointment> expected = new ArrayList<>(List.of(thirdAppointment));

        List<Appointment> actual = appointmentRepository.findByEndTimeBeforeAndIsCompletedFalse(time);

        assertAll(
            () -> assertEquals(expected.size(), actual.size(),
                    "Expected the same number of appointments as in the expected list"),
            () -> assertIterableEquals(expected, actual,
                    "Expected appointments to be the same as in the expected list"),
            () -> assertFalse(actual.contains(firstAppointment) || actual.contains(secondAppointment),
                    "Expected firstAppointment and secondAppointment to not be included in the result")
        );
    }

    @DisplayName("Test Find Appointments that Start Between Two Ranges of Time and Are Not Completed")
    @Test
    void testFindAppointmentsByStartTimeBetweenAndIsCompletedFalse() {
        // creates a time range that includes the first and second appointments
        LocalDateTime start = now;
        LocalDateTime end = start.plusMinutes(2L * service.getDuration());

        List<Appointment> expected = new ArrayList<>(List.of(firstAppointment, secondAppointment));

        List<Appointment> actual =
                appointmentRepository.findAppointmentsByStartTimeBetweenAndIsCompletedFalse(start, end);

        assertAll(
                () -> assertEquals(expected.size(), actual.size(),
                        "Expected the same number of appointments as in the expected list"),
                () -> assertIterableEquals(expected, actual,
                        "Expected appointments to be the same as in the expected list"),
                () -> assertFalse(actual.contains(thirdAppointment),
                        "Expected thirdAppointment to not be included in the result")
        );
    }

    @DisplayName("Test Overlapping Appointments")
    @Test
    void testOverlappingAppointments() {
        // creates an overlapping firstAppointment time range
        LocalDateTime overlappingStart = now.plusMinutes(1);
        LocalDateTime overlappingEnd = overlappingStart.plusMinutes(service.getDuration());

        // creates edge case for overlapping firstAppointment
        LocalDateTime edgeCaseOverlappingStart = now.plusMinutes(service.getDuration() - 1);
        LocalDateTime edgeCaseOverlappingEnd = edgeCaseOverlappingStart.plusMinutes(service.getDuration());

        boolean isOverlapping = appointmentRepository.isOverlapping(overlappingStart, overlappingEnd);
        boolean isEdgeCaseOverlapping = appointmentRepository.isOverlapping(edgeCaseOverlappingStart, edgeCaseOverlappingEnd);

        assertAll(
            // checks if the overlapping firstAppointment is found
            () -> assertTrue(isOverlapping, "Expected overlapping firstAppointment to be found"),
            // checks if the edge case overlapping firstAppointment is found
            () -> assertTrue(isEdgeCaseOverlapping, "Expected edge case overlapping firstAppointment to be found")

        );
    }

    @DisplayName("Test Non-Overlapping Appointments")
    @Test
    void testNonOverlappingAppointments() {

        // creates non-overlapping firstAppointment time range
        LocalDateTime nonOverlappingStart = now.plusMinutes(5L * service.getDuration());
        LocalDateTime nonOverlappingEnd = nonOverlappingStart.plusMinutes(service.getDuration());

        // creates edge case for non-overlapping firstAppointment
        LocalDateTime edgeCaseNonOverlappingStart = now.plusMinutes(2L * service.getDuration() + 10);
        LocalDateTime edgeCaseNonOverlappingEnd = nonOverlappingStart.plusMinutes(service.getDuration());

        boolean isOverlapping =
                appointmentRepository.isOverlapping(nonOverlappingStart, nonOverlappingEnd);
        boolean isEdgeCaseOverlapping =
                appointmentRepository.isOverlapping(edgeCaseNonOverlappingStart, edgeCaseNonOverlappingEnd);

        assertAll(
            // checks if the non-overlapping firstAppointment is not found
            () -> assertFalse(isOverlapping, "Expected non-overlapping firstAppointment to not be found"),
            // checks if the edge case non-overlapping firstAppointment is not found
            () -> assertFalse(isEdgeCaseOverlapping, "Expected edge case non-overlapping firstAppointment to not be found")
        );
    }
}