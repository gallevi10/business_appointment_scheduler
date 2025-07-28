package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.BusinessHour;

import java.time.LocalTime;
import java.util.List;

// This interface defines the contract for business hour-related operations in the business scheduler application.
public interface BusinessHourService {

    List<BusinessHour> findAll();

    BusinessHour findById(long id);

    BusinessHour save(BusinessHour businessHour);

    void deleteById(long id);

    List<BusinessHour> findAllRangesByDayOfWeek(byte dayOfWeek);

    // adds or updates a business hour entry
    void addOrUpdateBusinessHour(Long businessHourId,
                                 byte dayOfWeek,
                                 LocalTime startTime,
                                 LocalTime endTime,
                                 boolean isOpen);

    Byte findDayOfWeekById(long id);

    long count();

}
