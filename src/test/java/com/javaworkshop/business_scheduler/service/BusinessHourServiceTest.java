package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.config.DefaultInitializer;
import com.javaworkshop.business_scheduler.model.BusinessHour;
import com.javaworkshop.business_scheduler.repository.BusinessHourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class BusinessHourServiceTest {

    @MockitoBean
    private DefaultInitializer defaultInitializer;

    @MockitoBean
    private BusinessHourRepository businessHourRepository;

    @Autowired
    private BusinessHourService businessHourService;

    private BusinessHour firstBusinessHour, secondBusinessHour, thirdBusinessHour;

    @BeforeEach
    void setUp() {
        firstBusinessHour = new BusinessHour(UUID.randomUUID(), (byte) 0,
            LocalTime.of(9, 0), LocalTime.of(17, 0), true);
        secondBusinessHour = new BusinessHour(UUID.randomUUID(), (byte) 1,
            LocalTime.of(12, 30), LocalTime.of(18, 0), true);
        thirdBusinessHour = new BusinessHour(UUID.randomUUID(), (byte) 2,
            LocalTime.of(9, 0), LocalTime.of(19, 0), false);
    }

    @DisplayName("Find All Business Hours")
    @Test
    void findAllBusinessHours() {
        List<BusinessHour> businessHours =
            List.of(firstBusinessHour, secondBusinessHour, thirdBusinessHour);

        when(businessHourRepository.findAllByOrderByDayOfWeekAscStartTimeAsc())
            .thenReturn(businessHours);

        assertIterableEquals(businessHours, businessHourService.findAll(),
            "The list of business hours should match the expected list");

        verify(businessHourRepository).findAllByOrderByDayOfWeekAscStartTimeAsc();
    }

    @DisplayName("Find Business Hour By ID")
    @Test
    void findBusinessHourById() {
        List<BusinessHour> businessHours =
            List.of(firstBusinessHour, secondBusinessHour, thirdBusinessHour);

        businessHours.forEach(businessHour -> {
            when(businessHourRepository.findById(businessHour.getId()))
                .thenReturn(java.util.Optional.of(businessHour));
            assertEquals(businessHour, businessHourService.findById(businessHour.getId()),
                "The business hour should match the one retrieved by ID");
            verify(businessHourRepository).findById(businessHour.getId());
        });

        UUID nonExistentId = UUID.randomUUID();
        when(businessHourRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        assertNull(businessHourService.findById(nonExistentId),
            "The business hour should be null for a non-existent ID");
        verify(businessHourRepository).findById(nonExistentId);
    }

    @DisplayName("Save Business Hour")
    @Test
    void saveBusinessHour() {
        List<BusinessHour> businessHours =
            List.of(firstBusinessHour, secondBusinessHour, thirdBusinessHour);

        businessHours.forEach(businessHour -> {
            when(businessHourRepository.save(businessHour)).thenReturn(businessHour);
            assertEquals(businessHour, businessHourService.save(businessHour),
                "The saved business hour should match the one returned by the repository");
            verify(businessHourRepository).save(businessHour);
        });
    }

    @DisplayName("Delete Business Hour By ID")
    @Test
    void deleteBusinessHourById() {
        UUID idToDelete = firstBusinessHour.getId();

        businessHourService.deleteById(idToDelete);

        verify(businessHourRepository).deleteById(idToDelete);
    }

    @DisplayName("Find All Ranges By Day Of Week")
    @Test
    void findAllRangesByDayOfWeek() {
        byte sunday = 0;

        BusinessHour forthBusinessHour = new BusinessHour(UUID.randomUUID(), sunday,
            LocalTime.of(18, 0), LocalTime.of(22, 0), true);

        List<BusinessHour> expected = List.of(firstBusinessHour, forthBusinessHour);

        when(businessHourRepository.findByDayOfWeekOrderByStartTime(sunday))
            .thenReturn(expected);

        assertIterableEquals(expected, businessHourService.findAllRangesByDayOfWeek(sunday),
            "The list of business hours for the specified day should match the expected list");

        verify(businessHourRepository).findByDayOfWeekOrderByStartTime(sunday);
    }

    @DisplayName("Exception When Adding or Updating Business Hour with Invalid Time Range")
    @Test
    void exceptionWhenAddingOrUpdatingBusinessHourWithInvalidTimeRange() {
        byte dayOfWeek = 1;
        LocalTime startTime = LocalTime.of(18, 0);
        LocalTime endTime = LocalTime.of(9, 0);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> businessHourService.addOrUpdateBusinessHour(null,
                dayOfWeek, startTime, endTime, true));

        assertEquals("error.business.hour.start.after.end", exception.getMessage(),
            "Should throw an exception when start time is after end time");
    }

    @DisplayName("Exception When Adding or Updating Business Hour with Overlapping Time Range")
    @Test
    void exceptionWhenAddingOrUpdatingBusinessHourWithOverlappingTimeRange() {
        byte dayOfWeek = 1;
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(18, 0);

        when(businessHourRepository.isOverlapping(null, dayOfWeek, startTime, endTime))
            .thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> businessHourService.addOrUpdateBusinessHour(null,
                dayOfWeek, startTime, endTime, true));

        assertEquals("error.business.hour.overlapping", exception.getMessage(),
            "Should throw an exception when the time range overlaps with existing business hours");

        verify(businessHourRepository).isOverlapping(null, dayOfWeek, startTime, endTime);
    }

    @DisplayName("Successfully Add New Business Hour")
    @Test
    void successfullyAddNewBusinessHour() {

        BusinessHour newBusinessHour = new BusinessHour((byte) 3, LocalTime.of(10, 0),
            LocalTime.of(16, 0), true);

        when(businessHourRepository.isOverlapping(
            null, newBusinessHour.getDayOfWeek(),
            newBusinessHour.getStartTime(), newBusinessHour.getEndTime())
        ).thenReturn(false);

        when(businessHourRepository.save(any(BusinessHour.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() ->
            businessHourService.addOrUpdateBusinessHour(
                null, newBusinessHour.getDayOfWeek(),
                newBusinessHour.getStartTime(), newBusinessHour.getEndTime(),
                newBusinessHour.getIsOpen()
            ), "Should not throw an exception when adding a new valid business hour");

        verify(businessHourRepository).isOverlapping(null, newBusinessHour.getDayOfWeek(),
            newBusinessHour.getStartTime(), newBusinessHour.getEndTime());
        verify(businessHourRepository).save(any(BusinessHour.class));
    }

    @DisplayName("Successfully Update Existing Business Hour")
    @Test
    void successfullyUpdateExistingBusinessHour() {

        List<BusinessHour> existingBusinessHours =
            List.of(firstBusinessHour, secondBusinessHour, thirdBusinessHour);
        LocalTime newStartTime = LocalTime.of(10, 0);
        LocalTime newEndTime = LocalTime.of(16, 0);
        boolean newIsOpen = false;

        existingBusinessHours.forEach(existingBusinessHour -> {
            when(businessHourRepository.isOverlapping(
                existingBusinessHour.getId(),
                existingBusinessHour.getDayOfWeek(),
                newStartTime, newEndTime)
            ).thenReturn(false);

            when(businessHourRepository.findById(existingBusinessHour.getId()))
                .thenReturn(Optional.of(existingBusinessHour));

            when(businessHourRepository.save(existingBusinessHour))
                .thenReturn(existingBusinessHour);

            assertDoesNotThrow(() ->
                businessHourService.addOrUpdateBusinessHour(
                    existingBusinessHour.getId(), existingBusinessHour.getDayOfWeek(),
                    newStartTime, newEndTime, newIsOpen
                ), "Should not throw an exception when updating a valid business hour");

            verify(businessHourRepository).isOverlapping(
                existingBusinessHour.getId(),
                existingBusinessHour.getDayOfWeek(),
                newStartTime, newEndTime
            );
            verify(businessHourRepository).findById(existingBusinessHour.getId());
            verify(businessHourRepository).save(existingBusinessHour);
        });
    }

    @DisplayName("Find Day Of Week By ID")
    @Test
    void findDayOfWeekById() {
        List<BusinessHour> businessHours =
            List.of(firstBusinessHour, secondBusinessHour, thirdBusinessHour);

        businessHours.forEach(businessHour -> {
            when(businessHourRepository.findDayOfWeekById(businessHour.getId()))
                .thenReturn(businessHour.getDayOfWeek());
            assertEquals(businessHour.getDayOfWeek(),
                businessHourService.findDayOfWeekById(businessHour.getId()));
            verify(businessHourRepository).findDayOfWeekById(businessHour.getId());
        });

        UUID nonExistentId = UUID.randomUUID();
        when(businessHourRepository.findDayOfWeekById(nonExistentId)).thenReturn(null);
        assertNull(businessHourService.findDayOfWeekById(nonExistentId));
        verify(businessHourRepository).findDayOfWeekById(nonExistentId);
    }

    @DisplayName("Count Business Hours")
    @Test
    void countBusinessHours() {
        when(businessHourRepository.count()).thenReturn(3L);

        assertEquals(3, businessHourService.count());

        verify(businessHourRepository).count();
    }
}