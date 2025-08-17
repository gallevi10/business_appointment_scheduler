package com.javaworkshop.business_scheduler.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Objects;

// This class represents a form for creating a new service.
public class NewServiceForm {

    @NotBlank(message = "Service name is required")
    @Size(min = 3, max = 100, message = "Service name must be between 3 and 100 characters")
    private String serviceName;

    @NotNull(message = "Price is required")
    @Digits(integer = 10, fraction = 2, message = "Price must be a valid decimal number with up to 2 decimal places")
    private BigDecimal price;

    @NotNull(message = "Duration is required")
    @Digits(integer = 3, fraction = 0, message = "Duration must be a valid integer and cannot exceed 999 minutes")
    private int duration;

    private String imagePath;

    private MultipartFile serviceImage;

    public NewServiceForm() {
    }

    public NewServiceForm(String serviceName, BigDecimal price, int duration, String imagePath, MultipartFile image) {
        this.serviceName = serviceName;
        this.price = price;
        this.duration = duration;
        this.imagePath = imagePath;
        this.serviceImage = image;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public MultipartFile getServiceImage() {
        return serviceImage;
    }

    public void setServiceImage(MultipartFile serviceImage) {
        this.serviceImage = serviceImage;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NewServiceForm other = (NewServiceForm) o;
        return duration == other.duration &&
            Objects.equals(serviceName, other.serviceName) &&
            Objects.equals(price, other.price) &&
            Objects.equals(imagePath, other.imagePath) &&
            Objects.equals(serviceImage, other.serviceImage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, price, duration, imagePath, serviceImage);
    }
}
