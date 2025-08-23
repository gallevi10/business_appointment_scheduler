package com.javaworkshop.business_scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

// This class represents a form for changing a user's password.
public class ChangePasswordForm {

    @NotBlank(message = "Old password is required")
    @Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters")
    private String newPassword;

    @NotBlank(message = "Confirmation password is required")
    @Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters")
    private String confirmNewPassword;

    public ChangePasswordForm() {
    }

    public ChangePasswordForm(String oldPassword, String newPassword, String confirmNewPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.confirmNewPassword = confirmNewPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChangePasswordForm other = (ChangePasswordForm) o;
        return Objects.equals(oldPassword, other.oldPassword) &&
            Objects.equals(newPassword, other.newPassword) &&
            Objects.equals(confirmNewPassword, other.confirmNewPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldPassword, newPassword, confirmNewPassword);
    }

    @Override
    public String toString() {
        return "ChangePasswordForm{" +
            "oldPassword='" + oldPassword + '\'' +
            ", newPassword='" + newPassword + '\'' +
            ", confirmNewPassword='" + confirmNewPassword + '\'' +
            '}';
    }
}
