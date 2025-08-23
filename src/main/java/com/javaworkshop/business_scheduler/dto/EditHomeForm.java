package com.javaworkshop.business_scheduler.dto;

import com.javaworkshop.business_scheduler.model.BusinessInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

// This class represents the form for editing the business home page.
public class EditHomeForm {

    @NotBlank(message = "Business name is required")
    @Size(min = 3, max = 100, message = "Business name must be between 3 and 100 characters")
    private String businessName;

    @NotNull(message = "Description is required")
    @Size(min = 10, max = 9999, message = "Description must be between 10 and 9999 characters")
    private String description;

    private String imagePath;

    private MultipartFile backgroundImage;

    public EditHomeForm() {
    }

    public EditHomeForm(String businessName, String description, String imagePath, MultipartFile backgroundImage) {
        this.businessName = businessName;
        this.description = description;
        this.imagePath = imagePath;
        this.backgroundImage = backgroundImage;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public MultipartFile getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(MultipartFile backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    // creates an EditHomeForm from a BusinessInfo object
    public static EditHomeForm fromBusinessInfo(BusinessInfo businessInfo) {
        return new EditHomeForm(
                businessInfo.getName(),
                businessInfo.getDescription(),
                businessInfo.getBackgroundPath(),
                null // serviceImage will be set later if needed
        );
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EditHomeForm other = (EditHomeForm) o;
        return Objects.equals(businessName, other.businessName) &&
            Objects.equals(description, other.description) &&
            Objects.equals(imagePath, other.imagePath) &&
            Objects.equals(backgroundImage, other.backgroundImage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(businessName, description, imagePath, backgroundImage);
    }

    @Override
    public String toString() {
        return "EditHomeForm{" +
                "businessName='" + businessName + '\'' +
                ", description='" + description + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", backgroundImage=" + backgroundImage +
                '}';
    }
}
