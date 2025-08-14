package com.javaworkshop.business_scheduler.dto;

import jakarta.validation.Valid;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BookAppointmentForm other = (BookAppointmentForm) o;
        return Objects.equals(customerDetailsForm, other.customerDetailsForm) &&
            Objects.equals(appointmentTimeForm, other.appointmentTimeForm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerDetailsForm, appointmentTimeForm);
    }

    @Override
    public String toString() {
        return "BookAppointmentForm{" +
            "customerDetailsForm=" + customerDetailsForm +
            ", appointmentTimeForm=" + appointmentTimeForm +
            '}';
    }
}
