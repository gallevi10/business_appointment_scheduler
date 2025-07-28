package com.javaworkshop.business_scheduler.controller;

import com.javaworkshop.business_scheduler.dto.CustomerDetailsForm;
import com.javaworkshop.business_scheduler.dto.DashboardAppointment;
import com.javaworkshop.business_scheduler.model.Appointment;
import com.javaworkshop.business_scheduler.model.Customer;
import com.javaworkshop.business_scheduler.service.AppointmentService;
import com.javaworkshop.business_scheduler.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// This class handles the customer dashboard functionalities.
@Controller
@RequestMapping("/customer-dashboard")
public class CustomerController {

    CustomerService customerService;
    AppointmentService appointmentService;

    @Autowired
    public CustomerController(CustomerService customerService, AppointmentService appointmentService) {
        this.customerService = customerService;
        this.appointmentService = appointmentService;
    }

    // shows the customer appointments
    @GetMapping("/appointments")
    public String showAppointments(Model model, Authentication authentication) {
        UUID customerId = customerService.findIdByUsername(authentication.getName());
        List<Appointment> customerAppointments = appointmentService.findActiveAppointmentsByCustomerId(customerId);
        List<DashboardAppointment> dashboardAppointments =
                DashboardAppointment.fromAppointmentList(customerAppointments);

        model.addAttribute("appointments", dashboardAppointments);

        return "customer/appointments";
    }

    // cancels an appointment
    @GetMapping("/appointments/cancel")
    public String cancelAppointment(@RequestParam("aid") UUID appointmentId,
                                    Authentication authentication) {

        UUID customerId = customerService.findIdByUsername(authentication.getName());
        Appointment appointment = appointmentService.findById(appointmentId);
        if (appointment == null) {
            return "error/404"; // if the appointment does not exist return 404 error page
        }

        // cancel the appointment only if it belongs to the customer
        if (appointment.getCustomer().getId().equals(customerId)) {
            appointmentService.deleteById(appointmentId);
        }

        return "redirect:/customer-dashboard/appointments";
    }

    // shows the customer profile
    @GetMapping("/profile")
    public String showProfile(Model model, Authentication authentication) {
        Customer customer = customerService.findByUsername(authentication.getName());
        CustomerDetailsForm customerDetailsForm = new CustomerDetailsForm(customer);
        model.addAttribute("form", customerDetailsForm);
        return "customer/profile";
    }

    // processes the change profile form given by the customer
    @PostMapping("/profile/processChangeProfile")
    public String processChangeProfile(@ModelAttribute("form") @Valid CustomerDetailsForm form,
                                       BindingResult result,
                                       Authentication authentication) {
        Customer customer = customerService.findByUsername(authentication.getName());
        if (!result.hasErrors()) {
            try {
                String email = form.getEmail();
                String phone = form.getPhoneNumber();
                String firstName = form.getFirstName();
                String lastName = form.getLastName();
                customerService.updateCustomerDetails(customer, email, phone, firstName, lastName);
            } catch (RuntimeException e) {
                result.reject(e.getMessage());
            }
        }

        if (result.hasErrors()) {
            return "customer/profile";
        }

        return "redirect:/customer-dashboard/profile?success";
    }


}
