package com.javaworkshop.business_scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

// This class represents a form for capturing user details.
public class UserDetailsForm {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    @Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters")
    private String confirmPassword;

    public UserDetailsForm() {
    }

    public UserDetailsForm(String username, String password, String confirmPassword) {
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsForm other = (UserDetailsForm) o;
        return Objects.equals(username, other.username) &&
            Objects.equals(password, other.password) &&
            Objects.equals(confirmPassword, other.confirmPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, confirmPassword);
    }

    @Override
    public String toString() {
        return "UserDetailsForm{" +
            "username='" + username + '\'' +
            ", password='" + password + '\'' +
            ", confirmPassword='" + confirmPassword + '\'' +
            '}';
    }
}
