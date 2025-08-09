package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.Appointment;
import com.javaworkshop.business_scheduler.model.BusinessHour;
import com.javaworkshop.business_scheduler.model.Customer;
import com.javaworkshop.business_scheduler.model.Service;
import com.javaworkshop.business_scheduler.repository.AppointmentRepository;
import com.javaworkshop.business_scheduler.util.EmailUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("disable-scheduling")
class AppointmentServiceTest {

    @MockitoBean
    private AppointmentRepository appointmentRepository;

    @MockitoBean
    private EmailUtil emailUtil;

    @Autowired
    private AppointmentService appointmentService;

    private Appointment firstAppointment, secondAppointment, thirdAppointment;

    @BeforeEach
    void setUp() {
        Service service = new Service(UUID.randomUUID(), "Service",
            BigDecimal.valueOf(50), 45,
            null, true
        );
        Customer customer = new Customer(UUID.randomUUID(), null,
            "Customer", "Customer",
            "email@someserver.com", "05411111111"
        );
        LocalDateTime firstStartTime =
            LocalDateTime.of(2025, 8, 7, 9, 0, 0);
        LocalDateTime secondStartTime =
            LocalDateTime.of(2025, 8, 7, 11, 0, 0);
        LocalDateTime thirdStartTime =
            LocalDateTime.of(2025, 8, 7, 15, 0, 0);
        firstAppointment = new Appointment(UUID.randomUUID(), customer, service,
            firstStartTime,
            firstStartTime.plusMinutes(service.getDuration()), true
        );
        secondAppointment = new Appointment(UUID.randomUUID(), customer, service,
            secondStartTime,
            secondStartTime.plusMinutes(service.getDuration()), false
        );
        thirdAppointment = new Appointment(UUID.randomUUID(), customer, service,
            thirdStartTime,
            thirdStartTime.plusMinutes(service.getDuration()), false
        );
    }

    @DisplayName("Find All Appointments")
    @Test
    void findAllAppointments() {
        List<Appointment> expected =
            List.of(firstAppointment, secondAppointment, thirdAppointment);

        when(appointmentRepository.findByOrderByStartTime()).thenReturn(expected);

        assertIterableEquals(expected, appointmentService.findAll(),
            "The list of appointments should match the expected appointments");

        verify(appointmentRepository).findByOrderByStartTime();
    }

    @DisplayName("Find Appointment By ID")
    @Test
    void findAppointmentById() {
        List<Appointment> appointments =
            List.of(firstAppointment, secondAppointment, thirdAppointment);

        appointments.forEach(appointment -> {
            when(appointmentRepository.findById(appointment.getId()))
                .thenReturn(Optional.of(appointment));
            assertEquals(appointment, appointmentService.findById(appointment.getId()),
                "The appointment should match the one retrieved by ID");
            verify(appointmentRepository).findById(appointment.getId());
        });
    }

    @DisplayName("Save Appointment")
    @Test
    void saveAppointment() {
        List<Appointment> appointments =
            List.of(firstAppointment, secondAppointment, thirdAppointment);

        appointments.forEach(appointment -> {
            when(appointmentRepository.save(appointment)).thenReturn(appointment);
            assertEquals(appointment, appointmentService.save(appointment),
                "The saved appointment should match the one returned by the repository");
            verify(appointmentRepository).save(appointment);
        });
    }

    @DisplayName("Delete Appointment By ID")
    @Test
    void deleteAppointmentById() {
        UUID appointmentIdToDelete = UUID.randomUUID();

        appointmentService.deleteById(appointmentIdToDelete);

        verify(appointmentRepository).deleteById(appointmentIdToDelete);
    }

    @DisplayName("Find Active Appointments By Customer ID")
    @Test
    void findActiveAppointmentsByCustomerId() {
        List<Appointment> expected =
            List.of(secondAppointment, thirdAppointment);

        when(appointmentRepository
            .findAppointmentsByCustomerIdAndIsCompletedFalseOrderByStartTime(
                secondAppointment.getCustomer().getId())
        ).thenReturn(Optional.of(expected));

        assertEquals(expected, appointmentService.findActiveAppointmentsByCustomerId(
            secondAppointment.getCustomer().getId()
        ), "The list of active appointments should match the expected appointments");

        verify(appointmentRepository)
            .findAppointmentsByCustomerIdAndIsCompletedFalseOrderByStartTime(
                secondAppointment.getCustomer().getId()
            );
    }

    @DisplayName("Is Slot Available")
    @Test
    void isSlotAvailable() {

        LocalDateTime availableStart = secondAppointment.getStartTime();
        LocalDateTime availableEnd = secondAppointment.getEndTime();

        LocalDateTime unavailableStart = thirdAppointment.getStartTime();
        LocalDateTime unavailableEnd = thirdAppointment.getEndTime();

        when(appointmentRepository.isOverlapping(availableStart, availableEnd))
            .thenReturn(false);
        when(appointmentRepository.isOverlapping(unavailableStart, unavailableEnd))
            .thenReturn(true);

        assertTrue(appointmentService.isSlotAvailable(availableStart, availableEnd),
            "The slot should be available");
        assertFalse(appointmentService.isSlotAvailable(unavailableStart, unavailableEnd),
            "The slot should not be available");

        verify(appointmentRepository).isOverlapping(availableStart, availableEnd);
        verify(appointmentRepository).isOverlapping(unavailableStart, unavailableEnd);

    }

    @DisplayName("Get Available Slots - Wrong Date Cases")
    @Test
    void getAvailableSlotsWrongDateCases() {
        BusinessHour businessHour = new BusinessHour(UUID.randomUUID(), (byte) 0,
            LocalTime.of(9, 0), LocalTime.of(14, 30), true
        );
        Service selectedService = firstAppointment.getService();
        List<BusinessHour> selectedBusinessHours = List.of(businessHour);

        // wrong date case
        LocalDate beforeNowDate = LocalDate.now().minusDays(1);
        LocalDate afterMonthDate = LocalDate.now().plusDays(1).plusMonths(1);

        businessHour.setDayOfWeek((byte) beforeNowDate.getDayOfWeek().getValue());
        assertEquals(List.of(), appointmentService.getAvailableSlots(
            selectedService, beforeNowDate, selectedBusinessHours
        ), "There should be no available slots for date before now");

        businessHour.setDayOfWeek((byte) afterMonthDate.getDayOfWeek().getValue());
        assertEquals(List.of(), appointmentService.getAvailableSlots(
            selectedService, afterMonthDate, selectedBusinessHours
        ), "There should be no available slots for date after a month");
    }

    @DisplayName("Get Available Slots - Valid Case When All Slots Are Available")
    @Test
    void getAvailableSlotsValidCaseWhenAllSlotsAreAvailable() {
        BusinessHour firstBusinessHour = new BusinessHour(UUID.randomUUID(), (byte) 0,
            LocalTime.of(9, 0), LocalTime.of(12, 30), true
        );
        BusinessHour secondBusinessHour = new BusinessHour(UUID.randomUUID(), (byte) 0,
            LocalTime.of(13, 30), LocalTime.of(15, 30), true
        );
        Service selectedService = firstAppointment.getService();
        List<BusinessHour> selectedBusinessHours =
            List.of(firstBusinessHour, secondBusinessHour);

        LocalDate selectedDate = LocalDate.now().plusDays(1);
        selectedBusinessHours.forEach(businessHour ->
            businessHour.setDayOfWeek((byte) selectedDate.getDayOfWeek().getValue())
        );

        List<LocalTime> expectedAvailableSlots = List.of(
            LocalTime.of(9, 0), LocalTime.of(9, 45),
            LocalTime.of(10, 30), LocalTime.of(11, 15),
            LocalTime.of(13, 30), LocalTime.of(14, 15)
        );

        expectedAvailableSlots.forEach(time -> {
            LocalDateTime start = LocalDateTime.of(selectedDate, time);
            LocalDateTime end = start.plusMinutes(selectedService.getDuration());
            when(appointmentRepository.isOverlapping(start, end))
                .thenReturn(false);
        });

        assertIterableEquals(expectedAvailableSlots, appointmentService.getAvailableSlots(
            selectedService, selectedDate, selectedBusinessHours
        ), "The available slots should match the expected slots");

        expectedAvailableSlots.forEach(time -> {
            LocalDateTime start = LocalDateTime.of(selectedDate, time);
            LocalDateTime end = start.plusMinutes(selectedService.getDuration());
            verify(appointmentRepository).isOverlapping(start, end);
        });
    }

    @DisplayName("Get Available Slots - Valid Case When Some Slots Are Unavailable")
    @Test
    void getAvailableSlotsValidCaseWhenSomeSlotsAreUnavailable() {
        BusinessHour firstBusinessHour = new BusinessHour(UUID.randomUUID(), (byte) 0,
            LocalTime.of(9, 0), LocalTime.of(12, 30), true
        );
        BusinessHour secondBusinessHour = new BusinessHour(UUID.randomUUID(), (byte) 0,
            LocalTime.of(13, 30), LocalTime.of(15, 30), true
        );
        Service selectedService = firstAppointment.getService();
        List<BusinessHour> selectedBusinessHours =
            List.of(firstBusinessHour, secondBusinessHour);

        LocalDate selectedDate = LocalDate.now().plusDays(1);
        selectedBusinessHours.forEach(businessHour ->
            businessHour.setDayOfWeek((byte) selectedDate.getDayOfWeek().getValue())
        );

        Map<LocalTime, Boolean> slotsAvailability = Map.of(
            LocalTime.of(9, 0), false,
            LocalTime.of(9, 45), false,
            LocalTime.of(10, 30), true,
            LocalTime.of(11, 15), true,
            LocalTime.of(13, 30), true,
            LocalTime.of(14, 15), true
        );

        slotsAvailability.forEach((time, isAvailable) -> {
            LocalDateTime start = LocalDateTime.of(selectedDate, time);
            LocalDateTime end = start.plusMinutes(selectedService.getDuration());
            when(appointmentRepository.isOverlapping(start, end))
                .thenReturn(!isAvailable);
        });

        List<LocalTime> expectedAvailableSlots = List.of(
            LocalTime.of(10, 30), LocalTime.of(11, 15),
            LocalTime.of(13, 30), LocalTime.of(14, 15)
        );

        assertIterableEquals(expectedAvailableSlots, appointmentService.getAvailableSlots(
            selectedService, selectedDate, selectedBusinessHours
        ), "The available slots should match the expected slots");

        slotsAvailability.forEach((time, ignored) -> {
            LocalDateTime start = LocalDateTime.of(selectedDate, time);
            LocalDateTime end = start.plusMinutes(selectedService.getDuration());
            verify(appointmentRepository).isOverlapping(start, end);
        });
    }

    @DisplayName("Find All Not Marked As Completed Expired Appointments")
    @Test
    void findAllNotMarkedAsCompletedExpiredAppointments() {

        List<Appointment> expected = List.of(firstAppointment);

        when(appointmentRepository.findByEndTimeBeforeAndIsCompletedFalse(
            any(LocalDateTime.class))
        ).thenReturn(expected);

        assertIterableEquals(expected,
            appointmentService.findAllNotMarkedAsCompletedExpiredAppointments(),
            "The list of expired appointments should match the expected appointments"
        );

        verify(appointmentRepository)
            .findByEndTimeBeforeAndIsCompletedFalse(any(LocalDateTime.class));
    }

    @DisplayName("Export Appointments To XML - Active Only")
    @Test
    void exportAppointmentsToXMLActiveOnly() {
        List<Appointment> activeAppointments =
            List.of(secondAppointment, thirdAppointment);

        when(appointmentRepository.findAppointmentsByIsCompletedFalseOrderByStartTime())
            .thenReturn(activeAppointments);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            appointmentService.exportAppointmentsToXML(outputStream, true);
        } catch (Exception e) {
            fail("Exporting appointments to XML should not throw an exception: " + e.getMessage());
        }

        String xmlOutput = outputStream.toString();

        assertTrue(xmlOutput.contains("<appointments>"),
            "XML output should contain appointments root element");

        activeAppointments.forEach(appointment -> {
            assertTrue(xmlOutput.contains("<appointment>"),
                "XML output should contain appointment elements");
            assertTrue(xmlOutput.contains(appointment.getId().toString()),
                "XML output should contain the appointment ID: "
                    + appointment.getId());
            assertTrue(xmlOutput.contains(appointment.getCustomer().getFirstName()
                    + " " + appointment.getCustomer().getLastName()),
                "XML output should contain the customer name: "
                    + appointment.getCustomer().getFirstName() + " " + appointment.getCustomer().getLastName());
            assertTrue(xmlOutput.contains(appointment.getService().getServiceName()),
                "XML output should contain the service name: "
                    + appointment.getService().getServiceName());
            assertTrue(xmlOutput.contains(appointment.getStartTime().toString()),
                "XML output should contain the start time: "
                    + appointment.getStartTime());
            assertTrue(xmlOutput.contains(appointment.getEndTime().toString()),
                "XML output should contain the end time: "
                    + appointment.getEndTime());
        });

        assertFalse(xmlOutput.contains(firstAppointment.getId().toString()),
            "XML output should not contain the first appointment as it is completed");
    }

    @DisplayName("Export Appointments To XML - All Appointments")
    @Test
    void exportAppointmentsToXMLAllAppointments() {
        List<Appointment> allAppointments =
            List.of(firstAppointment, secondAppointment, thirdAppointment);

        when(appointmentRepository.findByOrderByStartTime())
            .thenReturn(allAppointments);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            appointmentService.exportAppointmentsToXML(outputStream, false);
        } catch (Exception e) {
            fail("Exporting appointments to XML should not throw an exception: " + e.getMessage());
        }

        String xmlOutput = outputStream.toString();

        assertTrue(xmlOutput.contains("<appointments>"),
            "XML output should contain appointments root element");

        allAppointments.forEach(appointment -> {
            assertTrue(xmlOutput.contains("<appointment>"),
                "XML output should contain appointment elements");
            assertTrue(xmlOutput.contains(appointment.getId().toString()),
                "XML output should contain the appointment ID: "
                    + appointment.getId());
            assertTrue(xmlOutput.contains(appointment.getCustomer().getFirstName()
                    + " " + appointment.getCustomer().getLastName()),
                "XML output should contain the customer name: "
                    + appointment.getCustomer().getFirstName() + " " + appointment.getCustomer().getLastName());
            assertTrue(xmlOutput.contains(appointment.getService().getServiceName()),
                "XML output should contain the service name: "
                    + appointment.getService().getServiceName());
            assertTrue(xmlOutput.contains(appointment.getStartTime().toString()),
                "XML output should contain the start time: "
                    + appointment.getStartTime());
            assertTrue(xmlOutput.contains(appointment.getEndTime().toString()),
                "XML output should contain the end time: "
                    + appointment.getEndTime());
        });
    }

    @DisplayName("Send Appointment Confirmation Email")
    @Test
    void sendAppointmentConfirmationEmail() {
        firstAppointment.setIsCompleted(false);
        List<Appointment> appointments =
            List.of(firstAppointment, secondAppointment, thirdAppointment);

        appointments.forEach(appointment -> {
            String toEmail = appointment.getCustomer().getEmail();
            String subject = "Appointment " + "Confirmation" + " – "
                + appointment.getService().getServiceName();
            String body = "Hello " + appointment.getCustomer().getFirstName() + ",\n\n" +
                "Your appointment has been scheduled for:\n" +
                "Date: " + appointment.getStartTime().toLocalDate() + "\n" +
                "Time: " + appointment.getStartTime().toLocalTime() + "\n" +
                "Service: " + appointment.getService().getServiceName() + "\n\n" +
                "Thank you for choosing our business!";
            appointmentService.sendAppointmentConfirmationEmail(appointment, false);
            try {
                verify(emailUtil).sendMail(toEmail, subject, body);
            } catch (Exception ignored) {
            } // ignored because EmailUtil is mocked
        });
    }

    @DisplayName("Mark Expired Appointments As Completed")
    @Test
    void markExpiredAppointmentsAsCompleted() {
        List<Appointment> expected = List.of(secondAppointment, thirdAppointment);

        when(appointmentRepository.findByEndTimeBeforeAndIsCompletedFalse(
            any(LocalDateTime.class))
        ).thenReturn(expected);

        when(appointmentRepository.saveAll(expected)).thenReturn(expected);

        appointmentService.markExpiredAppointmentsAsCompleted();

        assertAll(
            () -> assertTrue(secondAppointment.getIsCompleted(),
                "Second appointment should be marked as completed"),
            () -> assertTrue(thirdAppointment.getIsCompleted(),
                "Third appointment should be marked as completed")
        );

        verify(appointmentRepository)
            .findByEndTimeBeforeAndIsCompletedFalse(any(LocalDateTime.class));
        verify(appointmentRepository).saveAll(expected);
    }

    @DisplayName("Send Daily Reminders")
    @Test
    void sendDailyReminders() {
        firstAppointment.setIsCompleted(false);
        firstAppointment.setStartTime(LocalDateTime.now().plusHours(1));
        firstAppointment.setEndTime(firstAppointment.getStartTime().plusMinutes(
            firstAppointment.getService().getDuration()
        ));

        Appointment appointmentToRemind = firstAppointment;

        List<Appointment> expected = List.of(appointmentToRemind);

        when(appointmentRepository.findAppointmentsByStartTimeBetweenAndIsCompletedFalse(
            any(LocalDateTime.class), any(LocalDateTime.class))
        ).thenReturn(expected);

        String toEmail = appointmentToRemind.getCustomer().getEmail();
        String subject = "Reminder – Your Appointment is Today";
        String body = "Hello " + appointmentToRemind.getCustomer().getFirstName() + ",\n\n" +
            "This is a friendly reminder that you have an appointment today:\n" +
            "Time: " + appointmentToRemind.getStartTime().toLocalTime() + "\n" +
            "Service: " + appointmentToRemind.getService().getServiceName() + "\n\n" +
            "We look forward to seeing you!";
        appointmentService.sendDailyReminders();
        try {
            verify(emailUtil).sendMail(toEmail, subject, body);
        } catch (Exception ignored) {
        } // ignored because EmailUtil is mocked
    }
}