package com.javaworkshop.business_scheduler.dto;

import jakarta.validation.Valid;

import java.util.Objects;

// This class represents a form for registration.
public class RegistrationForm {

    @Valid
    private CustomerDetailsForm customerDetailsForm;

    @Valid
    private UserDetailsForm userDetailsForm;


    public RegistrationForm() {
    }

    public RegistrationForm(CustomerDetailsForm customerDetailsForm, UserDetailsForm userDetailsForm) {
        this.customerDetailsForm = customerDetailsForm;
        this.userDetailsForm = userDetailsForm;
    }

    public CustomerDetailsForm getCustomerDetailsForm() {
        return customerDetailsForm;
    }

    public void setCustomerDetailsForm(CustomerDetailsForm customerDetailsForm) {
        this.customerDetailsForm = customerDetailsForm;
    }

    public UserDetailsForm getUserDetailsForm() {
        return userDetailsForm;
    }

    public void setUserDetailsForm(UserDetailsForm userDetailsForm) {
        this.userDetailsForm = userDetailsForm;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RegistrationForm other = (RegistrationForm) o;
        return Objects.equals(customerDetailsForm, other.customerDetailsForm) &&
            Objects.equals(userDetailsForm, other.userDetailsForm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerDetailsForm, userDetailsForm);
    }

    @Override
    public String toString() {
        return "RegistrationForm{" +
            "customerDetailsForm=" + customerDetailsForm +
            ", userDetailsForm=" + userDetailsForm +
            '}';
    }
}
