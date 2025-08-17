package com.javaworkshop.business_scheduler.controller;

import com.javaworkshop.business_scheduler.config.DefaultInitializer;
import com.javaworkshop.business_scheduler.config.GlobalModelAttributes;
import com.javaworkshop.business_scheduler.dto.*;
import com.javaworkshop.business_scheduler.model.*;
import com.javaworkshop.business_scheduler.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.ModelAndViewAssert.assertModelAttributeValue;
import static org.springframework.test.web.ModelAndViewAssert.assertViewName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class OwnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BusinessHourService businessHourService;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private ServiceService serviceService;

    @MockitoBean
    private BusinessInfoService businessInfoService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private DefaultInitializer defaultInitializer;

    @MockitoBean
    private GlobalModelAttributes globalModelAttributes;

    private User ownerUser;

    private Customer customer;

    private Service service;

    private Appointment appointment;

    private BusinessHour businessHour;

    @BeforeEach
    void setUp() {
        ownerUser = new User(UUID.randomUUID(), "ownerUser",
            "password", "ROLE_OWNER", true);

        customer = new Customer(UUID.randomUUID(), null,
            "First", "First", "first@someservice.com", "0541111111");

        service = new Service(UUID.randomUUID(), "service", BigDecimal.valueOf(100),
            30, null, true);

        LocalDateTime firstStartTime =
            LocalDateTime.of(2025, 8, 7, 9, 0, 0);

        appointment = new Appointment(UUID.randomUUID(), customer, service,
            firstStartTime,
            firstStartTime.plusMinutes(service.getDuration()), true
        );

        businessHour = new BusinessHour(UUID.randomUUID(), (byte) 0,
            LocalTime.of(9, 0), LocalTime.of(17, 0), true);

    }

    @DisplayName("Show All Appointments")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void showAllAppointments() throws Exception {

        LocalDateTime secondStartTime =
            LocalDateTime.of(2025, 8, 7, 11, 0, 0);
        LocalDateTime thirdStartTime =
            LocalDateTime.of(2025, 8, 7, 15, 0, 0);

        Appointment secondAppointment = new Appointment(UUID.randomUUID(), customer, service,
            secondStartTime,
            secondStartTime.plusMinutes(service.getDuration()), false
        );
        Appointment thirdAppointment = new Appointment(UUID.randomUUID(), customer, service,
            thirdStartTime,
            thirdStartTime.plusMinutes(service.getDuration()), false
        );

        List<Appointment> expectedAppointments = List.of(
            appointment, secondAppointment, thirdAppointment
        );

        when(appointmentService.findAll()).thenReturn(expectedAppointments);

        List<DashboardAppointment> expectedDashboardAppointments =
            DashboardAppointment.fromAppointmentList(expectedAppointments);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/owner-dashboard/appointments"))
            .andExpect(status().isOk())
            .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        assertModelAttributeValue(mav, "appointments", expectedDashboardAppointments);
        assertViewName(mav, "owner/appointments");

        verify(appointmentService).findAll();

    }

    @DisplayName("Cancel Appointment")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void cancelAppointment() throws Exception {
        UUID appointmentToCancelId = appointment.getId();

        mockMvc.perform(MockMvcRequestBuilders.get("/owner-dashboard/appointments/cancel")
                .param("aid", appointmentToCancelId.toString()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/owner-dashboard/appointments"));

        verify(appointmentService).deleteById(appointmentToCancelId);
    }

    @DisplayName("Error On Export Appointments to XML")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void errorOnExportAppointmentsToXml() throws Exception {

        doThrow(XMLStreamException.class).when(appointmentService)
            .exportAppointmentsToXML(any(OutputStream.class), anyBoolean());

        mockMvc.perform(MockMvcRequestBuilders.get("/owner-dashboard/appointments/export-to-xml")
            .param("active", "true"))
            .andExpect(status().isInternalServerError());

        verify(appointmentService).exportAppointmentsToXML(any(OutputStream.class), anyBoolean());

    }

    @DisplayName("Success On Export Appointments to XML")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void successOnExportAppointmentsToXml() throws Exception {

        byte[] data = "<appointments>some data</appointments>".getBytes(StandardCharsets.UTF_8);

        doAnswer(inv -> {
            OutputStream os = inv.getArgument(0, OutputStream.class);
            os.write(data);
            os.flush();
            return null;
        }).when(appointmentService).exportAppointmentsToXML(any(OutputStream.class), anyBoolean());

        mockMvc.perform(MockMvcRequestBuilders.get("/owner-dashboard/appointments/export-to-xml")
                .param("active", "true"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=appointments.xml"))
            .andExpect(content().contentType(MediaType.APPLICATION_XML))
            .andExpect(content().bytes(data));

        verify(appointmentService).exportAppointmentsToXML(any(OutputStream.class), anyBoolean());

    }

    @DisplayName("Show Service Manager")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void showServiceManager() throws Exception {

        Service secondService = new Service(UUID.randomUUID(), "second", BigDecimal.valueOf(200),
            60, null, true);
        Service thirdService = new Service(UUID.randomUUID(), "third", BigDecimal.valueOf(150),
            45, null, false);

        List<Service> expectedServices = List.of(service, secondService, thirdService);

        when(serviceService.findAll()).thenReturn(expectedServices);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/owner-dashboard/service-manager"))
            .andExpect(status().isOk())
            .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        assertModelAttributeValue(mav, "services", expectedServices);
        assertViewName(mav, "owner/service-manager");

        verify(serviceService).findAll();
    }

    @DisplayName("Delete Service")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void deleteService() throws Exception {
        UUID serviceToDeleteId = service.getId();

        mockMvc.perform(MockMvcRequestBuilders.get("/owner-dashboard/service-manager/delete")
                .param("sid", serviceToDeleteId.toString()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/owner-dashboard/service-manager"));

        verify(serviceService).deleteById(serviceToDeleteId);
    }

    @DisplayName("Change Service Availability")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void changeServiceAvailability() throws Exception {
        Service serviceToChange = service;
        serviceToChange.setIsActive(true);

        when(serviceService.findById(serviceToChange.getId()))
            .thenReturn(serviceToChange);

        mockMvc.perform(MockMvcRequestBuilders.get("/owner-dashboard/service-manager/change-availability")
                .param("sid", serviceToChange.getId().toString()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/owner-dashboard/service-manager"));

        assertFalse(serviceToChange.getIsActive(),
            "Service should be inactive after changing availability");
        verify(serviceService).findById(serviceToChange.getId());
    }

    @DisplayName("Show Add Service")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void showAddService() throws Exception {

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get("/owner-dashboard/service-manager/add-service"))
            .andExpect(status().isOk())
            .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        assertModelAttributeValue(mav, "form", new NewServiceForm());
        assertViewName(mav, "owner/add-service");

    }

    @DisplayName("Error On Show Update Service - Non-Existent Service")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void errorOnShowUpdateServiceNonExistentService() throws Exception {
        UUID nonExistentServiceId = UUID.randomUUID();

        when(serviceService.findById(nonExistentServiceId))
            .thenReturn(null);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get("/owner-dashboard/service-manager/update-service")
                .param("sid", nonExistentServiceId.toString()))
            .andExpect(status().isOk())
            .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        assertViewName(mav, "error/404");

        verify(serviceService).findById(nonExistentServiceId);
    }

    @DisplayName("Success On Show Update Service")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void successOnShowUpdateService() throws Exception {
        Service serviceToUpdate = service;

        when(serviceService.findById(serviceToUpdate.getId()))
            .thenReturn(serviceToUpdate);

        NewServiceForm expectedForm = new NewServiceForm(
            serviceToUpdate.getServiceName(),
            serviceToUpdate.getPrice(),
            serviceToUpdate.getDuration(),
            serviceToUpdate.getImagePath(),
            null
        );

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get("/owner-dashboard/service-manager/update-service")
                .param("sid", serviceToUpdate.getId().toString()))
            .andExpect(status().isOk())
            .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        assertModelAttributeValue(mav, "form", expectedForm);
        assertViewName(mav, "owner/add-service");

        verify(serviceService).findById(serviceToUpdate.getId());
    }

    @DisplayName("Error On Remove Service Image - Already Removed Case")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void errorOnRemoveServiceImageAlreadyRemovedCase() throws Exception {
        UUID serviceWithoutImageId = UUID.randomUUID();

        doThrow(new RuntimeException()).when(serviceService)
            .removeServiceImage(serviceWithoutImageId);

        String expectedUrl =
            "/owner-dashboard/service-manager/update-service?sid=" + serviceWithoutImageId + "&noImage";

        mockMvc.perform(MockMvcRequestBuilders
            .get("/owner-dashboard/service-manager/update-service/remove-image")
            .param("sid", serviceWithoutImageId.toString()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(expectedUrl));

        verify(serviceService).removeServiceImage(serviceWithoutImageId);
    }

    @DisplayName("Error On Remove Service Image - IO Exception Case")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void errorOnRemoveServiceImageIOExceptionCase() throws Exception {
        UUID serviceToRemoveImageId = UUID.randomUUID();

        doThrow(IOException.class).when(serviceService)
            .removeServiceImage(serviceToRemoveImageId);

        String expectedUrl =
            "/owner-dashboard/service-manager/update-service?sid=" + serviceToRemoveImageId + "&removeFailure";

        mockMvc.perform(MockMvcRequestBuilders
                .get("/owner-dashboard/service-manager/update-service/remove-image")
                .param("sid", serviceToRemoveImageId.toString()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(expectedUrl));

        verify(serviceService).removeServiceImage(serviceToRemoveImageId);
    }

    @DisplayName("Success On Remove Service Image")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void successRemoveServiceImage() throws Exception {
        UUID serviceToRemoveImageId = UUID.randomUUID();

        String expectedUrl =
            "/owner-dashboard/service-manager/update-service?sid=" + serviceToRemoveImageId + "&removeSuccess";

        mockMvc.perform(MockMvcRequestBuilders
                .get("/owner-dashboard/service-manager/update-service/remove-image")
                .param("sid", serviceToRemoveImageId.toString()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(expectedUrl));

        verify(serviceService).removeServiceImage(serviceToRemoveImageId);
    }

    @DisplayName("Process Add New Service Form - Basic Validation Error Case")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void processAddNewServiceFormBasicValidationErrorCase() throws Exception {

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .post("/owner-dashboard/service-manager/add-service/processAddingService")
                .param("serviceName", "") // empty service name
                .param("price", "1.11111") // wrong price format
                .param("duration", "1000") // wrong duration format
                .param("serviceImage", "")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors(
                "form",
                "serviceName",
                "price",
                "duration",
                "serviceImage"
            )).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        assertViewName(mav, "owner/add-service");
        verify(serviceService, never()).addOrUpdateService(
            nullable(Service.class), anyString(), any(BigDecimal.class),
            anyInt(), nullable(MultipartFile.class)
        );
    }

    @DisplayName("Process Add New Service Form - Additional Validation Error Case")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void processAddNewServiceFormAdditionalValidationErrorCase() throws Exception {

        String possibleErrorCode = "error.service.service.name.conflict";

        MockMultipartFile someServiceImage = new MockMultipartFile(
            "serviceImage",
            "someServiceImage",
            MediaType.IMAGE_JPEG_VALUE,
            "some image content".getBytes(StandardCharsets.UTF_8)
        );

        doThrow(new RuntimeException(possibleErrorCode)).when(serviceService)
            .addOrUpdateService(nullable(Service.class), anyString(), any(BigDecimal.class),
                anyInt(), nullable(MultipartFile.class));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .multipart("/owner-dashboard/service-manager/add-service/processAddingService")
                .file(someServiceImage)
                .param("serviceName", "Some Service")
                .param("price", "10")
                .param("duration", "30")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(model().hasErrors()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        assertViewName(mav, "owner/add-service");

        BindingResult bindingResult =
            (BindingResult) mav.getModel().get(BindingResult.MODEL_KEY_PREFIX + "form");

        assertTrue(bindingResult.getAllErrors().stream().anyMatch(
                error -> possibleErrorCode.equals(error.getCode())),
            possibleErrorCode + " should be present in the errors");

        verify(serviceService).addOrUpdateService(
            nullable(Service.class), anyString(), any(BigDecimal.class),
            anyInt(), nullable(MultipartFile.class)
        );

    }

    @DisplayName("Successfully Process Add New Service Form")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void successfullyProcessAddNewServiceForm() throws Exception {

        String serviceNameToAdd = "New Service";
        BigDecimal priceToAdd = BigDecimal.valueOf(150);
        int durationToAdd = 45;
        MockMultipartFile serviceImageToAdd = new MockMultipartFile(
            "serviceImage",
            "serviceImage.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "image content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/owner-dashboard/service-manager/add-service/processAddingService")
                .file(serviceImageToAdd)
                .param("serviceName", serviceNameToAdd)
                .param("price", priceToAdd + "")
                .param("duration", durationToAdd + "")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/owner-dashboard/service-manager"));

        verify(serviceService).addOrUpdateService(
            null, serviceNameToAdd, priceToAdd,
            durationToAdd, serviceImageToAdd
        );

    }

    @DisplayName("Process Update Service Form - Non-Existent Service Case")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void processUpdateServiceFormNonExistentServiceCase() throws Exception {

        UUID nonExistentServiceId = UUID.randomUUID();
        String serviceNameToUpdate = "Updated Service";
        BigDecimal priceToUpdate = BigDecimal.valueOf(150);
        int durationToUpdate = 45;
        MockMultipartFile serviceImageToUpdate = new MockMultipartFile(
            "serviceImage",
            "serviceImage.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "image content".getBytes(StandardCharsets.UTF_8)
        );

        when(serviceService.findById(nonExistentServiceId))
            .thenReturn(null);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .multipart("/owner-dashboard/service-manager/add-service/processAddingService")
                .file(serviceImageToUpdate)
                .param("sid", nonExistentServiceId.toString())
                .param("serviceName", serviceNameToUpdate)
                .param("price", priceToUpdate + "")
                .param("duration", durationToUpdate + "")
                .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        assertNotNull(mav, "ModelAndView should not be null");
        assertViewName(mav, "error/404");

        verify(serviceService, never()).addOrUpdateService(
            nullable(Service.class), anyString(), any(BigDecimal.class),
            anyInt(), nullable(MultipartFile.class)
        );

    }

    @DisplayName("Successfully Process Update Service Form")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void successfullyProcessUpdateServiceForm() throws Exception {

        Service serviceToUpdate = service;
        String serviceNameToUpdate = "Updated Service";
        BigDecimal priceToUpdate = BigDecimal.valueOf(150);
        int durationToUpdate = 45;
        MockMultipartFile serviceImageToUpdate = new MockMultipartFile(
            "serviceImage",
            "serviceImage.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "image content".getBytes(StandardCharsets.UTF_8)
        );

        when(serviceService.findById(serviceToUpdate.getId()))
            .thenReturn(serviceToUpdate);

        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/owner-dashboard/service-manager/add-service/processAddingService")
                .file(serviceImageToUpdate)
                .param("sid", serviceToUpdate.getId().toString())
                .param("serviceName", serviceNameToUpdate)
                .param("price", priceToUpdate + "")
                .param("duration", durationToUpdate + "")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/owner-dashboard/service-manager"));

        verify(serviceService).addOrUpdateService(
            serviceToUpdate, serviceNameToUpdate, priceToUpdate,
            durationToUpdate, serviceImageToUpdate
        );

    }

    @DisplayName("Show Opening Hours")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void showOpeningHours() throws Exception {

        BusinessHour secondBusinessHour = new BusinessHour(UUID.randomUUID(), (byte) 1,
            LocalTime.of(12, 30), LocalTime.of(18, 0), true);
        BusinessHour thirdBusinessHour = new BusinessHour(UUID.randomUUID(), (byte) 2,
            LocalTime.of(9, 0), LocalTime.of(19, 0), false);
        List<BusinessHour> businessHours = List.of(businessHour, secondBusinessHour, thirdBusinessHour);

        when(businessHourService.findAll()).thenReturn(businessHours);

        List<OpeningHour> expectedOpeningHours = OpeningHour.fromBusinessHours(businessHours);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/owner-dashboard/opening-hours"))
            .andExpect(status().isOk())
            .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        assertNotNull(mav, "ModelAndView should not be null");
        assertModelAttributeValue(mav, "openingHours", expectedOpeningHours);
        assertViewName(mav, "owner/opening-hours");

        verify(businessHourService).findAll();

    }

    @DisplayName("Show Add Range")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void showAddRange() throws Exception {

        byte dayOfWeekToAddARange = 0;

        NewRangeForm expectedForm = new NewRangeForm();
        expectedForm.setDayOfWeek(dayOfWeekToAddARange);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get("/owner-dashboard/opening-hours/add-range")
                .param("dow", dayOfWeekToAddARange + ""))
            .andExpect(status().isOk())
            .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        assertNotNull(mav, "ModelAndView should not be null");
        assertModelAttributeValue(mav, "form", expectedForm);
        assertViewName(mav, "owner/add-range");
    }

    @DisplayName("Error On Show Edit Range - Non-Existent Range")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void errorOnShowEditRangeNonExistentRange() throws Exception {

        UUID nonExistentRangeId = UUID.randomUUID();
        when(businessHourService.findById(nonExistentRangeId))
            .thenReturn(null);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get("/owner-dashboard/opening-hours/edit-range")
                .param("bhid", nonExistentRangeId.toString()))
            .andExpect(status().isOk())
            .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        assertNotNull(mav, "ModelAndView should not be null");
        assertViewName(mav, "error/404");

        verify(businessHourService).findById(nonExistentRangeId);

    }

    @DisplayName("Success On Show Edit Range")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void successOnShowEditRange() throws Exception {

        BusinessHour rangeToEdit = businessHour;

        when(businessHourService.findById(rangeToEdit.getId()))
            .thenReturn(rangeToEdit);

        NewRangeForm expectedForm = new NewRangeForm(
            rangeToEdit.getDayOfWeek(),
            rangeToEdit.getStartTime(),
            rangeToEdit.getEndTime(),
            rangeToEdit.getIsOpen()
        );

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get("/owner-dashboard/opening-hours/edit-range")
                .param("bhid", rangeToEdit.getId().toString()))
            .andExpect(status().isOk())
            .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        assertNotNull(mav, "ModelAndView should not be null");
        assertModelAttributeValue(mav, "form", expectedForm);
        assertViewName(mav, "owner/add-range");

        verify(businessHourService).findById(rangeToEdit.getId());

    }

    @DisplayName("Delete Range - More Than One Range Case")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void deleteRangeMoreThanOneRangeCase() throws Exception {
        UUID rangeToDeleteId = businessHour.getId();
        byte expectedDayOfWeek = businessHour.getDayOfWeek();
        BusinessHour anotherBusinessHour = new BusinessHour(
            UUID.randomUUID(), expectedDayOfWeek,
            LocalTime.of(18, 30), LocalTime.of(22, 0), true
        );

        when(businessHourService.findDayOfWeekById(rangeToDeleteId))
            .thenReturn(expectedDayOfWeek);

        when(businessHourService.findAllRangesByDayOfWeek(expectedDayOfWeek))
            .thenReturn(List.of(businessHour, anotherBusinessHour));

        mockMvc.perform(MockMvcRequestBuilders.get("/owner-dashboard/opening-hours/delete-range")
                .param("bhid", rangeToDeleteId.toString()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/owner-dashboard/opening-hours"));

        verify(businessHourService).findDayOfWeekById(rangeToDeleteId);
        verify(businessHourService).findAllRangesByDayOfWeek(expectedDayOfWeek);
        verify(businessHourService).deleteById(rangeToDeleteId);
    }

    @DisplayName("Delete Range - Only One Range Case")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void deleteRangeOnlyOneRangeCase() throws Exception {
        UUID rangeToDeleteId = businessHour.getId();
        byte expectedDayOfWeek = businessHour.getDayOfWeek();

        when(businessHourService.findDayOfWeekById(rangeToDeleteId))
            .thenReturn(expectedDayOfWeek);

        when(businessHourService.findAllRangesByDayOfWeek(expectedDayOfWeek))
            .thenReturn(List.of(businessHour));

        mockMvc.perform(MockMvcRequestBuilders.get("/owner-dashboard/opening-hours/delete-range")
                .param("bhid", rangeToDeleteId.toString()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/owner-dashboard/opening-hours"));

        verify(businessHourService).findDayOfWeekById(rangeToDeleteId);
        verify(businessHourService).findAllRangesByDayOfWeek(expectedDayOfWeek);
        verify(businessHourService, never()).deleteById(rangeToDeleteId);
    }

    @DisplayName("Process Add New Range Form - Basic Validation Error Case")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void processAddNewRangeFormBasicValidationErrorCase() throws Exception {

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .post("/owner-dashboard/opening-hours/processNewRange")
                .param("dayOfWeek", "8") // too high day of week
                .param("startTime", "111") // wrong format
                .param("endTime", "55:11") // wrong format
                .param("isOpen", "") // empty isOpen
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors(
                "form",
                "dayOfWeek",
                "startTime",
                "endTime",
                "isOpen"
            )).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        assertViewName(mav, "owner/add-range");
        verify(businessHourService, never()).addOrUpdateBusinessHour(
            nullable(UUID.class), anyByte(), any(LocalTime.class),
            any(LocalTime.class), anyBoolean()
        );
    }

    @DisplayName("Process Add New Range Form - Additional Validation Error Case")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void processAddNewRangeFormAdditionalValidationErrorCase() throws Exception {

        List<String> possibleErrorCodes = List.of(
            "error.business.hour.start.after.end",
            "error.business.hour.overlapping"
        );

        for (String possibleErrorCode : possibleErrorCodes) {
            doThrow(new RuntimeException(possibleErrorCode)).when(businessHourService)
                .addOrUpdateBusinessHour(nullable(UUID.class), anyByte(),
                    any(LocalTime.class), any(LocalTime.class), anyBoolean());

            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                    .post("/owner-dashboard/opening-hours/processNewRange")
                    .param("dayOfWeek", "5")
                    .param("startTime", "11:00")
                    .param("endTime", "12:00")
                    .param("isOpen", "true")
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors()).andReturn();

            ModelAndView mav = mvcResult.getModelAndView();

            assertNotNull(mav, "ModelAndView should not be null");
            assertViewName(mav, "owner/add-range");

            BindingResult bindingResult =
                (BindingResult) mav.getModel().get(BindingResult.MODEL_KEY_PREFIX + "form");

            assertTrue(bindingResult.getAllErrors().stream().anyMatch(
                    error -> possibleErrorCode.equals(error.getCode())),
                possibleErrorCode + " should be present in the errors");

        }

        verify(businessHourService, times(possibleErrorCodes.size())).addOrUpdateBusinessHour(
            nullable(UUID.class), anyByte(),
            any(LocalTime.class), any(LocalTime.class), anyBoolean()
        );

    }

    @DisplayName("Successfully Process Add New Range Form")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void successfullyProcessAddNewRangeForm() throws Exception {

        byte dayOfWeekToAdd = 0;
        LocalTime startTimeToAdd = LocalTime.of(10, 0);
        LocalTime endTimeToAdd = LocalTime.of(12, 0);
        boolean isOpenToAdd = true;

        mockMvc.perform(MockMvcRequestBuilders
                .post("/owner-dashboard/opening-hours/processNewRange")
                .param("dayOfWeek", dayOfWeekToAdd + "")
                .param("startTime", startTimeToAdd.toString())
                .param("endTime", endTimeToAdd.toString())
                .param("isOpen", isOpenToAdd + "")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/owner-dashboard/opening-hours"));

        verify(businessHourService).addOrUpdateBusinessHour(
            null, dayOfWeekToAdd,
            startTimeToAdd, endTimeToAdd, isOpenToAdd
        );

    }

    @DisplayName("Successfully Process Update Range Form")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void successfullyProcessUpdateRangeForm() throws Exception {

        UUID rangeToUpdateId = businessHour.getId();
        byte dayOfWeekToAdd = businessHour.getDayOfWeek();
        LocalTime newStartTime = LocalTime.of(9, 0);
        LocalTime newEndTime = LocalTime.of(18, 30);
        boolean newIsOpen = true;

        when(businessHourService.findDayOfWeekById(rangeToUpdateId))
            .thenReturn(dayOfWeekToAdd);

        mockMvc.perform(MockMvcRequestBuilders
                .post("/owner-dashboard/opening-hours/processNewRange")
                .param("bhid", rangeToUpdateId.toString())
                .param("startTime", newStartTime.toString())
                .param("endTime", newEndTime.toString())
                .param("isOpen", newIsOpen + "")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/owner-dashboard/opening-hours"));

        verify(businessHourService).findDayOfWeekById(rangeToUpdateId);
        verify(businessHourService).addOrUpdateBusinessHour(
            rangeToUpdateId, dayOfWeekToAdd,
            newStartTime, newEndTime, newIsOpen
        );

    }

    @DisplayName("Error On Show Edit Home - Non-Existent Business Info")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void showEditHome() throws Exception {

        when(businessInfoService.getBusinessInfo())
            .thenReturn(null);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get("/owner-dashboard/edit-home"))
            .andExpect(status().isOk())
            .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        assertNotNull(mav, "ModelAndView should not be null");
        assertViewName(mav, "error/404");

        verify(businessInfoService).getBusinessInfo();

    }

    @DisplayName("Process Edit Home Form - Validation Error Case")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void processEditHomeFormValidationErrorCase() throws Exception {

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .post("/owner-dashboard/edit-home/processEditHome")
                .param("businessName", "") // empty business name
                .param("description", "1234") // too short description
                .param("backgroundImage","")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors(
                "form",
                "businessName",
                "description",
                "backgroundImage"
            )).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        assertViewName(mav, "owner/edit-home");
        verify(businessInfoService, never()).updateBusinessInfo(
            anyString(), anyString(), nullable(MultipartFile.class)
        );
    }

    @DisplayName("Successfully Process Edit Home Form")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void successfullyProcessEditHomeForm() throws Exception {

        String businessName = "businessName";
        String description = "some description";
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
            "backgroundImage",
            "image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "some image content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/owner-dashboard/edit-home/processEditHome")
                .file(mockMultipartFile)
                .param("businessName", businessName)
                .param("description", description)
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/owner-dashboard/edit-home?editSuccess"));

        verify(businessInfoService).updateBusinessInfo(
            businessName, description, mockMultipartFile
        );

    }

    @DisplayName("Error On Remove Background Image - Already Removed Case")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void errorOnRemoveBackgroundImageAlreadyRemovedCase() throws Exception {

        doThrow(new RuntimeException()).when(businessInfoService).removeBackgroundImage();

        mockMvc.perform(MockMvcRequestBuilders
                .get("/owner-dashboard/edit-home/remove-background-image"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/owner-dashboard/edit-home?noImage"));

        verify(businessInfoService).removeBackgroundImage();
    }

    @DisplayName("Error On Remove Background Image - IO Exception Case")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void errorOnRemoveBackgroundImageIOExceptionCase() throws Exception {

        doThrow(IOException.class).when(businessInfoService).removeBackgroundImage();

        mockMvc.perform(MockMvcRequestBuilders
                .get("/owner-dashboard/edit-home/remove-background-image"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/owner-dashboard/edit-home?removeFailure"));

        verify(businessInfoService).removeBackgroundImage();
    }

    @DisplayName("Success On Remove Background Image")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void successOnRemoveBackgroundImage() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                .get("/owner-dashboard/edit-home/remove-background-image"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/owner-dashboard/edit-home?removeSuccess"));

        verify(businessInfoService).removeBackgroundImage();
    }

    @DisplayName("Show Add Owner")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void showAddOwner() throws Exception {

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get("/owner-dashboard/add-owner"))
            .andExpect(status().isOk())
            .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        assertNotNull(mav, "ModelAndView should not be null");
        assertModelAttributeValue(mav, "form", new UserDetailsForm());
        assertViewName(mav, "owner/add-owner");
    }

    @DisplayName("Process Add Owner Form - Basic Validation Error Case")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void processAddOwnerFormBasicValidationErrorCase() throws Exception {

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .post("/owner-dashboard/add-owner/processAddOwner")
                .param("username", "") // empty field case
                .param("password", "1234") // too short field case
                .param("confirmPassword",
                    "123456789123456789123456789123456789") // too long field case
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors(
                "form",
                "username",
                "password",
                "confirmPassword"
            )).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        assertViewName(mav, "owner/add-owner");
        verify(userService, never()).addNewOwnerUser(
            anyString(), anyString(), anyString()
        );
    }

    @DisplayName("Process Add Owner Form - Additional Validation Error Case")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void processAddOwnerFormAdditionalValidationErrorCase() throws Exception {

        List<String> possibleErrorCodes = List.of(
            "error.user.username.conflict",
            "error.user.password.confirmation.mismatch"
        );

        for (String possibleErrorCode : possibleErrorCodes) {
            doThrow(new RuntimeException(possibleErrorCode)).when(userService)
                .addNewOwnerUser(anyString(), anyString(), anyString());

            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                    .post("/owner-dashboard/add-owner/processAddOwner")
                    .param("username", "someUsername")
                    .param("password", "somePassword")
                    .param("confirmPassword", "someConfirmPassword")
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors()).andReturn();

            ModelAndView mav = mvcResult.getModelAndView();

            assertNotNull(mav, "ModelAndView should not be null");
            assertViewName(mav, "owner/add-owner");

            BindingResult bindingResult =
                (BindingResult) mav.getModel().get(BindingResult.MODEL_KEY_PREFIX + "form");

            assertTrue(bindingResult.getAllErrors().stream().anyMatch(
                    error -> possibleErrorCode.equals(error.getCode())),
                possibleErrorCode + " should be present in the errors");

        }

        verify(userService, times(possibleErrorCodes.size())).addNewOwnerUser(
            anyString(), anyString(), anyString()
        );

    }

    @DisplayName("Successfully Process Add Owner Form")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void successfullyProcessAddOwnerForm() throws Exception {

        String usernameToAdd = "newOwner";
        String passwordToAdd = "newPassword";

        mockMvc.perform(MockMvcRequestBuilders.post("/owner-dashboard/add-owner/processAddOwner")
                .param("username", usernameToAdd)
                .param("password", passwordToAdd)
                .param("confirmPassword", passwordToAdd)
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/owner-dashboard/add-owner?success"));

        verify(userService).addNewOwnerUser(
            usernameToAdd, passwordToAdd, passwordToAdd
        );

    }
}