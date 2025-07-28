package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.Appointment;
import com.javaworkshop.business_scheduler.model.BusinessHour;
import com.javaworkshop.business_scheduler.model.Service;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

// This interface defines the contract for appointment-related operations in the business scheduler application.
public interface AppointmentService {

    List<Appointment> findAll();

    Appointment findById(UUID id);

    Appointment save(Appointment appointment);

    void deleteById(UUID id);

    List<Appointment> findActiveAppointmentsByCustomerId(UUID customerId);

    boolean isSlotAvailable(LocalDateTime start, LocalDateTime end);

    List<LocalTime> getAvailableSlots(Service service, LocalDate selectedDate, List<BusinessHour> businessHours);

    List<Appointment> findAllNotMarkedAsCompletedExpiredAppointments();

    void exportAppointmentsToXML(OutputStream outputStream, boolean activeAppointmentsOnly) throws XMLStreamException;

    void sendAppointmentConfirmationEmail(Appointment appointment, boolean isRescheduled);

    void markExpiredAppointmentsAsCompleted();

    void sendDailyReminders();
}
