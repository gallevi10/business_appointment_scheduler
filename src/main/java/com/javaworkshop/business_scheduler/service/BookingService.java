package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.Appointment;
import com.javaworkshop.business_scheduler.model.Customer;
import com.javaworkshop.business_scheduler.model.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

// This class handles the booking of appointments for customers.
@org.springframework.stereotype.Service
public class BookingService {

    private final CustomerService customerService;
    private final AppointmentService appointmentService;

    @Autowired
    public BookingService(CustomerService customerService, AppointmentService appointmentService) {
        this.customerService = customerService;
        this.appointmentService = appointmentService;
    }

    @Transactional // ensures that the booking operation is atomic
    public Appointment bookAppointment(String firstName, String lastName, String email, String phone,
                                       String username, Service service, UUID appointmentId,
                                       LocalDateTime startTime, LocalDateTime endTime) {

        // checks if the given start time is valid
        if (startTime.isBefore(LocalDateTime.now()) || startTime.isAfter(LocalDateTime.now().plusMonths(1))) {
            throw new RuntimeException("error.appointmentTime.invalid");
        }

        // checks if the slot is available for the given time range
        if (!appointmentService.isSlotAvailable(startTime, endTime)) {
            throw new RuntimeException("error.appointmentTime.taken");
        }

        Customer bookingCustomer;
        if (username != null) { // if the username is provided we are booking for an existing and valid customer
            bookingCustomer = customerService.findByUsername(username);
        }
        else {
            // if the customer already exists, we retrieve it by email and phone
            Customer existingCustomer = customerService.findByEmailAndPhone(email, phone);
            bookingCustomer = customerService.getValidCustomer(
                    existingCustomer, email, phone,
                    firstName, lastName, null
            );
        }

        Appointment appointmentToBook;
        if (appointmentId != null) { // if the appointmentId is provided, we are editing an existing appointment
            appointmentToBook = appointmentService.findById(appointmentId);
            appointmentToBook.setService(service);
            appointmentToBook.setStartTime(startTime);
            appointmentToBook.setEndTime(endTime);
            appointmentService.save(appointmentToBook);
            appointmentService.sendAppointmentConfirmationEmail(appointmentToBook, true);
        }
        else { // if the appointmentId is not provided, we are creating a new appointment
            appointmentToBook = new Appointment(bookingCustomer, service, startTime, endTime, false);
            customerService.save(bookingCustomer); // ensure the customer is saved before saving the appointment
            appointmentService.save(appointmentToBook);
            appointmentService.sendAppointmentConfirmationEmail(appointmentToBook, false);
        }

        return appointmentToBook;
    }

}
