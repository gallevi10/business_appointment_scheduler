package com.javaworkshop.business_scheduler.dto;

import jakarta.validation.Valid;

// This class represents a form for booking an appointment.
public class BookAppointmentForm {

    @Valid
    private CustomerDetailsForm customerDetailsForm;

    @Valid
    private AppointmentTimeForm appointmentTimeForm;

    public BookAppointmentForm() {
    }

    public BookAppointmentForm(CustomerDetailsForm customerDetailsForm, AppointmentTimeForm appointmentTimeForm) {
        this.customerDetailsForm = customerDetailsForm;
        this.appointmentTimeForm = appointmentTimeForm;
    }

    public CustomerDetailsForm getCustomerDetailsForm() {
        return customerDetailsForm;
    }

    public void setCustomerDetailsForm(CustomerDetailsForm customerDetailsForm) {
        this.customerDetailsForm = customerDetailsForm;
    }

    public AppointmentTimeForm getAppointmentTimeForm() {
        return appointmentTimeForm;
    }

    public void setAppointmentTimeForm(AppointmentTimeForm appointmentTimeForm) {
        this.appointmentTimeForm = appointmentTimeForm;
    }
}
