package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.BusinessHour;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

// This interface defines the contract for business hour-related operations in the business scheduler application.
public interface BusinessHourService {

    List<BusinessHour> findAll();

    BusinessHour findById(UUID id);

    BusinessHour save(BusinessHour businessHour);

    void deleteById(UUID id);

    List<BusinessHour> findAllRangesByDayOfWeek(byte dayOfWeek);

    // adds or updates a business hour entry
    void addOrUpdateBusinessHour(UUID businessHourId,
                                 byte dayOfWeek,
                                 LocalTime startTime,
                                 LocalTime endTime,
                                 boolean isOpen);

    Byte findDayOfWeekById(UUID id);

    long count();

}
