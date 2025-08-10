package com.javaworkshop.business_scheduler.controller;

import com.javaworkshop.business_scheduler.config.DefaultInitializer;
import com.javaworkshop.business_scheduler.model.Customer;
import com.javaworkshop.business_scheduler.model.User;
import com.javaworkshop.business_scheduler.service.RegistrationService;
import com.javaworkshop.business_scheduler.service.UserService;
import org.junit.jupiter.api.*;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private DefaultInitializer defaultInitializer;

    private Customer customer;

    private User customerUser, ownerUser, defaultOwnerUser;

    @BeforeEach
    void setUp() {

        customerUser = new User(
            UUID.randomUUID(),
            "customerUser",
            "123456789",
            "ROLE_CUSTOMER",
            true
        );

        ownerUser = new User(
            UUID.randomUUID(),
            "ownerUser",
            "123456789",
            "ROLE_OWNER",
            true
        );

        defaultOwnerUser = new User(
            UUID.randomUUID(),
            "owner",
            "123456789",
            "ROLE_OWNER",
            true
        );

        customer = new Customer(
            UUID.randomUUID(),
            customerUser,
            "firstName",
            "lastName",
            "email@server.com",
            "0541111111"
        );
    }

    @DisplayName("Show Registration Form")
    @Test
    void showRegistrationForm() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/register"))
            .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        ModelAndViewAssert.assertViewName(mvcResult.getModelAndView(), "auth/register");
    }

    @DisplayName("Successfully Process Registration Form")
    @Test
    void successfullyProcessRegistrationForm() throws Exception {
        Customer customerToRegister = customer;
        String username = customerToRegister.getUser().getUsername();
        String password = customerToRegister.getUser().getPassword();
        String email =  customerToRegister.getEmail();
        String phone = customerToRegister.getPhone();
        String firstName = customerToRegister.getFirstName();
        String lastName = customerToRegister.getLastName();

        mockMvc.perform(MockMvcRequestBuilders.post("/processRegistrationForm")
                .param("customerDetailsForm.firstName", firstName)
                .param("customerDetailsForm.lastName", lastName)
                .param("customerDetailsForm.email", email)
                .param("customerDetailsForm.phoneNumber", phone)
                .param("userDetailsForm.username", username)
                .param("userDetailsForm.password", password)
                .param("userDetailsForm.confirmPassword", password)
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?registrationSuccess"));

        verify(registrationService).registerNewCustomer(
            username, password, password,
            email, phone, firstName, lastName
        );
    }

    @DisplayName("Process Registration Form - Basic Validation Error Case")
    @Test
    void ProcessRegistrationFormBasicValidationErrorCase() throws Exception {

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/processRegistrationForm")
                .param("customerDetailsForm.firstName", "") // empty field case
                .param("customerDetailsForm.lastName", "l") // too short field case
                .param("customerDetailsForm.email", "invalidEmail") // invalid email case
                .param("customerDetailsForm.phoneNumber", "123") // too short phone case
                .param("userDetailsForm.username", "") // empty username case
                .param("userDetailsForm.password", "1234") // too short password case
                .param("userDetailsForm.confirmPassword",
                    "123456789123456789123456789123456789") // too long confirm password case
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors(
                "form",
                "customerDetailsForm.firstName",
                "customerDetailsForm.lastName",
                "customerDetailsForm.email",
                "customerDetailsForm.phoneNumber",
                "userDetailsForm.username",
                "userDetailsForm.password",
                "userDetailsForm.confirmPassword"
            )).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        ModelAndViewAssert.assertViewName(mvcResult.getModelAndView(), "auth/register");

        verify(registrationService, times(0)).registerNewCustomer(
            anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString(), anyString()
        );
    }

    @DisplayName("Process Registration Form - Additional Validation Error Case")
    @Test
    void ProcessRegistrationFormAdditionalValidationErrorCase() throws Exception {


        List<String> possibleErrorCodes = List.of(
            "error.customer.email.conflict",
            "error.customer.phone.conflict",
            "error.customer.email.and.phone.conflict",
            "error.user.username.conflict",
            "error.user.password.confirmation.mismatch",
            "error.customer.username.conflict"
        );

        for (String possibleErrorCode : possibleErrorCodes) {
            doThrow(new RuntimeException(possibleErrorCode))
                .when(registrationService).registerNewCustomer(
                    anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString()
                );

            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/processRegistrationForm")
                    .param("customerDetailsForm.firstName", "someFirstName")
                    .param("customerDetailsForm.lastName", "someLastName")
                    .param("customerDetailsForm.email", "someEmail@server.com")
                    .param("customerDetailsForm.phoneNumber", "0541111111")
                    .param("userDetailsForm.username", "someUsername")
                    .param("userDetailsForm.password", "somePassword")
                    .param("userDetailsForm.confirmPassword","someConfirmationPassword")
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors()).andReturn();

            ModelAndView mav = mvcResult.getModelAndView();

            assertNotNull(mav, "ModelAndView should not be null");
            ModelAndViewAssert.assertViewName(mvcResult.getModelAndView(), "auth/register");

            BindingResult bindingResult =
                (BindingResult) mav.getModel().get(BindingResult.MODEL_KEY_PREFIX + "form");

            assertTrue(bindingResult.getAllErrors().stream().anyMatch(
                error -> possibleErrorCode.equals(error.getCode())),
                possibleErrorCode + " should be present in the errors");
        }

        verify(registrationService, times(possibleErrorCodes.size())).registerNewCustomer(
            anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString(), anyString()
        );

    }

    @DisplayName("Show Change Password Form")
    @WithMockUser(roles = {"CUSTOMER, OWNER"})
    @Test
    void showChangePasswordForm() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/change-password"))
            .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        ModelAndViewAssert.assertViewName(mvcResult.getModelAndView(), "auth/change-password");
    }

    @DisplayName("Unauthenticated Show Change Password Form")
    @Test
    void unauthenticatedShowChangePasswordForm() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/change-password"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @DisplayName("Process Change Password Form - Unauthenticated Case Without CSRF")
    @Test
    void processChangePasswordFormUnauthenticatedCaseWithoutCSRF() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/change-password/processChangePassword"))
            .andExpect(status().isForbidden());
    }

    @DisplayName("Process Change Password Form - Unauthenticated Case With CSRF")
    @Test
    void processChangePasswordFormUnauthenticatedCaseWithCSRF() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/change-password/processChangePassword").with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @DisplayName("Process Change Password Form - Basic Validation Error Case")
    @WithMockUser(username = "customerUser", roles = {"CUSTOMER"})
    @Test
    void processChangePasswordFormBasicValidationErrorCase() throws Exception {

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/change-password/processChangePassword")
                .param("oldPassword", "") // empty field case
                .param("newPassword", "1234") // too short field case
                .param("confirmNewPassword",
                    "123456789123456789123456789123456789") // too long field case
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors(
                "form",
                "oldPassword",
                "newPassword",
                "confirmNewPassword"
            )).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        ModelAndViewAssert.assertViewName(mvcResult.getModelAndView(), "auth/change-password");
        verify(userService, times(0)).changePassword(
            any(User.class), anyString(), anyString(), anyString()
        );
    }

    @DisplayName("Process Change Password Form - Additional Validation Error Case")
    @WithMockUser(username = "customerUser", roles = {"CUSTOMER"})
    @Test
    void processChangePasswordFormAdditionalValidationErrorCase() throws Exception {

        List<String> possibleErrorCodes = List.of(
            "error.user.old.password.incorrect",
            "error.user.password.confirmation.mismatch"
        );

        when(userService.findByUsername(customerUser.getUsername()))
            .thenReturn(customerUser);

        for (String possibleErrorCode : possibleErrorCodes) {
            doThrow(new RuntimeException(possibleErrorCode)).when(userService)
                .changePassword(eq(customerUser), anyString(), anyString(), anyString());

            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/change-password/processChangePassword")
                    .param("oldPassword", "someOldPassword")
                    .param("newPassword", "someNewPassword")
                    .param("confirmNewPassword", "someConfirmNewPassword")
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors()).andReturn();

            ModelAndView mav = mvcResult.getModelAndView();

            assertNotNull(mav, "ModelAndView should not be null");
            ModelAndViewAssert.assertViewName(mvcResult.getModelAndView(), "auth/change-password");

            BindingResult bindingResult =
                (BindingResult) mav.getModel().get(BindingResult.MODEL_KEY_PREFIX + "form");

            assertTrue(bindingResult.getAllErrors().stream().anyMatch(
                    error -> possibleErrorCode.equals(error.getCode())),
                possibleErrorCode + " should be present in the errors");

        }

        verify(userService, times(possibleErrorCodes.size()))
            .findByUsername(customerUser.getUsername());
        verify(userService, times(possibleErrorCodes.size())).changePassword(
            eq(customerUser), anyString(),
            anyString(), anyString()
        );

    }

    @DisplayName("Successfully Process Change Password Form")
    @WithMockUser(username = "customerUser", roles = {"CUSTOMER"})
    @Test
    void successfullyProcessChangePasswordForm() throws Exception {

        String oldPassword = customerUser.getPassword();
        String newPassword = "1234567891";

        when(userService.findByUsername(customerUser.getUsername()))
            .thenReturn(customerUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/change-password/processChangePassword")
                .param("oldPassword", oldPassword)
                .param("newPassword", newPassword)
                .param("confirmNewPassword", newPassword)
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/change-password?success"));

        verify(userService).findByUsername(customerUser.getUsername());
        verify(userService).changePassword(
            customerUser, oldPassword,
            newPassword, newPassword
        );

    }

    @DisplayName("Process Delete Account - Unauthenticated Case")
    @Test
    void processDeleteAccountUnauthenticatedCase() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/delete-account"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @DisplayName("Process Delete Account - Customer Case")
    @WithMockUser(username = "customerUser", roles = {"CUSTOMER"})
    @Test
    void processDeleteAccountCustomerCase() throws Exception {

        when(userService.findByUsername(customerUser.getUsername())).thenReturn(customerUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/delete-account"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?accountDeleted"));

        verify(userService).findByUsername(customerUser.getUsername());
        verify(userService).deleteById(customerUser.getId());

    }

    @DisplayName("Process Delete Account - Regular Owner Case")
    @WithMockUser(username = "ownerUser", roles = {"OWNER"})
    @Test
    void processDeleteAccountRegularOwnerCase() throws Exception {

        when(userService.findByUsername(ownerUser.getUsername())).thenReturn(ownerUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/delete-account"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?accountDeleted"));

        verify(userService).findByUsername(ownerUser.getUsername());
        verify(userService).deleteById(ownerUser.getId());

    }

    @DisplayName("Process Delete Account - Default Owner Case")
    @WithMockUser(username = "owner", roles = {"OWNER"})
    @Test
    void processDeleteAccountDefaultOwnerCase() throws Exception {

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/delete-account"))
            .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        ModelAndViewAssert.assertViewName(mvcResult.getModelAndView(), "error/404");

        verify(userService, times(0)).findByUsername(defaultOwnerUser.getUsername());
        verify(userService, times(0)).deleteById(defaultOwnerUser.getId());

    }

    @DisplayName("Show Login Form")
    @Test
    void showLoginForm() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/login"))
            .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        assertNotNull(mav, "ModelAndView should not be null");
        ModelAndViewAssert.assertViewName(mvcResult.getModelAndView(), "auth/login");

    }
}