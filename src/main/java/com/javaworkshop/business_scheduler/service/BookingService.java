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

    private CustomerService customerService;
    private AppointmentService appointmentService;

    @Autowired
    public BookingService(CustomerService customerService, AppointmentService appointmentService) {
        this.customerService = customerService;
        this.appointmentService = appointmentService;
    }

    @Transactional // ensures that the booking operation is atomic
    public Appointment bookAppointment(Customer customer, Service service, UUID appointmentId,
                                LocalDateTime startTime, LocalDateTime endTime) {

        // checks if the given start time is valid
        if (startTime.isBefore(LocalDateTime.now()) || startTime.isAfter(LocalDateTime.now().plusMonths(1))) {
            throw new RuntimeException("error.appointmentTime.invalid");
        }

        // double check if the slot is available
        if (!appointmentService.isSlotAvailable(startTime, endTime)) {
            throw new RuntimeException("error.appointmentTime.taken");
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
            appointmentToBook = new Appointment(customer, service, startTime, endTime, false);
            customerService.save(customer); // ensure the customer is saved before saving the appointment
            appointmentService.save(appointmentToBook);
            appointmentService.sendAppointmentConfirmationEmail(appointmentToBook, false);
        }

        return appointmentToBook;
    }

}
