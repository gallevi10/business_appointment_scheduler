package com.javaworkshop.business_scheduler.controller;

import com.javaworkshop.business_scheduler.config.DefaultInitializer;
import com.javaworkshop.business_scheduler.config.GlobalModelAttributes;
import com.javaworkshop.business_scheduler.dto.AppointmentTimeForm;
import com.javaworkshop.business_scheduler.dto.BookAppointmentForm;
import com.javaworkshop.business_scheduler.dto.CustomerDetailsForm;
import com.javaworkshop.business_scheduler.dto.DashboardAppointment;
import com.javaworkshop.business_scheduler.model.*;
import com.javaworkshop.business_scheduler.service.AppointmentService;
import com.javaworkshop.business_scheduler.service.BookingService;
import com.javaworkshop.business_scheduler.service.BusinessInfoService;
import com.javaworkshop.business_scheduler.service.ServiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.ModelAndViewAssert.assertModelAttributeValue;
import static org.springframework.test.web.ModelAndViewAssert.assertViewName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class GeneralControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private ServiceService serviceService;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private BusinessInfoService businessInfoService;

    @MockitoBean
    private DefaultInitializer defaultInitializer;

    @MockitoBean
    private GlobalModelAttributes globalModelAttributes;

    private Service firstService, secondService, thirdService;

    private Customer firstCustomer, secondCustomer;

    private Appointment firstAppointment, secondAppointment;


    @BeforeEach
    void setUp() {
        firstService = new Service(UUID.randomUUID(), "first", BigDecimal.valueOf(100),
            30, null, true);
        secondService = new Service(UUID.randomUUID(), "second", BigDecimal.valueOf(200),
            60, null, true);
        thirdService = new Service(UUID.randomUUID(), "third", BigDecimal.valueOf(150),
            45, null, false);

        firstCustomer = new Customer(UUID.randomUUID(), null,
            "First", "First", "first@someservice.com", "0541111111");
        secondCustomer = new Customer(UUID.randomUUID(), null,
            "Second", "Second", "second@someservice.com", "0542222222");

        LocalDateTime firstStartTime =
            LocalDateTime.of(2025, 8, 7, 9, 0, 0);
        LocalDateTime secondStartTime =
            LocalDateTime.of(2025, 8, 7, 11, 0, 0);
        LocalDateTime thirdStartTime =
            LocalDateTime.of(2025, 8, 7, 15, 0, 0);
        firstAppointment = new Appointment(UUID.randomUUID(), firstCustomer, firstService,
            firstStartTime,
            firstStartTime.plusMinutes(firstService.getDuration()), true
        );
        secondAppointment = new Appointment(UUID.randomUUID(), secondCustomer, secondService,
            secondStartTime,
            secondStartTime.plusMinutes(secondService.getDuration()), false
        );
    }

    @DisplayName("Show Index Page")
    @Test
    void showIndexPage() throws Exception {
        BusinessInfo bi = new BusinessInfo(
            "Test",
            "Test",
            null
        );

        when(businessInfoService.getBusinessInfo())
            .thenReturn(bi);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/"))
            .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        assertModelAttributeValue(mav, "businessInfo", bi);
        assertViewName(mav, "general/index");

        verify(businessInfoService).getBusinessInfo();
    }

    @DisplayName("Error On Show Pick Service Page")
    @Test
    void errorOnShowPickServicePage() throws Exception {

        int[] wrongPages = {-1, 1000}; // assuming there are not more than 1000 pages

        for (int wrongPage : wrongPages) {
            when(serviceService.getServicePage(wrongPage, 3))
                .thenThrow(new RuntimeException());

            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/pick-service")
                    .param("p", wrongPage + ""))
                .andExpect(status().isOk())
                .andReturn();

            ModelAndView mav = mvcResult.getModelAndView();
            assertNotNull(mav, "ModelAndView should not be null");
            assertViewName(mav, "error/404");

            verify(serviceService).getServicePage(wrongPage, 3);
        }
    }

    @DisplayName("Successfully Show Pick Service Page")
    @Test
    void successfullyShowPickServicePage() throws Exception {
        List<Service> services = List.of(firstService, secondService);
        PageRequest pageRequest = PageRequest.of(0, 3);
        Page<Service> expectedPage = new PageImpl<>(services, pageRequest, services.size());
        when(serviceService.getServicePage(0, 3))
            .thenReturn(expectedPage);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/pick-service")
            .param("p", "0"))
            .andExpect(status().isOk())
            .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        assertNotNull(mav, "ModelAndView should not be null");
        assertModelAttributeValue(mav, "servicePage", expectedPage);
        assertModelAttributeValue(mav, "currentPage", 0);
        assertViewName(mav, "general/services");

        verify(serviceService).getServicePage(0, 3);
    }

    @DisplayName("Successfully Show Book Service Page")
    @Test
    void successfullyShowBookServicePage() throws Exception {
        Service selectedService = firstService;
        when(serviceService.findById(selectedService.getId()))
            .thenReturn(selectedService);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/book")
                .param("sid", selectedService.getId().toString()))
            .andExpect(status().isOk())
            .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        assertNotNull(mav, "ModelAndView should not be null");
        assertModelAttributeValue(mav, "form", new BookAppointmentForm(
            new CustomerDetailsForm(), new AppointmentTimeForm()
        ));
        assertModelAttributeValue(mav, "selectedService", selectedService);
        assertViewName(mav, "general/book");

        verify(serviceService).findById(selectedService.getId());
        verify(appointmentService, never()).findById(any(UUID.class));

    }

    @DisplayName("Successfully Show Update Appointment Page")
    @Test
    void successfullyShowUpdateAppointmentPage() throws Exception {
        Service selectedService = firstService;
        Appointment selectedAppointmentToUpdate = firstAppointment;
        when(serviceService.findById(selectedService.getId()))
            .thenReturn(selectedService);
        when(appointmentService.findById(selectedAppointmentToUpdate.getId()))
            .thenReturn(selectedAppointmentToUpdate);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/book")
            .param("sid", selectedService.getId().toString())
            .param("aid", selectedAppointmentToUpdate.getId().toString()))
            .andExpect(status().isOk())
            .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        assertNotNull(mav, "ModelAndView should not be null");
        assertModelAttributeValue(mav, "form", new BookAppointmentForm(
            new CustomerDetailsForm(), new AppointmentTimeForm(
                selectedAppointmentToUpdate.getStartTime().toLocalDate(),
                selectedAppointmentToUpdate.getStartTime().toLocalTime()
            )
        ));
        assertModelAttributeValue(mav, "selectedService", selectedService);
        assertViewName(mav, "general/book");

        verify(serviceService).findById(selectedService.getId());
        verify(appointmentService).findById(selectedAppointmentToUpdate.getId());

    }

    @DisplayName("Error On Show Book Service Page - Non-Existent Service Case")
    @Test
    void errorOnShowBookServiceNonExistentServiceCase() throws Exception {
        UUID nonExistentServiceId = UUID.randomUUID();
        when(serviceService.findById(nonExistentServiceId))
            .thenReturn(null);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/book")
            .param("sid", nonExistentServiceId.toString()))
            .andExpect(status().isOk())
            .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        assertNotNull(mav, "ModelAndView should not be null");
        assertViewName(mav, "error/404");

        verify(serviceService).findById(nonExistentServiceId);
        verify(appointmentService, never()).findById(any(UUID.class));

    }

    @DisplayName("Error On Show Update Appointment - Non-Existent Appointment Case")
    @Test
    void errorOnShowUpdateAppointmentNonExistentAppointmentCase() throws Exception {
        Service existentService = firstService;
        UUID nonExistentAppointmentId = UUID.randomUUID();
        when(serviceService.findById(existentService.getId()))
            .thenReturn(existentService);
        when(appointmentService.findById(nonExistentAppointmentId))
            .thenReturn(null);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/book")
            .param("sid", existentService.getId().toString())
            .param("aid", nonExistentAppointmentId.toString()))
            .andExpect(status().isOk())
            .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        assertNotNull(mav, "ModelAndView should not be null");
        assertViewName(mav, "error/404");

        verify(serviceService).findById(existentService.getId());
        verify(appointmentService).findById(nonExistentAppointmentId);

    }

    @DisplayName("Error On Show Update Appointment - Not Matching Appointment Case")
    @Test
    void errorOnShowUpdateAppointmentNotMatchingAppointmentCase() throws Exception {
        Service existentService = firstService;
        Appointment notMatchingAppointment = secondAppointment;
        when(serviceService.findById(existentService.getId()))
            .thenReturn(existentService);
        when(appointmentService.findById(notMatchingAppointment.getId()))
            .thenReturn(notMatchingAppointment);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/book")
                .param("sid", existentService.getId().toString())
                .param("aid", notMatchingAppointment.getId().toString()))
            .andExpect(status().isOk())
            .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        assertNotNull(mav, "ModelAndView should not be null");
        assertViewName(mav, "error/404");

        verify(serviceService).findById(existentService.getId());
        verify(appointmentService).findById(notMatchingAppointment.getId());

    }

    @DisplayName("Error On Process Booking - Non-Existent Service")
    @Test
    void errorOnProcessBookingNonExistentService() throws Exception {

        UUID nonExistentServiceId = UUID.randomUUID();
        when(serviceService.findById(nonExistentServiceId))
            .thenReturn(null);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/book/processBooking")
                .param("sid", nonExistentServiceId.toString())
                .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        assertNotNull(mav, "ModelAndView should not be null");
        assertViewName(mav, "error/404");

        verify(serviceService).findById(nonExistentServiceId);
        verify(bookingService, never()).bookAppointment(
            anyString(), anyString(), anyString(), anyString(),
            anyString(), any(Service.class), any(UUID.class),
            any(LocalDateTime.class), any(LocalDateTime.class)
        );

    }

    @DisplayName("Error On Process Booking - Basic Validation Errors")
    @Test
    void errorOnProcessBookingBasicValidationErrors() throws Exception {

        Service chosenService = firstService;
        when(serviceService.findById(chosenService.getId()))
            .thenReturn(chosenService);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/book/processBooking")
                .param("sid", chosenService.getId().toString())
                .param("customerDetailsForm.email", "invalidEmail") // invalid email case
                .param("customerDetailsForm.phoneNumber", "054") // too short phone case
                .param("customerDetailsForm.firstName", "") // empty field case
                .param("customerDetailsForm.lastName",
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa") // too long field case
                .param("appointmentTimeForm.appointmentDate", "22-8-2025") // wrong date format
                .param("appointmentTimeForm.appointmentTime", "2:333") // wrong time format
                .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        assertNotNull(mav, "ModelAndView should not be null");
        assertViewName(mav, "general/book");

        verify(serviceService).findById(chosenService.getId());
        verify(bookingService, never()).bookAppointment(
            anyString(), anyString(), anyString(), anyString(),
            anyString(), any(Service.class), any(UUID.class),
            any(LocalDateTime.class), any(LocalDateTime.class)
        );

    }

    @DisplayName("Error On Process Booking - Additional Validation Errors Guest Case")
    @Test
    void errorOnProcessBookingAdditionalValidationErrorsGuestCase() throws Exception {

        Service chosenService = firstService;
        when(serviceService.findById(chosenService.getId()))
            .thenReturn(chosenService);

        List<String> possibleErrorCodes = List.of(
            "error.appointmentTime.invalid.or.taken",
            "error.customer.email.conflict",
            "error.customer.phone.conflict",
            "error.customer.email.and.phone.conflict"
        );

        for (String possibleErrorCode : possibleErrorCodes) {
            doThrow(new RuntimeException(possibleErrorCode)).when(bookingService)
                .bookAppointment(anyString(), anyString(), anyString(), anyString(),
                    nullable(String.class), any(Service.class), nullable(UUID.class),
                    any(LocalDateTime.class), any(LocalDateTime.class));

            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                    .post("/book/processBooking")
                    .param("sid", chosenService.getId().toString())
                    .param("customerDetailsForm.email", "some@server.com")
                    .param("customerDetailsForm.phoneNumber", "0541111111")
                    .param("customerDetailsForm.firstName", "someFirstName")
                    .param("customerDetailsForm.lastName", "someLastName")
                    .param("appointmentTimeForm.appointmentDate", "2025-08-01")
                    .param("appointmentTimeForm.appointmentTime", "08:00")
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors()).andReturn();

            ModelAndView mav = mvcResult.getModelAndView();

            assertNotNull(mav, "ModelAndView should not be null");
            assertViewName(mav, "general/book");

            BindingResult bindingResult =
                (BindingResult) mav.getModel().get(BindingResult.MODEL_KEY_PREFIX + "form");

            assertTrue(bindingResult.getAllErrors().stream().anyMatch(
                    error -> possibleErrorCode.equals(error.getCode())),
                possibleErrorCode + " should be present in the errors");

        }

        verify(serviceService, times(possibleErrorCodes.size()))
            .findById(chosenService.getId());
        verify(bookingService, times(possibleErrorCodes.size())).bookAppointment(
            anyString(), anyString(), anyString(), anyString(),
            nullable(String.class), any(Service.class), nullable(UUID.class),
            any(LocalDateTime.class), any(LocalDateTime.class)
        );

    }

    @DisplayName("Error On Process Booking - Additional Validation Errors Authenticated Case")
    @WithMockUser(username = "user", roles = {"CUSTOMER"})
    @Test
    void errorOnProcessBookingAdditionalValidationErrorsGuestCaseAuthenticatedCase() throws Exception {

        Service chosenService = firstService;
        Customer customerToBookFor = firstCustomer;
        User user = new User(UUID.randomUUID(), "user",
            "123456789", "ROLE_CUSTOMER", true);
        customerToBookFor.setUser(user);

        when(serviceService.findById(chosenService.getId()))
            .thenReturn(chosenService);

        String possibleErrorCode = "error.appointmentTime.invalid.or.taken";

        doThrow(new RuntimeException(possibleErrorCode)).when(bookingService)
            .bookAppointment(nullable(String.class), nullable(String.class), nullable(String.class),
                nullable(String.class), nullable(String.class), any(Service.class), nullable(UUID.class),
                any(LocalDateTime.class), any(LocalDateTime.class));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .post("/book/processBooking")
                .param("sid", chosenService.getId().toString())
                .param("appointmentTimeForm.appointmentDate", "2025-08-01")
                .param("appointmentTimeForm.appointmentTime", "08:00")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(model().hasErrors()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        assertViewName(mav, "general/book");

        BindingResult bindingResult =
            (BindingResult) mav.getModel().get(BindingResult.MODEL_KEY_PREFIX + "form");

        assertTrue(bindingResult.getAllErrors().stream().anyMatch(
                error -> possibleErrorCode.equals(error.getCode())),
            possibleErrorCode + " should be present in the errors");



        verify(serviceService).findById(chosenService.getId());
        verify(bookingService).bookAppointment(
            nullable(String.class), nullable(String.class), nullable(String.class),
            nullable(String.class), nullable(String.class), any(Service.class), nullable(UUID.class),
            any(LocalDateTime.class), any(LocalDateTime.class)
        );

    }

    @DisplayName("Success On Process Booking")
    @Test
    void successProcessBooking() throws Exception {

        Service chosenService = firstService;
        Customer customerToBookFor = firstCustomer;
        Appointment expectedAppointment = firstAppointment;

        when(serviceService.findById(chosenService.getId()))
            .thenReturn(chosenService);
        when(bookingService.bookAppointment(
            customerToBookFor.getFirstName(), customerToBookFor.getLastName(),
            customerToBookFor.getEmail(), customerToBookFor.getPhone(),
            null, chosenService, null,
            expectedAppointment.getStartTime(), expectedAppointment.getEndTime()))
            .thenReturn(expectedAppointment);


        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .post("/book/processBooking")
                .param("sid", chosenService.getId().toString())
                .param("customerDetailsForm.email", customerToBookFor.getEmail())
                .param("customerDetailsForm.phoneNumber", customerToBookFor.getPhone())
                .param("customerDetailsForm.firstName", customerToBookFor.getFirstName())
                .param("customerDetailsForm.lastName", customerToBookFor.getLastName())
                .param("appointmentTimeForm.appointmentDate",
                    expectedAppointment.getStartTime().toLocalDate().toString())
                .param("appointmentTimeForm.appointmentTime",
                    expectedAppointment.getStartTime().toLocalTime().toString())
                .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        assertModelAttributeValue(mav, "bookedAppointment", expectedAppointment);
        assertModelAttributeValue(mav, "isRescheduling", false);
        assertViewName(mav, "general/success-booking");


        verify(serviceService).findById(chosenService.getId());
        verify(bookingService).bookAppointment(
            customerToBookFor.getFirstName(), customerToBookFor.getLastName(),
            customerToBookFor.getEmail(), customerToBookFor.getPhone(),
            null, chosenService, null,
            expectedAppointment.getStartTime(), expectedAppointment.getEndTime()
        );

    }

}