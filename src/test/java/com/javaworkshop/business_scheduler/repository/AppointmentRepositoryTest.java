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

    @DisplayName("Find All Appointments Ordered by Start Time")
    @Test
    void findAllAppointmentsOrderedByStartTime() {

        List<Appointment> expected = new ArrayList<>(List.of(thirdAppointment, firstAppointment, secondAppointment));

        List<Appointment> actual = appointmentRepository.findByOrderByStartTime();

        assertIterableEquals(expected, actual, "Expected appointments to be ordered by start time");
    }


    @DisplayName("Find All Uncompleted Appointments Ordered by Start Time")
    @Test
    void FindAllUncompletedAppointmentsOrderedByStartTime() {
        thirdAppointment.setIsCompleted(true);
        appointmentRepository.save(thirdAppointment);

        List<Appointment> expected = new ArrayList<>(List.of(firstAppointment, secondAppointment));

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

    @DisplayName("Find All Uncompleted Appointments by Customer id Ordered by Start Time")
    @Test
    void findAllUncompletedAppointmentsByCustomerIdOrderedByStartTime() {

        thirdAppointment.setIsCompleted(true);
        appointmentRepository.save(thirdAppointment);

        List<Appointment> expected = new ArrayList<>(List.of(firstAppointment, secondAppointment));

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

    @DisplayName("Find Uncompleted Appointments That End Before a Specific Time")
    @Test
    void findUncompletedAppointmentsThatEndBeforeASpecificTime() {
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

    @DisplayName("Find Appointments That Start Between Two Ranges of Time and Are Not Completed")
    @Test
    void findAppointmentsThatStartBetweenTwoRangesOfTimeAndAreNotCompleted() {
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

    @DisplayName("Overlapping Appointments")
    @Test
    void overlappingAppointments() {

        // creates overlapping times for the first appointment
        // first case
        LocalDateTime firstCaseOverlappingStart = firstAppointment.getStartTime().plusMinutes(1);
        LocalDateTime firstCaseOverlappingEnd = firstAppointment.getEndTime().plusMinutes(1);

        // second case
        LocalDateTime secondCaseOverlappingStart = firstAppointment.getStartTime().plusMinutes(1);
        LocalDateTime secondCaseOverlappingEnd = firstAppointment.getEndTime().minusMinutes(1);

        // third case
        LocalDateTime thirdCaseOverlappingStart = firstAppointment.getStartTime().minusMinutes(1);
        LocalDateTime thirdOverlappingEnd = firstAppointment.getEndTime().minusMinutes(1);

        // fourth case
        LocalDateTime forthCaseOverlappingStart = firstAppointment.getStartTime().minusMinutes(1);
        LocalDateTime forthCaseOverlappingEnd = firstAppointment.getEndTime().plusMinutes(1);

        List<Boolean> allOverlappingCases = List.of(
            appointmentRepository.isOverlapping(firstCaseOverlappingStart, firstCaseOverlappingEnd),
            appointmentRepository.isOverlapping(secondCaseOverlappingStart, secondCaseOverlappingEnd),
            appointmentRepository.isOverlapping(thirdCaseOverlappingStart, thirdOverlappingEnd),
            appointmentRepository.isOverlapping(forthCaseOverlappingStart, forthCaseOverlappingEnd)
        );

        for (boolean isOverlapping : allOverlappingCases) {
            assertTrue(isOverlapping, "Expected overlapping appointment to be found");
        }
    }

    @DisplayName("Non-Overlapping Appointments")
    @Test
    void nonOverlappingAppointments() {

        // creates non-overlapping times
        // first case
        LocalDateTime firstCaseNonOverlappingStart = firstAppointment.getStartTime().minusMinutes(10);
        LocalDateTime firstCaseNonOverlappingEnd = firstAppointment.getStartTime();

        // second case
        LocalDateTime secondCaseNonOverlappingStart = firstAppointment.getEndTime();
        LocalDateTime secondCaseNonOverlappingEnd = firstAppointment.getEndTime().plusMinutes(10);

        // third case
        LocalDateTime thirdCaseNonOverlappingStart = firstAppointment.getStartTime().plusDays(1);
        LocalDateTime thirdCaseNonOverlappingEnd = firstAppointment.getEndTime().plusDays(1);

        List<Boolean> allNonOverlappingCases = List.of(
            appointmentRepository.isOverlapping(firstCaseNonOverlappingStart, firstCaseNonOverlappingEnd),
            appointmentRepository.isOverlapping(secondCaseNonOverlappingStart, secondCaseNonOverlappingEnd),
            appointmentRepository.isOverlapping(thirdCaseNonOverlappingStart, thirdCaseNonOverlappingEnd)
        );

        for (boolean isOverlapping : allNonOverlappingCases) {
            assertFalse(isOverlapping, "Expected no overlapping appointment to be found");
        }
    }
}