package com.javaworkshop.business_scheduler.controller;

import com.javaworkshop.business_scheduler.dto.AppointmentTimeForm;
import com.javaworkshop.business_scheduler.dto.BookAppointmentForm;
import com.javaworkshop.business_scheduler.dto.CustomerDetailsForm;
import com.javaworkshop.business_scheduler.model.Appointment;
import com.javaworkshop.business_scheduler.model.BusinessInfo;
import com.javaworkshop.business_scheduler.model.Customer;
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

    private final int PAGE_SIZE = 3;
    private ServiceService serviceService;
    private CustomerService customerService;
    private AppointmentService appointmentService;
    private BookingService bookingService;
    private BusinessInfoService businessInfoService;

    @Autowired
    public GeneralController(ServiceService serviceService,
                             CustomerService customerService,
                             AppointmentService appointmentService,
                             BookingService bookingService,
                             BusinessInfoService businessInfoService) {
            this.serviceService = serviceService;
            this.customerService = customerService;
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

        Page<Service> servicePage = serviceService.getServicePage(page, PAGE_SIZE);
        if (page < 0 || page >= servicePage.getTotalPages()) {
            return "error/404"; // if the page number is invalid return 404 error page
        }
        model.addAttribute("servicePage", servicePage);
        model.addAttribute("currentPage", page);

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
        try {
            selectedService = serviceService.findById(serviceId);
            if (appointmentId != null) { // this is a rescheduling request
                appointment = appointmentService.findById(appointmentId);
                form.setCustomerDetailsForm( // sets the customer details form with the existing appointment details
                        new CustomerDetailsForm(
                                appointment.getCustomer().getFirstName(),
                                appointment.getCustomer().getLastName(),
                                appointment.getCustomer().getEmail(),
                                appointment.getCustomer().getPhone()
                        )
                );
                form.setAppointmentTimeForm( // sets the appointment time form with the existing appointment details
                        new AppointmentTimeForm(
                                appointment.getStartTime().toLocalDate(),
                                appointment.getStartTime().toLocalTime()
                        )
                );
            }
        } catch (RuntimeException e) {
            return "error/404"; // if the service or appointment does not exist return 404 error page
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
        model.addAttribute("selectedService", service);

        // STAGE 1: validate the customer details form and get the customer object
        Customer customer = null;
        if (authentication != null) { // if the user is logged in
            customer = customerService.findByUsername(authentication.getName());
        }
        else {
            if (!result.hasFieldErrors("customerDetailsForm.*")) { // if there are no errors in the customer details form
                String customerEmail = form.getCustomerDetailsForm().getEmail();
                String customerPhone = form.getCustomerDetailsForm().getPhoneNumber();
                String customerFirstName = form.getCustomerDetailsForm().getFirstName();
                String customerLastName = form.getCustomerDetailsForm().getLastName();
                Customer exisitingCustomer = customerService.findByEmailAndPhone(customerEmail, customerPhone);
                try {
                    customer = customerService.validateCustomer(exisitingCustomer, customerEmail, customerPhone,
                            customerFirstName, customerLastName, null, false
                    );
                } catch (RuntimeException e) {
                    result.rejectValue("customerDetailsForm", e.getMessage());
                }

            }
            // validate customer details (input/after validation)
            if (result.hasFieldErrors("customerDetailsForm.*") || result.hasFieldErrors("customerDetailsForm")) {
                return "general/book";
            }
        }

        // STAGE 2: validate the appointment time form and book the appointment
        if (!result.hasFieldErrors("appointmentTimeForm.*")) { // if there are no errors in the appointment time form
            LocalDate appointmentDate = form.getAppointmentTimeForm().getAppointmentDate();
            LocalTime appointmentTime = form.getAppointmentTimeForm().getAppointmentTime();
            LocalDateTime startTime = LocalDateTime.of(appointmentDate, appointmentTime);
            LocalDateTime endTime = startTime.plusMinutes(service.getDuration());

            try {
                Appointment bookedAppointment = bookingService.bookAppointment(customer, service, appointmentId, startTime, endTime);
                boolean isRescheduling = appointmentId != null;
                model.addAttribute("bookedAppointment", bookedAppointment);
                model.addAttribute("isRescheduling", isRescheduling);
            } catch (RuntimeException e) {
                result.rejectValue("appointmentTimeForm.appointmentTime", e.getMessage());
            }
        }

        // validate appointment time (input/after booking)
        if (result.hasFieldErrors("appointmentTimeForm.*")) {
            return "general/book";
        }

        return "general/success-booking";
    }

}
