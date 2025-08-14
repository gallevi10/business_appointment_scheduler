package com.javaworkshop.business_scheduler.dto;

import com.javaworkshop.business_scheduler.model.Customer;
import jakarta.validation.constraints.*;

import java.util.Objects;

// This class represents a form for capturing customer details.
public class CustomerDetailsForm {

    @NotNull(message = "Phone number is required")
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @NotNull(message = "Email is required")
    @NotBlank(message = "Email is required")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    @Email(message = "Email should be valid")
    private String email;

    @NotNull(message = "First name is required")
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @NotNull(message = "Last name is required")
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    public CustomerDetailsForm() {
    }

    public CustomerDetailsForm(Customer customer) {
        this.phoneNumber = customer.getPhone();
        this.email = customer.getEmail();
        this.firstName = customer.getFirstName();
        this.lastName = customer.getLastName();
    }

    public CustomerDetailsForm(String phoneNumber, String email, String firstName, String lastName) {
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CustomerDetailsForm other = (CustomerDetailsForm) o;
        return Objects.equals(phoneNumber, other.phoneNumber) &&
            Objects.equals(email, other.email) &&
            Objects.equals(firstName, other.firstName) &&
            Objects.equals(lastName, other.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber, email, firstName, lastName);
    }

    @Override
    public String toString() {
        return "CustomerDetailsForm{" +
            "phoneNumber='" + phoneNumber + '\'' +
            ", email='" + email + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            '}';
    }
}
