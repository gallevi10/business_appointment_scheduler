package com.javaworkshop.business_scheduler.controller;

import com.javaworkshop.business_scheduler.dto.*;
import com.javaworkshop.business_scheduler.model.Appointment;
import com.javaworkshop.business_scheduler.model.BusinessHour;
import com.javaworkshop.business_scheduler.model.BusinessInfo;
import com.javaworkshop.business_scheduler.model.Service;
import com.javaworkshop.business_scheduler.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

// This class handles the owner dashboard functionalities.
@Controller
@RequestMapping("/owner-dashboard")
public class OwnerController {

    private final BusinessHourService businessHourService;
    private final AppointmentService appointmentService;
    private final ServiceService serviceService;
    private final BusinessInfoService businessInfoService;
    private final UserService userService;

    @Autowired
    public OwnerController(BusinessHourService businessHourService,
                           AppointmentService appointmentService,
                           ServiceService serviceService,
                           BusinessInfoService businessInfoService,
                           UserService userService) {
        this.businessHourService = businessHourService;
        this.appointmentService = appointmentService;
        this.serviceService = serviceService;
        this.businessInfoService = businessInfoService;
        this.userService = userService;
    }

    // shows all appointments for the owner
    @GetMapping("/appointments")
    public String showAppointments(Model model) {
        List<Appointment> customerAppointments = appointmentService.findAll();
        List<DashboardAppointment> dashboardAppointments =
                DashboardAppointment.fromAppointmentList(customerAppointments);

        model.addAttribute("appointments", dashboardAppointments);

        return "owner/appointments";
    }

    // cancels an appointment
    @GetMapping("/appointments/cancel")
    public String cancelAppointment(@RequestParam("aid") UUID appointmentId) {
        appointmentService.deleteById(appointmentId);
        return "redirect:/owner-dashboard/appointments";
    }

    // handles exporting appointments to XML
    @GetMapping("/appointments/export-to-xml")
    public ResponseEntity<ByteArrayResource> exportAppointmentsToXml(@RequestParam("active") boolean activeAppointmentsOnly) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            appointmentService.exportAppointmentsToXML(outputStream, activeAppointmentsOnly);
            byte[] data = outputStream.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(data);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=appointments.xml")
                    .contentType(MediaType.APPLICATION_XML)
                    .contentLength(data.length)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // shows the service manager page
    @GetMapping("/service-manager")
    public String showServiceManager(Model model) {
        model.addAttribute("services", serviceService.findAll());
        return "owner/service-manager";
    }

    // deletes a service
    @GetMapping("/service-manager/delete")
    public String deleteService(@RequestParam("sid") UUID serviceId) {
        serviceService.deleteById(serviceId);
        return "redirect:/owner-dashboard/service-manager";
    }

    // changes the availability of a service
    @GetMapping("/service-manager/change-availability")
    public String changeServiceAvailability(@RequestParam("sid") UUID serviceId) {
        Service service = serviceService.findById(serviceId);
        if (service == null) {
            return "error/404"; // if the service does not exist return 404 error page
        }
        service.setIsActive(!service.getIsActive());
        serviceService.save(service);

        return "redirect:/owner-dashboard/service-manager";
    }

    // shows the add or update service form
    @GetMapping("/service-manager/add-service")
    public String showAddService(Model model) {
        model.addAttribute("form", new NewServiceForm());
        return "owner/add-service";
    }

    // shows the update service form with existing service details
    @GetMapping("/service-manager/update-service")
    public String showUpdateService(@RequestParam("sid") UUID serviceId, Model model) {
        Service service = serviceService.findById(serviceId);
        if (service == null) {
            return "error/404"; // if the service does not exist return 404 error page
        }
        NewServiceForm form = new NewServiceForm();
        form.setServiceName(service.getServiceName());
        form.setPrice(service.getPrice());
        form.setDuration(service.getDuration());
        form.setImagePath(service.getImagePath());
        model.addAttribute("form", form);
        return "owner/add-service";
    }

    // removes the service image
    @GetMapping("/service-manager/update-service/remove-image")
    public String removeServiceImage(@RequestParam("sid") UUID serviceId) {
        try {
            serviceService.removeServiceImage(serviceId);
        } catch (IOException e) {
            return "redirect:/owner-dashboard/service-manager/update-service?sid=" + serviceId + "&removeFailure";
        }
        return "redirect:/owner-dashboard/service-manager/update-service?sid=" + serviceId + "&removeSuccess";
    }

    // processes the adding or updating of a service
    @PostMapping("service-manager/add-service/processAddingService")
    public String processAddingService(@RequestParam(value = "sid", required = false) UUID serviceId,
                                       @ModelAttribute("form") @Valid NewServiceForm form,
                                       BindingResult result) {
        if (!result.hasErrors()) {
            Service existingService = null;
            if (serviceId != null) { // if serviceId is provided we are updating an existing service
                existingService = serviceService.findById(serviceId);
                if (existingService == null) {
                    return "error/404"; // if the service does not exist return 404 error page
                }
            }
            String serviceName = form.getServiceName();
            BigDecimal price = form.getPrice();
            int duration = form.getDuration();
            MultipartFile serviceImage = form.getServiceImage();
            try {
                serviceService.addOrUpdateService(existingService, serviceName, price, duration, serviceImage);
            } catch (RuntimeException e) {
                result.reject(e.getMessage());
            }

        }

        if (result.hasErrors()) {
            return "owner/add-service";
        }

        return "redirect:/owner-dashboard/service-manager";
    }

    // shows the opening hours management page
    @GetMapping("/opening-hours")
    public String showOpeningHours(Model model) {
        List<BusinessHour> businessHours = businessHourService.findAll();
        List<OpeningHour> openingHours = OpeningHour.fromBusinessHours(businessHours);
        model.addAttribute("openingHours", openingHours);
        return "owner/opening-hours";
    }

    // shows the add opening hour form
    @GetMapping("/opening-hours/add-range")
    public String showAddOpeningHour(@RequestParam("dow") byte dayOfWeek, Model model) {
        NewRangeForm form = new NewRangeForm();
        form.setDayOfWeek(dayOfWeek);
        model.addAttribute("form", form);
        return "owner/add-range";
    }

    // shows the edit opening hour form with existing details
    @GetMapping("/opening-hours/edit-range")
    public String showEditOpeningHour(@RequestParam("bhid") UUID businessHourId, Model model) {
        BusinessHour businessHour = businessHourService.findById(businessHourId);
        if (businessHour == null) {
            return "error/404"; // if the business hour does not exist return 404 error page
        }
        NewRangeForm form = new NewRangeForm(
                businessHour.getDayOfWeek(),
                businessHour.getStartTime(),
                businessHour.getEndTime(),
                businessHour.getIsOpen()
        );
        model.addAttribute("form", form);
        return "owner/add-range";
    }

    // deletes an opening hour
    @GetMapping("/opening-hours/delete-range")
    public String deleteOpeningHour(@RequestParam("bhid") UUID businessHourId) {
        businessHourService.deleteById(businessHourId);
        return "redirect:/owner-dashboard/opening-hours";
    }

    // processes the adding or updating of an opening hour
    @PostMapping("/opening-hours/processNewRange")
    public String processNewRange(@RequestParam(value = "bhid", required = false) UUID businessHourId,
                                  @ModelAttribute("form") @Valid NewRangeForm form,
                                  BindingResult result) {
        if (!result.hasErrors()) {
            try {
                byte dayOfWeek = businessHourId != null ?
                        businessHourService.findDayOfWeekById(businessHourId) : form.getDayOfWeek();
                LocalTime startTime = form.getStartTime();
                LocalTime endTime = form.getEndTime();
                boolean isOpen = form.getIsOpen();
                businessHourService.addOrUpdateBusinessHour(businessHourId, dayOfWeek, startTime, endTime, isOpen);
            } catch (RuntimeException e) {
                result.rejectValue("startTime", e.getMessage());
            }
        }

        if (result.hasErrors()) {
            return "owner/add-range";
        }

        return "redirect:/owner-dashboard/opening-hours";
    }

    // shows the edit home page
    @GetMapping("/edit-home")
    public String showEditHome(Model model) {
        BusinessInfo businessInfo = businessInfoService.getBusinessInfo();
        if (businessInfo == null) {
            return "error/404"; // if the business info does not exist return 404 error page
        }
        EditHomeForm form = EditHomeForm.fromBusinessInfo(businessInfo);
        model.addAttribute("form", form);
        return "owner/edit-home";
    }

    // processes the edit home form
    @PostMapping("/edit-home/processEditHome")
    public String processEditHome(@ModelAttribute("form") @Valid EditHomeForm form,
                                  BindingResult result) {
        if (!result.hasErrors()) {
            try {
                String businessName = form.getBusinessName();
                String description = form.getDescription();
                MultipartFile backgroundImage = form.getBackgroundImage();
                businessInfoService.updateBusinessInfo(
                        businessName,
                        description,
                        backgroundImage
                );
            } catch (RuntimeException e) {
                result.rejectValue("backgroundImage", e.getMessage());
            }
        }

        if (result.hasErrors()) {
            return "owner/edit-home";
        }

        return "redirect:/owner-dashboard/edit-home?editSuccess";
    }

    // removes the background image
    @GetMapping("/edit-home/remove-background-image")
    public String removeBackgroundImage() {
        try {
            businessInfoService.removeBackgroundImage();
        } catch (IOException e) {
            return "redirect:/owner-dashboard/edit-home?removeFailure";
        }
        return "redirect:/owner-dashboard/edit-home?removeSuccess";
    }

    // shows the add owner form
    @GetMapping("add-owner")
    public String showAddOwner(Model model) {
        model.addAttribute("form", new UserDetailsForm());
        return "owner/add-owner";
    }

    // processes the add owner form
    @PostMapping("add-owner/processAddOwner")
    public String processAddOwner(@ModelAttribute("form") @Valid UserDetailsForm form,
                                  BindingResult result) {
        if (!result.hasErrors()) {
            String username = form.getUsername();
            String password = form.getPassword();
            String confirmPassword = form.getConfirmPassword();
            try {
                userService.addNewOwnerUser(username, password, confirmPassword);
            } catch (RuntimeException e) {
                result.reject(e.getMessage());
            }
        }

        if (result.hasErrors()) {
            return "owner/add-owner";
        }

        return "redirect:/owner-dashboard/add-owner?success";
    }
}
