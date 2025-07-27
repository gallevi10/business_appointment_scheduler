package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.dto.OpeningHour;
import com.javaworkshop.business_scheduler.model.BusinessHour;
import com.javaworkshop.business_scheduler.repository.BusinessHourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class BusinessHourServiceImpl implements BusinessHourService{

    BusinessHourRepository businessHourRepository;

    @Autowired
    public BusinessHourServiceImpl(BusinessHourRepository businessHourRepository) {
        this.businessHourRepository = businessHourRepository;
    }

    @Override
    public List<BusinessHour> findAll() {
        return businessHourRepository.findAllByOrderByDayOfWeekAscStartTimeAsc();
    }

    @Override
    public BusinessHour findById(long id) throws RuntimeException {
        Optional<BusinessHour> businessHour = businessHourRepository.findById(id);

        if (businessHour.isPresent()) {
            return businessHour.get();
        } else {
            throw new RuntimeException("BusinessHour not found with id: " + id);
        }
    }

    @Override
    public BusinessHour save(BusinessHour businessHour) {
        return businessHourRepository.save(businessHour);
    }

    @Override
    public void deleteById(long id) {
        businessHourRepository.deleteById(id);
    }

    @Override
    public List<BusinessHour> findAllRangesByDayOfWeek(byte dayOfWeek) {
        return businessHourRepository.findByDayOfWeekOrderByStartTime(dayOfWeek);
    }

    @Override
    public List<OpeningHour> getDayBusinessHours(byte dayOfWeek) {
        List<BusinessHour> businessHours = findAllRangesByDayOfWeek(dayOfWeek);
        return OpeningHour.fromBusinessHours(businessHours);
    }

    @Transactional
    @Override
    public void addOrUpdateBusinessHour(Long businessHourId,
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

        BusinessHour businessHour;
        if (businessHourId != null) {
            businessHour = findById(businessHourId);
        } else {
            businessHour = new BusinessHour();
        }

        businessHour.setDayOfWeek(dayOfWeek);
        businessHour.setStartTime(startTime);
        businessHour.setEndTime(endTime);
        businessHour.setIsOpen(isOpen);
        save(businessHour);
    }

    @Override
    public Byte findDayOfWeekById(long id) {
        return businessHourRepository.findDayOfWeekById(id);
    }

    @Override
    public long count() {
        return businessHourRepository.count();
    }

}
