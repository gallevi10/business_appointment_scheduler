package com.javaworkshop.business_scheduler.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// This class represents the business information entity in the business scheduler application.
@Entity
@Table(name = "business_info")
public class BusinessInfo {

    @Id
    @Column(name = "id", nullable = false)
    private int id;

    @Size(max = 100)
    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Lob
    @Size(max = 9999)
    @Column(name = "description")
    private String description;

    @Size(max = 255)
    @Column(name = "background_path")
    private String backgroundPath;

    public BusinessInfo() {
    }

    public BusinessInfo(String name, String description, String backgroundPath) {
        this.name = name;
        this.description = description;
        this.backgroundPath = backgroundPath;
    }

    public BusinessInfo(int id, String name, String description, String backgroundPath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.backgroundPath = backgroundPath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBackgroundPath() {
        return backgroundPath;
    }

    public void setBackgroundPath(String backgroundPath) {
        this.backgroundPath = backgroundPath;
    }

    @Override
    public String toString() {
        return "BusinessInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", backgroundPath='" + backgroundPath + '\'' +
                '}';
    }
}