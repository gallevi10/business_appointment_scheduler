package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.BusinessHour;
import com.javaworkshop.business_scheduler.repository.BusinessHourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// This class implements the BusinessHourService interface providing methods
// for managing business hours in the business scheduler application.
@Service
public class BusinessHourServiceImpl implements BusinessHourService{

    private final BusinessHourRepository businessHourRepository;

    @Autowired
    public BusinessHourServiceImpl(BusinessHourRepository businessHourRepository) {
        this.businessHourRepository = businessHourRepository;
    }

    @Override
    public List<BusinessHour> findAll() {
        return businessHourRepository.findAllByOrderByDayOfWeekAscStartTimeAsc();
    }

    @Override
    public BusinessHour findById(UUID id) {
        Optional<BusinessHour> businessHour = businessHourRepository.findById(id);
        return businessHour.orElse(null);
    }

    @Override
    public BusinessHour save(BusinessHour businessHour) {
        return businessHourRepository.save(businessHour);
    }

    @Override
    public void deleteById(UUID id) {
        businessHourRepository.deleteById(id);
    }

    @Override
    public List<BusinessHour> findAllRangesByDayOfWeek(byte dayOfWeek) {
        return businessHourRepository.findByDayOfWeekOrderByStartTime(dayOfWeek);
    }

    @Override
    public synchronized void addOrUpdateBusinessHour(UUID businessHourId,
                                        byte dayOfWeek,
                                        LocalTime startTime,
                                        LocalTime endTime,
                                        boolean isOpen) {

        // validates inputs
        if (startTime.isAfter(endTime)) {
            throw new RuntimeException("error.business.hour.start.after.end");
        }
        if (businessHourRepository.isOverlapping(businessHourId, dayOfWeek, startTime, endTime)) {
            throw new RuntimeException("error.business.hour.overlapping");
        }

        BusinessHour businessHour = businessHourId != null ?
                findById(businessHourId) : new BusinessHour();

        businessHour.setDayOfWeek(dayOfWeek);
        businessHour.setStartTime(startTime);
        businessHour.setEndTime(endTime);
        businessHour.setIsOpen(isOpen);
        save(businessHour);
    }

    @Override
    public Byte findDayOfWeekById(UUID id) {
        return businessHourRepository.findDayOfWeekById(id);
    }

    @Override
    public long count() {
        return businessHourRepository.count();
    }

}
