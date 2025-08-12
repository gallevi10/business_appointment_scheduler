package com.javaworkshop.business_scheduler.controller;

import com.javaworkshop.business_scheduler.dto.CustomerDetailsForm;
import com.javaworkshop.business_scheduler.dto.DashboardAppointment;
import com.javaworkshop.business_scheduler.model.Appointment;
import com.javaworkshop.business_scheduler.model.Customer;
import com.javaworkshop.business_scheduler.model.Service;
import com.javaworkshop.business_scheduler.model.User;
import com.javaworkshop.business_scheduler.service.AppointmentService;
import com.javaworkshop.business_scheduler.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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
import static org.springframework.test.web.ModelAndViewAssert.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class CustomerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private AppointmentService appointmentService;

    private User customerUser;

    private Customer customer;

    private Service service;

    private Appointment firstAppointment, secondAppointment, thirdAppointment;

    @BeforeEach
    void setUp() {
        customerUser = new User(UUID.randomUUID(), "customerUser",
                "123456789", "ROLE_CUSTOMER", true);
        customer = new Customer(UUID.randomUUID(), customerUser, "firstName",
                "lastName", "customer@server.com", "0541111111");

        service = new Service(UUID.randomUUID(), "Service",
                BigDecimal.valueOf(50), 45,
                null, true
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

    @DisplayName("Show All Customer Appointments")
    @WithMockUser(username = "customerUser", roles = {"CUSTOMER"})
    @Test
    void showAllCustomerAppointments() throws Exception{

        when(customerService.findIdByUsername(customerUser.getUsername()))
                .thenReturn(customer.getId());

        List<Appointment> expected = List.of(secondAppointment, thirdAppointment);
        when(appointmentService.findActiveAppointmentsByCustomerId(customer.getId()))
                .thenReturn(expected);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/customer-dashboard/appointments"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        assertModelAttributeValue(
                mav, "appointments", DashboardAppointment.fromAppointmentList(expected)
        );
        assertViewName(mav, "customer/appointments");

        verify(customerService).findIdByUsername(customerUser.getUsername());
        verify(appointmentService).findActiveAppointmentsByCustomerId(customer.getId());

    }

    @DisplayName("Cancel Appointment - Appointment Not Found")
    @WithMockUser(username = "customerUser", roles = {"CUSTOMER"})
    @Test
    void cancelAppointmentAppointmentNotFound() throws Exception {

        when(customerService.findIdByUsername(customerUser.getUsername()))
            .thenReturn(customer.getId());

        UUID nonExistentUUID = UUID.randomUUID();
        when(appointmentService.findById(nonExistentUUID))
            .thenReturn(null);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get("/customer-dashboard/appointments/cancel")
                .param("aid", nonExistentUUID.toString())
                .with(csrf()))
            .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        assertViewName(mav, "error/404");

        verify(customerService).findIdByUsername(customerUser.getUsername());
        verify(appointmentService, times(0)).deleteById(nonExistentUUID);

    }

    @DisplayName("Cancel Appointment - Not Matching Appointment")
    @WithMockUser(username = "customerUser", roles = {"CUSTOMER"})
    @Test
    void cancelAppointmentNotMatchingAppointment() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 8, 7, 10, 0, 0);
        LocalDateTime end = start.plusMinutes(service.getDuration());
        Customer otherCustomer = new Customer(UUID.randomUUID(), null, "otherFirstName",
                "otherLastName", "other@server.com", "0542222222");
        Appointment notMatchingAppointment = new Appointment(
                UUID.randomUUID(), otherCustomer, service, start, end, false
        );

        when(customerService.findIdByUsername(customerUser.getUsername()))
                .thenReturn(customer.getId());

        when(appointmentService.findById(notMatchingAppointment.getId()))
            .thenReturn(notMatchingAppointment);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/customer-dashboard/appointments/cancel")
                .param("aid", notMatchingAppointment.getId().toString())
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/customer-dashboard/appointments"));

        verify(customerService).findIdByUsername(customerUser.getUsername());
        verify(appointmentService, times(0)).deleteById(notMatchingAppointment.getId());

    }

    @DisplayName("Successfully Cancel Appointment")
    @WithMockUser(username = "customerUser", roles = {"CUSTOMER"})
    @Test
    void successfullyCancelAppointment() throws Exception {

        when(customerService.findIdByUsername(customerUser.getUsername()))
            .thenReturn(customer.getId());

        when(appointmentService.findById(secondAppointment.getId()))
            .thenReturn(secondAppointment);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/customer-dashboard/appointments/cancel")
                .param("aid", secondAppointment.getId().toString())
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/customer-dashboard/appointments"));

        verify(customerService).findIdByUsername(customerUser.getUsername());
        verify(appointmentService).deleteById(secondAppointment.getId());

    }

    @DisplayName("Show Customer Profile")
    @WithMockUser(username = "customerUser", roles = {"CUSTOMER"})
    @Test
    void showCustomerProfile() throws Exception {

        when(customerService.findByUsername(customerUser.getUsername()))
                .thenReturn(customer);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/customer-dashboard/profile"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        assertModelAttributeValue(mav, "form", new CustomerDetailsForm(customer));
        assertViewName(mav, "customer/profile");

        verify(customerService).findByUsername(customerUser.getUsername());

    }

    @DisplayName("Process Change Profile Form - Basic Validation Error Case")
    @WithMockUser(username = "customerUser", roles = {"CUSTOMER"})
    @Test
    void processChangeProfileBasicValidationErrorCase() throws Exception {

        when(customerService.findByUsername(customerUser.getUsername()))
            .thenReturn(customer);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(
            "/customer-dashboard/profile/processChangeProfile")
                .param("email", "wrongEmail") // wrong email format
                .param("phoneNumber", "054") // too short phone number
                .param("firstName", "") // empty first name
                .param("lastName", "a") // too short last name
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors(
                "form",
                "email",
                "phoneNumber",
                "firstName",
                "lastName"
            )).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        assertViewName(mav, "customer/profile");

        verify(customerService).findByUsername(customerUser.getUsername());
        verify(customerService, times(0)).updateCustomerDetails(
            eq(customer), anyString(), anyString(), anyString(), anyString()
        );
    }

    @DisplayName("Process Change Profile Form - Additional Validation Error Case")
    @WithMockUser(username = "customerUser", roles = {"CUSTOMER"})
    @Test
    void processChangeProfileFormAdditionalValidationErrorCase() throws Exception {

        List<String> possibleErrorCodes = List.of(
            "error.customer.email.conflict",
            "error.customer.phone.conflict"
        );

        when(customerService.findByUsername(customerUser.getUsername()))
            .thenReturn(customer);

        for (String possibleErrorCode : possibleErrorCodes) {
            doThrow(new RuntimeException(possibleErrorCode)).when(customerService)
                .updateCustomerDetails(eq(customer), anyString(), anyString(), anyString(), anyString());

            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                    .post("/customer-dashboard/profile/processChangeProfile")
                    .param("email", "some@server.com")
                    .param("phoneNumber", "0541111111")
                    .param("firstName", "someFirstName")
                    .param("lastName", "someLastName")
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors()).andReturn();

            ModelAndView mav = mvcResult.getModelAndView();

            assertNotNull(mav, "ModelAndView should not be null");
            assertViewName(mav, "customer/profile");

            BindingResult bindingResult =
                (BindingResult) mav.getModel().get(BindingResult.MODEL_KEY_PREFIX + "form");

            assertTrue(bindingResult.getAllErrors().stream().anyMatch(
                    error -> possibleErrorCode.equals(error.getCode())),
                possibleErrorCode + " should be present in the errors");

        }

        verify(customerService, times(possibleErrorCodes.size()))
            .findByUsername(customerUser.getUsername());
        verify(customerService, times(possibleErrorCodes.size())).updateCustomerDetails(
            eq(customer), anyString(), anyString(),
            anyString(), anyString()
        );

    }

    @DisplayName("Successfully Process Change Profile Form")
    @WithMockUser(username = "customerUser", roles = {"CUSTOMER"})
    @Test
    void successfullyProcessChangeProfileForm() throws Exception {

        String newEmail = "new@server.com";
        String newPhone = "0542222222";
        String newFirstName = "newFirstName";
        String newLastName = "newLastName";

        when(customerService.findByUsername(customerUser.getUsername()))
            .thenReturn(customer);

        mockMvc.perform(MockMvcRequestBuilders
            .post("/customer-dashboard/profile/processChangeProfile")
            .param("email", newEmail)
            .param("phoneNumber", newPhone)
            .param("firstName", newFirstName)
            .param("lastName", newLastName)
            .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/customer-dashboard/profile?success"));

        verify(customerService).findByUsername(customerUser.getUsername());
        verify(customerService).updateCustomerDetails(
            customer, newEmail, newPhone,
            newFirstName, newLastName
        );

    }
}