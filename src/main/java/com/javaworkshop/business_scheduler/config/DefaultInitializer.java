package com.javaworkshop.business_scheduler.config;

import com.javaworkshop.business_scheduler.model.BusinessHour;
import com.javaworkshop.business_scheduler.model.BusinessInfo;
import com.javaworkshop.business_scheduler.model.User;
import com.javaworkshop.business_scheduler.service.BusinessHourService;
import com.javaworkshop.business_scheduler.service.BusinessInfoService;
import com.javaworkshop.business_scheduler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

// This class initializes a default owner user, default business hours, and default business information.
@Component
public class DefaultInitializer implements CommandLineRunner {

    private final UserService userService;
    private final BusinessHourService businessHourService;
    private final BusinessInfoService businessInfoService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DefaultInitializer(UserService userService,
                              BusinessHourService businessHourService,
                              BusinessInfoService businessInfoService,
                              PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.businessHourService = businessHourService;
        this.businessInfoService = businessInfoService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initializeDefaultOwnerUser();
        initializeDefaultBusinessHours();
        initializeDefaultBusinessInfo();
    }



    private void initializeDefaultOwnerUser() {
        String username = "owner";
        if (userService.findByUsername(username) == null) {
            User owner = new User();
            owner.setUsername(username);
            owner.setPassword(passwordEncoder.encode("Owner1234"));
            owner.setRole("ROLE_OWNER");
            owner.setEnabled(true);
            userService.save(owner);
        }
    }

    private void initializeDefaultBusinessHours() {
        if (businessHourService.count() == 0) {
            for (int day = 0; day <= 6; day++) {
                BusinessHour bh = new BusinessHour();
                bh.setDayOfWeek((byte) day);
                bh.setStartTime(LocalTime.of(9, 0));
                bh.setEndTime(LocalTime.of(17, 0));
                bh.setIsOpen(true);
                businessHourService.save(bh);
            }
        }
    }

    private void initializeDefaultBusinessInfo() {
        if (!businessInfoService.isThereBusinessInfo()) {
            BusinessInfo businessInfo = new BusinessInfo();
            businessInfo.setId(1);
            businessInfo.setName("Default Business");
            businessInfo.setDescription("This is a default business description.");
            businessInfoService.save(businessInfo);
        }
    }
}
