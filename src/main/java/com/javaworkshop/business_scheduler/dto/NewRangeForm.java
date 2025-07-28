package com.javaworkshop.business_scheduler.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

// This class represents the form for creating a new time range for the business's operating hours.
public class NewRangeForm {

    private byte dayOfWeek;

    @NotNull(message = "Start time is required")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @NotNull(message = "Open status is required")
    private boolean isOpen;

    public NewRangeForm() {
    }

    public NewRangeForm(byte dayOfWeek, LocalTime startTime, LocalTime endTime, boolean isOpen) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isOpen = isOpen;
    }

    public byte getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(byte dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public boolean getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(boolean open) {
        isOpen = open;
    }
}
