package com.javaworkshop.business_scheduler.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.UUID;

// This class represents a service entity in the business scheduler application.
@Entity
@Table(name = "services")
public class Service {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 100)
    @NotNull
    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @NotNull
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull
    @Column(name = "duration", nullable = false)
    private int duration;

    @Size(max = 255)
    @Column(name = "image_path")
    private String imagePath;

    @ColumnDefault("1")
    @Column(name = "is_active")
    private boolean isActive;

    public Service() {
    }

    public Service(String serviceName, BigDecimal price, int duration, String imagePath, boolean isActive) {
        this.serviceName = serviceName;
        this.price = price;
        this.duration = duration;
        this.imagePath = imagePath;
        this.isActive = isActive;
    }

    public Service(UUID id, String serviceName, BigDecimal price, int duration, String imagePath, boolean isActive) {
        this.id = id;
        this.serviceName = serviceName;
        this.price = price;
        this.duration = duration;
        this.imagePath = imagePath;
        this.isActive = isActive;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

}