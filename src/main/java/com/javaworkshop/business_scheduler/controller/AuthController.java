package com.javaworkshop.business_scheduler.controller;

import com.javaworkshop.business_scheduler.dto.ChangePasswordForm;
import com.javaworkshop.business_scheduler.dto.RegistrationForm;
import com.javaworkshop.business_scheduler.model.User;
import com.javaworkshop.business_scheduler.service.RegistrationService;
import com.javaworkshop.business_scheduler.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

// This class handles user authentication-related actions.
@Controller
public class AuthController {

    private RegistrationService registrationService;
    private UserService userService;

    @Autowired
    public AuthController(RegistrationService registrationService, UserService userService) {
        this.registrationService = registrationService;
        this.userService = userService;
    }

    // shows the registration form
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "auth/register";
    }

    // processes the registration form given by the user
    @PostMapping("/processRegistrationForm")
    public String processRegistrationForm(@ModelAttribute("form") @Valid RegistrationForm form,
                                          BindingResult result) {

        if (!result.hasErrors()) {
            String customerEmail = form.getCustomerDetailsForm().getEmail();
            String customerPhone = form.getCustomerDetailsForm().getPhoneNumber();
            String customerFirstName = form.getCustomerDetailsForm().getFirstName();
            String customerLastName = form.getCustomerDetailsForm().getLastName();
            String username = form.getUserDetailsForm().getUsername();
            String password = form.getUserDetailsForm().getPassword();
            String confirmPassword = form.getUserDetailsForm().getConfirmPassword();
            try {
                registrationService.registerNewCustomer(username, password, confirmPassword,
                        customerEmail, customerPhone, customerFirstName, customerLastName);
            } catch (RuntimeException e) {
                result.reject(e.getMessage());
            }
        }

        if (result.hasErrors()) { // if there is any validation error (input/after registration)
            return "auth/register";
        }

        return "redirect:/login?registrationSuccess";
    }

    // shows the change password form
    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("form", new ChangePasswordForm());
        return "auth/change-password";
    }

    // processes the change password form given by the user
    @PostMapping("/change-password/processChangePassword")
    public String processChangePasswordForm(@ModelAttribute("form") @Valid ChangePasswordForm form,
                                            BindingResult result,
                                            Authentication authentication) {
        if (!result.hasErrors()) {
            try {
                User user = userService.findByUsername(authentication.getName());
                String oldPassword = form.getOldPassword();
                String newPassword = form.getNewPassword();
                String confirmNewPassword = form.getConfirmNewPassword();
                userService.changePassword(user, oldPassword, newPassword, confirmNewPassword);
            } catch (RuntimeException e) {
                result.reject(e.getMessage());
            }
        }

        if (result.hasErrors()) {
            return "auth/change-password";
        }

        return "redirect:/change-password?success";
    }

    // processes the deletion of the user account
    @GetMapping("/delete-account")
    public String processDeleteAccount(Authentication authentication) {
        String username = authentication.getName();
        if (username.equals("owner")) {
            return "error/404"; // prevents deletion of default owner account
        }
        User user = userService.findByUsername(username);
        userService.deleteById(user.getId());
        return "redirect:/login?accountDeleted";
    }

    // shows the login form
    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

}
