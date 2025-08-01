package com.javaworkshop.business_scheduler.repository;

import com.javaworkshop.business_scheduler.model.BusinessHour;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BusinessHourRepositoryTest {

    @Autowired
    private BusinessHourRepository businessHourRepository;

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

    @DisplayName("Test Find All Business Hours Ordered by Day of Week and Start Time")
    @Test
    void testFindAllByOrderByDayOfWeekAscStartTimeAsc() {

        List<BusinessHour> expected = List.of(secondBusinessHour, firstBusinessHour, thirdBusinessHour);

        List<BusinessHour> actual = businessHourRepository.findAllByOrderByDayOfWeekAscStartTimeAsc();

        assertAll(
            () -> assertEquals(expected.size(), actual.size(),
                    "The size of the list should match the expected size"),
            () -> assertIterableEquals(actual, expected,
                    "The actual list should match the expected list")
        );

    }

    @DisplayName("Test Find Business Hours by Day of Week Ordered by Start Time")
    @Test
    void testFindByDayOfWeekOrderByStartTime() {
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

    @DisplayName("Test Find Day of Week by ID")
    @Test()
    void testFindDayOfWeekById() {
        List<BusinessHour> businessHours = List.of(firstBusinessHour, secondBusinessHour, thirdBusinessHour);

        for (BusinessHour businessHour : businessHours) {
            byte expected = businessHour.getDayOfWeek();

            byte actual = businessHourRepository.findDayOfWeekById(businessHour.getId());

            assertEquals(expected, actual,
                    "The day of week should match the expected value for the given ID");
        }
    }

    @DisplayName("Test If Business Hour is Overlapping when no id is provided")
    @Test
    void testIfBusinessHourIsOverlappingWhenNoIdIsProvided() {
        // creates overlapping times for the first business hour
        // first case
        LocalTime firstCaseOverlappingStart = firstBusinessHour.getStartTime().plusMinutes(1);
        LocalTime firstCaseOverlappingEnd = firstBusinessHour.getEndTime().plusMinutes(1);

        // second case
        LocalTime secondCaseOverlappingStart = firstBusinessHour.getStartTime().plusMinutes(1);
        LocalTime secondCaseOverlappingEnd = firstBusinessHour.getEndTime().minusMinutes(1);

        // third case
        LocalTime thirdCaseOverlappingStart = firstBusinessHour.getStartTime().minusMinutes(1);
        LocalTime thirdOverlappingEnd = firstBusinessHour.getEndTime().minusMinutes(1);

        // fourth case
        LocalTime forthCaseOverlappingStart = firstBusinessHour.getStartTime().minusMinutes(1);
        LocalTime forthCaseOverlappingEnd = firstBusinessHour.getEndTime().plusMinutes(1);

        List<Boolean> allOverlappingCases = List.of(
                businessHourRepository.isOverlapping(null, firstBusinessHour.getDayOfWeek(),
                        firstCaseOverlappingStart, firstCaseOverlappingEnd),
                businessHourRepository.isOverlapping(null, firstBusinessHour.getDayOfWeek(),
                        secondCaseOverlappingStart, secondCaseOverlappingEnd),
                businessHourRepository.isOverlapping(null, firstBusinessHour.getDayOfWeek(),
                        thirdCaseOverlappingStart, thirdOverlappingEnd),
                businessHourRepository.isOverlapping(null, firstBusinessHour.getDayOfWeek(),
                        forthCaseOverlappingStart, forthCaseOverlappingEnd)
        );

        for (boolean isOverlapping : allOverlappingCases) {
            assertTrue(isOverlapping, "Expected overlapping business hour to be found");
        }

    }

    @DisplayName("Test If Business Hour is Overlapping when id is provided")
    @Test
    void testIfBusinessHourIsOverlappingIdIsProvided() {

        // checks if the first business hour overlaps with itself
        boolean isOverlappingWithItself = businessHourRepository.isOverlapping(
                firstBusinessHour.getId(), firstBusinessHour.getDayOfWeek(),
                firstBusinessHour.getStartTime(), firstBusinessHour.getEndTime());

        assertFalse(isOverlappingWithItself, "Expected that business hour does not overlap with itself");

    }

    @DisplayName("Test If Business Hour is Not Overlapping")
    @Test
    void testIfBusinessHourIsNotOverlapping() {
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