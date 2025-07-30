package com.javaworkshop.business_scheduler.controller;

import com.javaworkshop.business_scheduler.model.BusinessHour;
import com.javaworkshop.business_scheduler.model.Service;
import com.javaworkshop.business_scheduler.service.AppointmentService;
import com.javaworkshop.business_scheduler.service.BusinessHourService;
import com.javaworkshop.business_scheduler.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// This class handles REST API requests related to business operations.
@RestController
@RequestMapping("/api")
public class BusinessRestController {

    private final ServiceService serviceService;
    private final AppointmentService appointmentService;
    private final BusinessHourService businessHourService;

    @Autowired
    public BusinessRestController(ServiceService serviceService,
                                  AppointmentService appointmentService,
                                  BusinessHourService businessHourService) {
        this.serviceService = serviceService;
        this.appointmentService = appointmentService;
        this.businessHourService = businessHourService;
    }

    // returns a list of available time slots for a given service on a specific date
    @GetMapping("general/available-slots")
    public List<LocalTime> getAvailableSlots(
            @RequestParam("sid") UUID serviceId,
            @RequestParam("d") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate date) {
        Service service = serviceService.findById(serviceId);
        if (service == null) {
            return new ArrayList<>(); // return empty list if service not found
        }
        byte dayOfWeek = (byte) (date.getDayOfWeek().getValue() % 7); // converts to 0-6 range for Sunday-Saturday
        List<BusinessHour> businessHours = businessHourService.findAllRangesByDayOfWeek(dayOfWeek);
        return appointmentService.getAvailableSlots(service, date, businessHours);


    }

}
