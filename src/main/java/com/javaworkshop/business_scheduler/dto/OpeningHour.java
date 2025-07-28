package com.javaworkshop.business_scheduler.dto;

import com.javaworkshop.business_scheduler.model.BusinessHour;

import java.util.ArrayList;
import java.util.List;

// This class represents the opening hours of a business for each day of the week.
public class OpeningHour {
    private static final String[] DAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    private String day;

    private List<BusinessHour> ranges;

    public OpeningHour() {
    }

    public OpeningHour(String day, List<BusinessHour> ranges) {
        this.day = day;
        this.ranges = ranges;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
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

            openingHours.add(new OpeningHour(day, ranges));
        }

        return openingHours;
    }
}
