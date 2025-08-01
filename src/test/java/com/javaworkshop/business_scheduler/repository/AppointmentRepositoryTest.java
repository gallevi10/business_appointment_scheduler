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
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        // initializes the database with two appointments
        customer = new Customer(null, "David", "Cohen",
                "david@someserver.com", "0541111111");
        service = new Service("Service", BigDecimal.valueOf(50),
                30, null, true);
        appointment = new Appointment(customer, service,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(service.getDuration()), false);
        customerRepository.save(customer);
        serviceRepository.save(service);
        appointmentRepository.save(appointment);
    }

    @AfterEach
    void tearDown() {
        // clears the database after each test
        appointmentRepository.deleteAll();
        customerRepository.deleteAll();
        serviceRepository.deleteAll();
    }

    @DisplayName("Test Overlapping Appointments")
    @Test
    void testOverlappingAppointments() {

        // creates an overlapping appointment time range
        LocalDateTime overlappingStart = LocalDateTime.now().plusMinutes(1);
        LocalDateTime overlappingEnd = overlappingStart.plusMinutes(service.getDuration());

        // creates edge case for overlapping appointment
        LocalDateTime edgeCaseOverlappingStart = LocalDateTime.now().plusMinutes(service.getDuration() - 1);
        LocalDateTime edgeCaseOverlappingEnd = edgeCaseOverlappingStart.plusMinutes(service.getDuration());

        boolean isOverlapping = appointmentRepository.isOverlapping(overlappingStart, overlappingEnd);
        boolean isEdgeCaseOverlapping = appointmentRepository.isOverlapping(edgeCaseOverlappingStart, edgeCaseOverlappingEnd);

        assertAll(
            // checks if the overlapping appointment is found
            () -> assertTrue(isOverlapping, "Expected overlapping appointment to be found"),
            // checks if the edge case overlapping appointment is found
            () -> assertTrue(isEdgeCaseOverlapping, "Expected edge case overlapping appointment to be found")

        );
    }

    @DisplayName("Test Non-Overlapping Appointments")
    @Test
    void testNonOverlappingAppointments() {

        // creates non-overlapping appointment time range
        LocalDateTime nonOverlappingStart = LocalDateTime.now().plusMinutes(service.getDuration() + 1);
        LocalDateTime nonOverlappingEnd = nonOverlappingStart.plusMinutes(service.getDuration());

        // creates edge case for non-overlapping appointment
        LocalDateTime edgeCaseNonOverlappingStart = LocalDateTime.now().plusMinutes(service.getDuration());
        LocalDateTime edgeCaseNonOverlappingEnd = nonOverlappingStart.plusMinutes(service.getDuration());

        boolean isOverlapping =
                appointmentRepository.isOverlapping(nonOverlappingStart, nonOverlappingEnd);
        boolean isEdgeCaseOverlapping =
                appointmentRepository.isOverlapping(edgeCaseNonOverlappingStart, edgeCaseNonOverlappingEnd);

        assertAll(
            // checks if the non-overlapping appointment is not found
            () -> assertFalse(isOverlapping, "Expected non-overlapping appointment to not be found"),
            // checks if the edge case non-overlapping appointment is not found
            () -> assertFalse(isEdgeCaseOverlapping, "Expected edge case non-overlapping appointment to not be found")
        );
    }

    @DisplayName("Test Find All Appointments Ordered by Start Time")
    @Test
    void testFindByOrderByStartTime() {
        Appointment secondAppointment = new Appointment(customer, service,
                LocalDateTime.now().plusMinutes(service.getDuration() + 10),
                LocalDateTime.now().plusMinutes(2L * service.getDuration() + 10), false);
        Appointment thirdAppointment = new Appointment(customer, service,
                LocalDateTime.now().minusMinutes(service.getDuration() + 10),
                LocalDateTime.now().minusMinutes(10), false);

        appointmentRepository.save(secondAppointment);
        appointmentRepository.save(thirdAppointment);

        List<Appointment> expected = new ArrayList<>(List.of(appointment, thirdAppointment, secondAppointment));
        expected.sort(Comparator.comparing(Appointment::getStartTime));

        List<Appointment> actual = appointmentRepository.findByOrderByStartTime();

        assertIterableEquals(expected, actual, "Expected appointments to be ordered by start time");
    }
}