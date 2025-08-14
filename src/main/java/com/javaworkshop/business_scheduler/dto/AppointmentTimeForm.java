package com.javaworkshop.business_scheduler.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

// This class represents a form for capturing appointment date and time.
public class AppointmentTimeForm {

    @NotNull(message = "Appointment date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate appointmentDate;

    @NotNull(message = "Appointment time is required")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime appointmentTime;

    public AppointmentTimeForm() {
    }

    public AppointmentTimeForm(LocalDate appointmentDate, LocalTime appointmentTime) {
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AppointmentTimeForm other = (AppointmentTimeForm) o;
        return Objects.equals(appointmentDate, other.appointmentDate) &&
            Objects.equals(appointmentTime, other.appointmentTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appointmentDate, appointmentTime);
    }

    @Override
    public String toString() {
        return "AppointmentTimeForm{" +
            "appointmentDate=" + appointmentDate +
            ", appointmentTime=" + appointmentTime +
            '}';
    }
}
