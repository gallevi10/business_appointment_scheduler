package com.javaworkshop.business_scheduler.controller;

import com.javaworkshop.business_scheduler.dto.AppointmentTimeForm;
import com.javaworkshop.business_scheduler.dto.BookAppointmentForm;
import com.javaworkshop.business_scheduler.model.Appointment;
import com.javaworkshop.business_scheduler.model.BusinessInfo;
import com.javaworkshop.business_scheduler.model.Service;
import com.javaworkshop.business_scheduler.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;


@Controller
public class GeneralController {

    private static final int PAGE_SIZE = 3;
    private final ServiceService serviceService;
    private final AppointmentService appointmentService;
    private final BookingService bookingService;
    private final BusinessInfoService businessInfoService;

    @Autowired
    public GeneralController(ServiceService serviceService,
                             AppointmentService appointmentService,
                             BookingService bookingService,
                             BusinessInfoService businessInfoService) {
            this.serviceService = serviceService;
            this.appointmentService = appointmentService;
            this.bookingService = bookingService;
            this.businessInfoService = businessInfoService;
    }

    // shows the index page with the business information
    @GetMapping("/")
    public String showIndex(Model model) {

        BusinessInfo businessInfo = businessInfoService.getBusinessInfo();
        model.addAttribute("businessInfo", businessInfo);

        return "general/index";
    }

    // shows the pick service page with a paginated list of services
    @GetMapping("/pick-service")
    public String showPickService(@RequestParam(value = "p", defaultValue = "0") int page,
                                  Model model) {

        try {
            Page<Service> servicePage = serviceService.getServicePage(page, PAGE_SIZE);
            model.addAttribute("servicePage", servicePage);
            model.addAttribute("currentPage", page);
        } catch (RuntimeException e) {
            return "error/404"; // if there is an error fetching services, return 404 error page
        }

        return "general/services";
    }

    // shows the book appointment page for a specific service
    @GetMapping("/book")
    public String showBookService(@RequestParam("sid") UUID serviceId,
                                  @RequestParam(value = "aid", required = false) UUID appointmentId,
                                  Model model) {

        Appointment appointment;
        Service selectedService;
        BookAppointmentForm form = new BookAppointmentForm();
        selectedService = serviceService.findById(serviceId);
        if (selectedService == null) {
            return "error/404"; // if the service does not exist return 404 error page
        }
        if (appointmentId != null) { // this is a rescheduling request
            appointment = appointmentService.findById(appointmentId);
            if (appointment == null || !appointment.getService().getId().equals(serviceId)) {
                return "error/404"; // if the appointment does not exist or does not belong to the service, return 404 error page
            }
            form.setAppointmentTimeForm( // sets the appointment time form with the existing appointment details
                    new AppointmentTimeForm(
                            appointment.getStartTime().toLocalDate(),
                            appointment.getStartTime().toLocalTime()
                    )
            );
        }

        model.addAttribute("form", form);
        model.addAttribute("selectedService", selectedService);

        return "general/book";
    }

    // processes the booking of an appointment
    @PostMapping("book/processBooking")
    public String processBooking(@RequestParam("sid") UUID serviceId,
                                 @RequestParam(value = "aid", required = false) UUID appointmentId,
                                 @ModelAttribute("form") @Valid BookAppointmentForm form,
                                 BindingResult result,
                                 Model model,
                                 Authentication authentication) {

        Service service = serviceService.findById(serviceId);
        if (service == null) {
            return "error/404"; // if the service does not exist return 404 error page
        }
        model.addAttribute("selectedService", service);
        String username = authentication != null ? authentication.getName() : null;

        if (!result.hasErrors()) { // if there are no validation errors
            String customerEmail = null, customerPhone = null, customerFirstName = null, customerLastName = null;
            if (form.getCustomerDetailsForm() != null) { // if it's a guest booking
                customerEmail = form.getCustomerDetailsForm().getEmail();
                customerPhone = form.getCustomerDetailsForm().getPhoneNumber();
                customerFirstName = form.getCustomerDetailsForm().getFirstName();
                customerLastName = form.getCustomerDetailsForm().getLastName();
            }
            LocalDate appointmentDate = form.getAppointmentTimeForm().getAppointmentDate();
            LocalTime appointmentTime = form.getAppointmentTimeForm().getAppointmentTime();
            LocalDateTime startTime = LocalDateTime.of(appointmentDate, appointmentTime);
            LocalDateTime endTime = startTime.plusMinutes(service.getDuration());

            try {
                Appointment bookedAppointment = bookingService.bookAppointment(
                        customerFirstName, customerLastName, customerEmail, customerPhone,
                        username, service, appointmentId, startTime, endTime
                );
                boolean isRescheduling = appointmentId != null;
                model.addAttribute("bookedAppointment", bookedAppointment);
                model.addAttribute("isRescheduling", isRescheduling);
            } catch (RuntimeException e) {
                result.reject(e.getMessage());
            }
        }

        if (result.hasErrors()) { // if there is any validation error
            return "general/book";
        }

        return "general/success-booking";
    }

}
