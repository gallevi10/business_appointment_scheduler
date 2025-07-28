package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.dto.OpeningHour;
import com.javaworkshop.business_scheduler.model.BusinessHour;

import java.time.LocalTime;
import java.util.List;

public interface BusinessHourService {

    List<BusinessHour> findAll();

    BusinessHour findById(long id);

    BusinessHour save(BusinessHour businessHour);

    void deleteById(long id);

    List<BusinessHour> findAllRangesByDayOfWeek(byte dayOfWeek);

    List<OpeningHour> getDayBusinessHours(byte dayOfWeek);

    void addOrUpdateBusinessHour(Long businessHourId,
                                 byte dayOfWeek,
                                 LocalTime startTime,
                                 LocalTime endTime,
                                 boolean isOpen);

    Byte findDayOfWeekById(long id);

    long count();

}
