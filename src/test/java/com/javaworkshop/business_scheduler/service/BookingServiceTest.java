package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.Appointment;
import com.javaworkshop.business_scheduler.model.Customer;
import com.javaworkshop.business_scheduler.model.Service;
import com.javaworkshop.business_scheduler.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class BookingServiceTest {

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private AppointmentService appointmentService;

    @Autowired
    private BookingService bookingService;

    private Customer customer;

    private Service service;

    @BeforeEach
    void setUp() {
        User user = new User(UUID.randomUUID(), "user",
            "123456789", "ROLE_CUSTOMER", true);
        customer = new Customer(UUID.randomUUID(), user, "Customer",
            "Customer", "email@someserver.com", "05411111111");
        service = new Service(UUID.randomUUID(), "Service",
            BigDecimal.valueOf(50), 100, null, true);
    }

    @DisplayName("Exception when booking an appointment with invalid start time")
    @Test
    void exceptionWhenBookingAnAppointmentWithInvalidStartTime() {
        LocalDateTime firstWrongStartTime = LocalDateTime.now().minusDays(1);
        LocalDateTime firstEndTime = firstWrongStartTime.plusMinutes(service.getDuration());
        LocalDateTime secondWrongStartTime = LocalDateTime.now().plusMonths(2);
        LocalDateTime secondEndTime = secondWrongStartTime.plusMinutes(service.getDuration());

        Exception firstException = assertThrows(RuntimeException.class, () ->
            bookingService.bookAppointment(customer.getFirstName(), customer.getLastName(),
                customer.getEmail(), customer.getPhone(), null, service, null,
                firstWrongStartTime, firstEndTime)
        );

        Exception secondException = assertThrows(RuntimeException.class, () ->
            bookingService.bookAppointment(customer.getFirstName(), customer.getLastName(),
                customer.getEmail(), customer.getPhone(), null, service, null,
                secondWrongStartTime, secondEndTime)
        );

        assertAll(
            () -> assertEquals("error.appointmentTime.invalid", firstException.getMessage()),
            () -> assertEquals("error.appointmentTime.invalid", secondException.getMessage())
        );

    }

    @DisplayName("Exception when booking an appointment with Overlapping time")
    @Test
    void exceptionWhenBookingAnAppointmentWithOverlappingTime() {
        LocalDateTime startTime = LocalDateTime
            .now()
            .plusDays(1)
            .withHour(9)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        LocalDateTime endTime = startTime.plusMinutes(service.getDuration());

        when(appointmentService.isSlotAvailable(startTime, endTime)).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () ->
            bookingService.bookAppointment(customer.getFirstName(), customer.getLastName(),
                customer.getEmail(), customer.getPhone(), null,
                service, null, startTime, endTime)
        );

        assertEquals("error.appointmentTime.taken", exception.getMessage());

        verify(appointmentService).isSlotAvailable(startTime, endTime);

    }

    @DisplayName("Successfully Booking An Appointment As Existing Customer By Username")
    @Test
    void successfullyBookingAnAppointmentAsExistingCustomerByUsername() {
        LocalDateTime startTime = LocalDateTime
            .now()
            .plusDays(1)
            .withHour(9)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        LocalDateTime endTime = startTime.plusMinutes(service.getDuration());

        when(appointmentService.isSlotAvailable(startTime, endTime)).thenReturn(true);
        when(customerService.findByUsername(customer.getUser().getUsername()))
            .thenReturn(customer);
        when(appointmentService.save(any(Appointment.class)))
            .thenAnswer(inv -> inv.getArguments()[0]);

        // new appointment case
        assertDoesNotThrow(() -> bookingService.bookAppointment(
            customer.getFirstName(), customer.getLastName(),
            customer.getEmail(), customer.getPhone(),
            customer.getUser().getUsername(), service,
            null, startTime, endTime
        ));

        // updating existing appointment case
        Appointment existingAppointment = new Appointment(
            UUID.randomUUID(), customer, service, startTime, endTime, false
        );
        when(appointmentService.findById(existingAppointment.getId()))
            .thenReturn(existingAppointment);

        assertDoesNotThrow(() -> bookingService.bookAppointment(
            customer.getFirstName(), customer.getLastName(),
            customer.getEmail(), customer.getPhone(),
            customer.getUser().getUsername(), service,
            existingAppointment.getId(), startTime, endTime
        ));

        verify(appointmentService, times(2)).isSlotAvailable(startTime, endTime);
        verify(customerService, times(2)).findByUsername(customer.getUser().getUsername());
        verify(appointmentService, times(2)).save(any(Appointment.class));
        verify(appointmentService).findById(existingAppointment.getId());
    }

    @DisplayName("Successfully Booking An Appointment As Existing Customer By Email and Phone")
    @Test
    void successfullyBookingAnAppointmentAsExistingCustomerByEmailAndPhone() {
        LocalDateTime startTime = LocalDateTime
            .now()
            .plusDays(1)
            .withHour(9)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        LocalDateTime endTime = startTime.plusMinutes(service.getDuration());

        when(appointmentService.isSlotAvailable(startTime, endTime)).thenReturn(true);
        when(customerService.findByEmailAndPhone(customer.getEmail(), customer.getPhone()))
            .thenReturn(customer);
        when(customerService.getValidCustomer(customer, customer.getEmail(), customer.getPhone(),
            customer.getFirstName(), customer.getLastName(), null))
            .thenReturn(customer);
        when(appointmentService.save(any(Appointment.class)))
            .thenAnswer(inv -> inv.getArguments()[0]);

        // new appointment case
        assertDoesNotThrow(() -> bookingService.bookAppointment(
            customer.getFirstName(), customer.getLastName(),
            customer.getEmail(), customer.getPhone(),
            null, service,
            null, startTime, endTime
        ));

        // updating existing appointment case
        Appointment existingAppointment = new Appointment(
            UUID.randomUUID(), customer, service, startTime, endTime, false
        );
        when(appointmentService.findById(existingAppointment.getId()))
            .thenReturn(existingAppointment);

        assertDoesNotThrow(() -> bookingService.bookAppointment(
            customer.getFirstName(), customer.getLastName(),
            customer.getEmail(), customer.getPhone(),
            null, service,
            existingAppointment.getId(), startTime, endTime
        ));

        verify(appointmentService, times(2))
            .isSlotAvailable(startTime, endTime);
        verify(customerService, times(2))
            .findByEmailAndPhone(customer.getEmail(), customer.getPhone());
        verify(customerService, times(2)).getValidCustomer(
            customer, customer.getEmail(), customer.getPhone(),
            customer.getFirstName(), customer.getLastName(), null
        );
        verify(appointmentService, times(2)).save(any(Appointment.class));
        verify(appointmentService).findById(existingAppointment.getId());
    }

    @DisplayName("Successfully Booking An Appointment As a New Customer")
    @Test
    void successfullyBookingAnAppointmentAsANewCustomer() {
        LocalDateTime startTime = LocalDateTime
            .now()
            .plusDays(1)
            .withHour(9)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        LocalDateTime endTime = startTime.plusMinutes(service.getDuration());

        when(appointmentService.isSlotAvailable(startTime, endTime)).thenReturn(true);
        when(customerService.findByEmailAndPhone(customer.getEmail(), customer.getPhone()))
            .thenReturn(null);
        when(customerService.getValidCustomer(null, customer.getEmail(), customer.getPhone(),
            customer.getFirstName(), customer.getLastName(), null))
            .thenReturn(customer);
        when(customerService.save(customer)).thenReturn(customer);
        when(appointmentService.save(any(Appointment.class)))
            .thenAnswer(inv -> inv.getArguments()[0]);

        assertDoesNotThrow(() -> bookingService.bookAppointment(
            customer.getFirstName(), customer.getLastName(),
            customer.getEmail(), customer.getPhone(),
            null, service,
            null, startTime, endTime
        ));

        verify(appointmentService).isSlotAvailable(startTime, endTime);
        verify(customerService).findByEmailAndPhone(customer.getEmail(), customer.getPhone());
        verify(customerService).getValidCustomer(
            null, customer.getEmail(), customer.getPhone(),
            customer.getFirstName(), customer.getLastName(), null
        );
        verify(customerService).save(customer);
        verify(appointmentService).save(any(Appointment.class));
    }


}