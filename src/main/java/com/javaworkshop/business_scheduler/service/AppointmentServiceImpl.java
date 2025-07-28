package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.Appointment;
import com.javaworkshop.business_scheduler.model.BusinessHour;
import com.javaworkshop.business_scheduler.model.Service;
import com.javaworkshop.business_scheduler.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@org.springframework.stereotype.Service
public class AppointmentServiceImpl implements AppointmentService{

    @Value("${spring.mail.username}")
    private String EMAIL_FROM;

    private final int MINUTE = 60000;
    private AppointmentRepository appointmentRepository;
    private JavaMailSender javaMailSender;

    @Autowired
    public AppointmentServiceImpl(AppointmentRepository appointmentRepository, JavaMailSender javaMailSender) {
        this.appointmentRepository = appointmentRepository;
        this.javaMailSender = javaMailSender;
    }

    @Override
    public List<Appointment> findAll() {
        return appointmentRepository.findByOrderByStartTime();
    }

    @Override
    public Appointment findById(UUID id) throws RuntimeException {
        Optional<Appointment> appointment = appointmentRepository.findById(id);
        return appointment.orElse(null);
    }

    @Override
    public Appointment save(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    @Scheduled(cron = "0 0 7 * * *") // every day at 7:00 AM
    @Override
    public void sendDailyReminders() {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

            List<Appointment> todayAppointments =
                    appointmentRepository.findAppointmentsByStartTimeBetweenAndIsCompletedFalse(startOfDay, endOfDay);

            for (Appointment appointment : todayAppointments) {
                if (appointment.getCustomer() != null && appointment.getCustomer().getEmail() != null) {
                    String toEmail = appointment.getCustomer().getEmail();
                    String subject = "Reminder – Your Appointment is Today";
                    String body = "Hello " + appointment.getCustomer().getFirstName() + ",\n\n" +
                            "This is a friendly reminder that you have an appointment today:\n" +
                            "Time: " + appointment.getStartTime().toLocalTime() + "\n" +
                            "Service: " + appointment.getService().getServiceName() + "\n\n" +
                            "We look forward to seeing you!";

                    sendMail(toEmail, subject, body);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to send daily reminders: " + e.getMessage());
        }
    }

    @Override
    public void deleteById(UUID id) {
        appointmentRepository.deleteById(id);
    }

    @Override
    public List<Appointment> findActiveAppointmentsByCustomerId(UUID customerId) {
        Optional<List<Appointment>> appointments =
                appointmentRepository.findAppointmentsByCustomerIdAndIsCompletedFalseOrderByStartTime(customerId);

        if (appointments.isPresent()) {
            return appointments.get();
        }

        return new ArrayList<>();
    }

    @Override
    public List<LocalTime> getAvailableSlots(Service service,
                                             LocalDate selectedDate,
                                             List<BusinessHour> selectedBusinessHours) {

        List<LocalTime> availableSlots = new ArrayList<>();

        // if the selected date is not within the next month return empty list
        if (selectedDate.isBefore(LocalDate.now()) || selectedDate.isAfter(LocalDate.now().plusMonths(1))) {
            return availableSlots;
        }

        // appointment duration for the given service
        int slotDuration = service.getDuration();

        for (BusinessHour selectedBusinessHour : selectedBusinessHours) {
            // skips business hours that are not open
            if (!selectedBusinessHour.getIsOpen()) {
                continue;
            }
            for (LocalTime time = selectedBusinessHour.getStartTime();
                 time.plusMinutes(slotDuration-1).isBefore(selectedBusinessHour.getEndTime());
                 time = time.plusMinutes(slotDuration)) {
                if (selectedDate.equals(LocalDate.now()) && time.isBefore(LocalTime.now())) {
                    // skips past times if the selected date is today
                    continue;
                }
                LocalDateTime start = LocalDateTime.of(selectedDate, time);
                LocalDateTime end = start.plusMinutes(slotDuration);
                if (isSlotAvailable(start, end)) {
                    availableSlots.add(time);
                }
            }
        }

        return availableSlots;
    }

    @Override
    public List<Appointment> findAllNotMarkedAsCompletedExpiredAppointments() {
        return appointmentRepository.findByEndTimeBeforeAndIsCompletedFalse(LocalDateTime.now());
    }

    @Override
    public void exportAppointmentsToXML(OutputStream outputStream, boolean activeAppointmentsOnly) throws XMLStreamException {
        List<Appointment> appointments = activeAppointmentsOnly ? findAllActiveAppointments() : findAll();

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = factory.createXMLStreamWriter(outputStream, "UTF-8");

        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement("appointments");

        for (Appointment appointment : appointments) {
            writer.writeStartElement("appointment");

            writer.writeStartElement("id");
            writer.writeCharacters(appointment.getId().toString());
            writer.writeEndElement(); // </id>

            writer.writeStartElement("customer");
            writer.writeCharacters(appointment.getCustomer().getFirstName() + " " + appointment.getCustomer().getLastName());
            writer.writeEndElement(); // </customer>

            writer.writeStartElement("service");
            writer.writeCharacters(appointment.getService().getServiceName());
            writer.writeEndElement(); // </service>

            writer.writeStartElement("start");
            writer.writeCharacters(appointment.getStartTime().toString());
            writer.writeEndElement();  // </start>

            writer.writeStartElement("end");
            writer.writeCharacters(appointment.getEndTime().toString());
            writer.writeEndElement(); // </end>

            writer.writeEndElement(); // </appointment>
        }

        writer.writeEndElement(); // </appointments>
        writer.writeEndDocument();
        writer.flush();
        writer.close();
    }


    // this method is scheduled to run every minute to mark expired appointments as completed
    @Scheduled(fixedDelay = MINUTE) // runs every minute
    @Override
    public void markExpiredAppointmentsAsCompleted() {
        try {
            List<Appointment> expiredAppointments = findAllNotMarkedAsCompletedExpiredAppointments();
            for (Appointment appointment : expiredAppointments) {
                appointment.setIsCompleted(true);
            }
            appointmentRepository.saveAll(expiredAppointments);
        } catch (Exception e) {
            System.err.println("Error marking expired appointments as completed: " + e.getMessage());
        }
    }

    @Override
    public boolean isSlotAvailable(LocalDateTime startTime, LocalDateTime endTime) {
        return !appointmentRepository.isOverlapping(startTime, endTime);
    }

    public void sendAppointmentConfirmationEmail(Appointment appointment, boolean isRescheduled) {
        try {
            String toEmail = appointment.getCustomer().getEmail();
            String subject = "Appointment " + (isRescheduled ? "Updated" : "Confirmation") + " – "
                    + appointment.getService().getServiceName();
            String body = "Hello " + appointment.getCustomer().getFirstName() + ",\n\n" +
                    "Your appointment has been scheduled for:\n" +
                    "Date: " + appointment.getStartTime().toLocalDate() + "\n" +
                    "Time: " + appointment.getStartTime().toLocalTime() + "\n" +
                    "Service: " + appointment.getService().getServiceName() + "\n\n" +
                    "Thank you for choosing our business!";

            sendMail(toEmail, subject, body);

        } catch (Exception e) {
            System.err.println("Failed to send confirmation email: " + e.getMessage());
        }
    }

    private List<Appointment> findAllActiveAppointments() {
        return appointmentRepository.findAppointmentsByIsCompletedFalseOrderByStartTime();
    }

    private void sendMail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(EMAIL_FROM);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        javaMailSender.send(message);
    }
}
