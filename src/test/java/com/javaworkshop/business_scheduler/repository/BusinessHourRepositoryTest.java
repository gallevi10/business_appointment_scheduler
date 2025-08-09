package com.javaworkshop.business_scheduler.repository;

import com.javaworkshop.business_scheduler.model.BusinessHour;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BusinessHourRepositoryTest {

    @Autowired
    private BusinessHourRepository businessHourRepository;

    private static final int[] MINUTES_TO_ADD = {5, -5};
    private BusinessHour firstBusinessHour, secondBusinessHour, thirdBusinessHour;

    @BeforeEach
    void setUp() {
        // initializes the database with three business hours
        firstBusinessHour = new BusinessHour((byte) 1, // Monday
            LocalTime.of(9, 0), // 09:00 AM
            LocalTime.of(17, 0), // 05:00 PM
            true); // isOpen
        secondBusinessHour = new BusinessHour((byte) 0, // Sunday
            LocalTime.of(10, 0), // 10:00 AM
            LocalTime.of(18, 0), // 06:00 PM
            true);
        thirdBusinessHour = new BusinessHour((byte) 4, // Thursday
            LocalTime.of(11, 0), // 11:00 AM
            LocalTime.of(19, 0), // 07:00 PM
            true);
        businessHourRepository.saveAll(List.of(firstBusinessHour, secondBusinessHour, thirdBusinessHour));
    }

    @AfterEach
    void tearDown() {
        // cleans up the database after each test
        businessHourRepository.deleteAll();
    }

    @DisplayName("Find All Business Hours Ordered By Day of Week And Start Time")
    @Test
    void findAllBusinessHoursOrderedByDayOfWeekAndStartTime() {

        List<BusinessHour> expected = List.of(secondBusinessHour, firstBusinessHour, thirdBusinessHour);

        List<BusinessHour> actual = businessHourRepository.findAllByOrderByDayOfWeekAscStartTimeAsc();

        assertAll(
            () -> assertEquals(expected.size(), actual.size(),
                "The size of the list should match the expected size"),
            () -> assertIterableEquals(actual, expected,
                "The actual list should match the expected list")
        );

    }

    @DisplayName("Find Business Hours By Day of Week Ordered By Start Time")
    @Test
    void findBusinessHoursByDayOfWeekOrderedByStartTime() {
        // adding another range for Monday to test the method
        BusinessHour anotherBusinessHour = new BusinessHour((byte) 1, // Monday
            LocalTime.of(18, 30), // 06:30 PM
            LocalTime.of(21, 0), // 09:00 PM
            true);

        businessHourRepository.save(anotherBusinessHour);

        List<BusinessHour> expected = List.of(firstBusinessHour, anotherBusinessHour);

        List<BusinessHour> actual = businessHourRepository.findByDayOfWeekOrderByStartTime((byte) 1); // Monday

        assertAll(
            () -> assertEquals(expected.size(), actual.size(),
                "The size of the list should match the expected size"),
            () -> assertIterableEquals(actual, expected,
                "The actual list should match the expected list"),
            () -> assertFalse(actual.contains(secondBusinessHour) || actual.contains(thirdBusinessHour),
                "The list should not contain business hours for other days")
        );

    }

    @DisplayName("Find Day of Week by ID")
    @Test
    void findDayOfWeekById() {
        List<BusinessHour> businessHours = List.of(firstBusinessHour, secondBusinessHour, thirdBusinessHour);

        for (BusinessHour businessHour : businessHours) {
            byte expected = businessHour.getDayOfWeek();

            byte actual = businessHourRepository.findDayOfWeekById(businessHour.getId());

            assertEquals(expected, actual,
                "The day of week should match the expected value for the given ID");
        }
    }

    @DisplayName("Overlapping Business Hours When No Id Is Provided")
    @Test
    void OverlappingBusinessHoursWhenNoIdIsProvided() {
        // checks all the cases for overlapping business hours when no id is provided
        for (int minuteToAddForStart : MINUTES_TO_ADD) {
            for (int minuteToAddForEnd : MINUTES_TO_ADD) {
                LocalTime startTime = firstBusinessHour.getStartTime().plusMinutes(minuteToAddForStart);
                LocalTime endTime = firstBusinessHour.getEndTime().plusMinutes(minuteToAddForEnd);

                boolean isOverlapping = businessHourRepository.isOverlapping(
                    null, firstBusinessHour.getDayOfWeek(), startTime, endTime);

                assertTrue(isOverlapping, "Expected overlapping business hour to be found");
            }
        }

    }

    @DisplayName("Non-Overlapping Business Hours When Id Is Provided")
    @Test
    void NonOverlappingBusinessHoursWhenIdIsProvided() {

        // checks all the cases for non-overlapping business hours when id is provided
        // it should not overlap with itself
        for (int minuteToAddForStart : MINUTES_TO_ADD) {
            for (int minuteToAddForEnd : MINUTES_TO_ADD) {
                LocalTime startTime = firstBusinessHour.getStartTime().plusMinutes(minuteToAddForStart);
                LocalTime endTime = firstBusinessHour.getEndTime().plusMinutes(minuteToAddForEnd);

                boolean isOverlapping = businessHourRepository.isOverlapping(
                    firstBusinessHour.getId(), firstBusinessHour.getDayOfWeek(),
                    startTime, endTime);

                assertFalse(isOverlapping, "Expected that the new range for the first business hour" +
                    " does not overlap with itself");
            }
        }

    }

    @DisplayName("Overlapping Business Hours When Id Is Provided")
    @Test
    void OverlappingBusinessHoursWhenIdIsProvided() {

        // check for all the cases for overlapping business hours when id is provided
        for (int minuteToAddForStart : MINUTES_TO_ADD) {
            for (int minuteToAddForEnd : MINUTES_TO_ADD) {
                LocalTime startTime = secondBusinessHour.getStartTime().plusMinutes(minuteToAddForStart);
                LocalTime endTime = secondBusinessHour.getEndTime().plusMinutes(minuteToAddForEnd);

                boolean isOverlapping = businessHourRepository.isOverlapping(
                    firstBusinessHour.getId(), secondBusinessHour.getDayOfWeek(),
                    startTime, endTime);

                assertTrue(isOverlapping, "Expected that the new range for the first business hour" +
                    " overlaps with the second business hour");
            }
        }
    }

    @DisplayName("Non-Overlapping Business Hours When No Id Is Provided")
    @Test
    void NonOverlappingBusinessHoursWhenNoIdIsProvided() {
        // creates non-overlapping times
        // first case
        LocalTime firstCaseNonOverlappingStart = firstBusinessHour.getStartTime().minusMinutes(10);
        LocalTime firstCaseNonOverlappingEnd = firstBusinessHour.getStartTime();

        // second case
        LocalTime secondCaseNonOverlappingStart = firstBusinessHour.getEndTime();
        LocalTime secondCaseNonOverlappingEnd = firstBusinessHour.getEndTime().plusMinutes(10);

        List<Boolean> allNonOverlappingCases = List.of(
            businessHourRepository.isOverlapping(null, firstBusinessHour.getDayOfWeek(),
                firstCaseNonOverlappingStart, firstCaseNonOverlappingEnd),
            businessHourRepository.isOverlapping(null, firstBusinessHour.getDayOfWeek(),
                secondCaseNonOverlappingStart, secondCaseNonOverlappingEnd)
        );

        for (boolean isOverlapping : allNonOverlappingCases) {
            assertFalse(isOverlapping, "Expected no overlapping business hour to be found");
        }
    }
}