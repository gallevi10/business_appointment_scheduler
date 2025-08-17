package com.javaworkshop.business_scheduler.dto;

import com.javaworkshop.business_scheduler.model.BusinessHour;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// This class represents the opening hours of a business for each day of the week.
public class OpeningHour {
    private static final String[] DAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    private String day;

    private byte dayOfWeek;

    private List<BusinessHour> ranges;

    public OpeningHour() {
    }

    public OpeningHour(String day, byte dayOfWeek, List<BusinessHour> ranges) {
        this.day = day;
        this.dayOfWeek = dayOfWeek;
        this.ranges = ranges;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(byte dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public List<BusinessHour> getRanges() {
        return ranges;
    }

    public void setRanges(List<BusinessHour> ranges) {
        this.ranges = ranges;
    }

    // converts a list of BusinessHour objects into a list of OpeningHour objects
    public static List<OpeningHour> fromBusinessHours(List<BusinessHour> businessHours) {

        List<OpeningHour> openingHours = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < 7; i++) {
            String day = DAYS[i];
            List<BusinessHour> ranges = new ArrayList<>();

            // assumes that businessHours are sorted by dayOfWeek and startTime
            while (j < businessHours.size() && businessHours.get(j).getDayOfWeek() == i) {
                ranges.add(businessHours.get(j));
                j++;
            }

            openingHours.add(new OpeningHour(day, (byte) i, ranges));
        }

        return openingHours;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OpeningHour other = (OpeningHour) o;
        return dayOfWeek == other.dayOfWeek &&
            Objects.equals(day, other.day) &&
            Objects.equals(ranges, other.ranges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, dayOfWeek, ranges);
    }
}
