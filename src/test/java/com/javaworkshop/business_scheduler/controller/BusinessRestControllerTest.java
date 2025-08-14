package com.javaworkshop.business_scheduler.controller;

import com.javaworkshop.business_scheduler.model.BusinessHour;
import com.javaworkshop.business_scheduler.model.Service;
import com.javaworkshop.business_scheduler.service.AppointmentService;
import com.javaworkshop.business_scheduler.service.BusinessHourService;
import com.javaworkshop.business_scheduler.service.ServiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.ModelAndViewAssert.assertModelAttributeValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class BusinessRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServiceService serviceService;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private BusinessHourService businessHourService;

    private Service service;

    private LocalDate date;

    BusinessHour firstBusinessHour, secondBusinessHour;

    @BeforeEach
    void setUp() {
        service = new Service(UUID.randomUUID(), "service", BigDecimal.valueOf(100),
            30, null, true);
        date = LocalDate.of(2025, 8, 1);
        firstBusinessHour = new BusinessHour(
            UUID.randomUUID(), (byte) (date.getDayOfWeek().getValue() % 7),
            LocalTime.of(10, 30), LocalTime.of(12, 0), true
        );
        secondBusinessHour = new BusinessHour(
            UUID.randomUUID(), (byte) date.getDayOfWeek().getValue(),
            LocalTime.of(13, 30), LocalTime.of(14, 30), true
        );
    }

    @DisplayName("Get Available Slots - Non-Existent Service Case")
    @Test
    void getAvailableSlotsNonExistentServiceCase() throws Exception {

        UUID nonExistentServiceId = UUID.randomUUID();
        when(serviceService.findById(nonExistentServiceId))
            .thenReturn(null);

        String expectedJson = "[]";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/general/available-slots")
                .param("sid", nonExistentServiceId.toString())
                .param("d", date.toString()))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(serviceService).findById(nonExistentServiceId);

    }

    @DisplayName("Get Available Slots - Valid Input Casse When All Slots Are Available")
    @Test
    void getAvailableSlotsValidInputCaseWhenAllSlotsAreAvailable() throws Exception {

        when(serviceService.findById(service.getId()))
            .thenReturn(service);

        List<LocalTime> expectedSlots = List.of(
            LocalTime.of(10, 30),
            LocalTime.of(11, 0),
            LocalTime.of(11, 30),
            LocalTime.of(13, 30),
            LocalTime.of(14, 0)
        );

        List<BusinessHour> expectedBusinessHours = List.of(firstBusinessHour, secondBusinessHour);

        byte expectedDayOfWeek = (byte) (date.getDayOfWeek().getValue() % 7);

        when(businessHourService.findAllRangesByDayOfWeek(expectedDayOfWeek))
            .thenReturn(expectedBusinessHours);

        when(appointmentService.getAvailableSlots(service, date, expectedBusinessHours))
            .thenReturn(expectedSlots);

        String expectedJson = "[\"10:30:00\", \"11:00:00\", \"11:30:00\", \"13:30:00\", \"14:00:00\"]";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/general/available-slots")
                .param("sid", service.getId().toString())
                .param("d", date.toString()))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(serviceService).findById(service.getId());
        verify(businessHourService).findAllRangesByDayOfWeek(expectedDayOfWeek);
        verify(appointmentService).getAvailableSlots(
            service, date, expectedBusinessHours
        );

    }

    @DisplayName("Get Available Slots - Valid Input Case When Some Slots Are Unavailable")
    @Test
    void getAvailableSlotsValidInputCaseWhenAllSlotsAreUnavailable() throws Exception {

        when(serviceService.findById(service.getId()))
            .thenReturn(service);

        // assuming that 11:00 and 13:30 are taken
        List<LocalTime> expectedSlots = List.of(
            LocalTime.of(10, 30),
            LocalTime.of(11, 30),
            LocalTime.of(14, 0)
        );

        List<BusinessHour> expectedBusinessHours = List.of(firstBusinessHour, secondBusinessHour);

        byte expectedDayOfWeek = (byte) (date.getDayOfWeek().getValue() % 7);

        when(businessHourService.findAllRangesByDayOfWeek(expectedDayOfWeek))
            .thenReturn(expectedBusinessHours);

        when(appointmentService.getAvailableSlots(service, date, expectedBusinessHours))
            .thenReturn(expectedSlots);

        String expectedJson = "[\"10:30:00\", \"11:30:00\", \"14:00:00\"]";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/general/available-slots")
                .param("sid", service.getId().toString())
                .param("d", date.toString()))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(serviceService).findById(service.getId());
        verify(businessHourService).findAllRangesByDayOfWeek(expectedDayOfWeek);
        verify(appointmentService).getAvailableSlots(
            service, date, expectedBusinessHours
        );

    }
}