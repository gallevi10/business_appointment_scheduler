package com.javaworkshop.business_scheduler.dto;

import com.javaworkshop.business_scheduler.model.Appointment;
import com.javaworkshop.business_scheduler.model.Customer;
import com.javaworkshop.business_scheduler.model.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

// This class is used to represent an appointment in the dashboard view.
public class DashboardAppointment {

    private UUID appointmentId;

    private Customer customer;

    private Service service;

    private LocalDate date;

    private LocalTime time;

    private boolean isActive;

    public DashboardAppointment() {
    }

    public DashboardAppointment(Service service, Customer customer, LocalDate date, LocalTime time, boolean isActive) {
        this.service = service;
        this.customer = customer;
        this.date = date;
        this.time = time;
        this.isActive = isActive;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(UUID appointmentId) {
        this.appointmentId = appointmentId;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean active) {
        isActive = active;
    }

    // converts a list of Appointment objects to a list of DashboardAppointment objects.
    public static List<DashboardAppointment> fromAppointmentList(List<Appointment> customerAppointments) {

        List<DashboardAppointment> dashboardAppointments = new ArrayList<>();

        for (Appointment appointment : customerAppointments) {
            DashboardAppointment dashboardAppointment = new DashboardAppointment();
            dashboardAppointment.setAppointmentId(appointment.getId());
            dashboardAppointment.setCustomer(appointment.getCustomer());
            dashboardAppointment.setService(appointment.getService());
            LocalDateTime appointmentDateTime = appointment.getStartTime();
            dashboardAppointment.setDate(appointmentDateTime.toLocalDate());
            dashboardAppointment.setTime(appointmentDateTime.toLocalTime());
            dashboardAppointment.setIsActive(!appointment.getIsCompleted());
            dashboardAppointments.add(dashboardAppointment);
        }

        return dashboardAppointments;

    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DashboardAppointment other = (DashboardAppointment) o;
        return isActive == other.isActive &&
                Objects.equals(appointmentId, other.appointmentId) &&
                Objects.equals(customer, other.customer) &&
                Objects.equals(service, other.service) &&
                Objects.equals(date, other.date) &&
                Objects.equals(time, other.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appointmentId, customer, service, date, time, isActive);
    }

    @Override
    public String toString() {
        return "DashboardAppointment{" +
            "appointmentId=" + appointmentId +
            ", customer=" + customer +
            ", service=" + service +
            ", date=" + date +
            ", time=" + time +
            ", isActive=" + isActive +
            '}';
    }
}
