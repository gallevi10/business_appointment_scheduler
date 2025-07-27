package com.javaworkshop.business_scheduler.dto;

import jakarta.validation.Valid;

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
}
