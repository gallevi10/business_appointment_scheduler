package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.BusinessInfo;
import com.javaworkshop.business_scheduler.repository.BusinessInfoRepository;
import com.javaworkshop.business_scheduler.util.ImageStorageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

// This class implements the BusinessInfoService interface providing methods
// for managing business information in the business scheduler application.
@Service
public class BusinessInfoServiceImpl implements BusinessInfoService {

    private final BusinessInfoRepository businessInfoRepository;

    private final ImageStorageUtils imageStorageUtils;

    @Autowired
    public BusinessInfoServiceImpl(BusinessInfoRepository businessInfoRepository,
                                   ImageStorageUtils imageStorageUtils) {
        this.businessInfoRepository = businessInfoRepository;
        this.imageStorageUtils = imageStorageUtils;
    }

    @Override
    public BusinessInfo getBusinessInfo() {
        Optional<BusinessInfo> businessInfo = businessInfoRepository.findById(1);
        return businessInfo.orElse(null);
    }

    @Override
    public BusinessInfo save(BusinessInfo businessInfo) {
        return businessInfoRepository.save(businessInfo);
    }

    // updates the business information including name, description, and background image
    @Override
    public synchronized void updateBusinessInfo(String businessName,
                                   String description,
                                   MultipartFile backgroundImage) {

        BusinessInfo businessInfo = getBusinessInfo();
        businessInfo.setName(businessName);
        businessInfo.setDescription(description);

        if (backgroundImage != null && !backgroundImage.isEmpty()) {
            Path uploadPath = Paths.get("uploads/business_background");
            String imagePath = imageStorageUtils.saveImage(backgroundImage, "background_image", uploadPath);
            businessInfo.setBackgroundPath(imagePath);
        }
        save(businessInfo);

    }

    // removes the background image from the business information
    @Override
    public synchronized void removeBackgroundImage() throws IOException {
        BusinessInfo businessInfo = getBusinessInfo();
        if (businessInfo.getBackgroundPath() == null) { // if the remove has more than one asynchronous call
            throw new RuntimeException();
        }
        Path folderPath = Paths.get("uploads/business_background");
        imageStorageUtils.clearFolder(folderPath);
        businessInfo.setBackgroundPath(null);
        save(businessInfo);
    }

    // checks if the business information exists in the database
    @Override
    public boolean isThereBusinessInfo() {
        return businessInfoRepository.existsById(1);
    }
}
