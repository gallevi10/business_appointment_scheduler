package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.BusinessInfo;
import com.javaworkshop.business_scheduler.repository.BusinessInfoRepository;
import com.javaworkshop.business_scheduler.util.ImageStorageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Service
public class BusinessInfoServiceImpl implements BusinessInfoService {

    private BusinessInfoRepository businessInfoRepository;

    @Autowired
    public BusinessInfoServiceImpl(BusinessInfoRepository businessInfoRepository) {
        this.businessInfoRepository = businessInfoRepository;
    }

    @Override
    public BusinessInfo getBusinessInfo() {
        Optional<BusinessInfo> businessInfo = businessInfoRepository.findById(1);

        if (businessInfo.isPresent()) {
            return businessInfo.get();
        } else {
            throw new RuntimeException("BusinessInfo not found");
        }
    }

    @Override
    public BusinessInfo save(BusinessInfo businessInfo) {
        return businessInfoRepository.save(businessInfo);
    }

    @Transactional
    @Override
    public void updateBusinessInfo(String businessName,
                                   String description,
                                   MultipartFile backgroundImage) {

        BusinessInfo businessInfo = getBusinessInfo();
        businessInfo.setName(businessName);
        businessInfo.setDescription(description);

        if (backgroundImage != null && !backgroundImage.isEmpty()) {
            Path uploadPath = Paths.get("uploads/business_background");
            String imagePath = ImageStorageUtils.saveImage(backgroundImage, "background_image", uploadPath);
            businessInfo.setBackgroundPath(imagePath);
        }
        save(businessInfo);

    }

    @Transactional
    @Override
    public void removeBackgroundImage() {
        BusinessInfo businessInfo = getBusinessInfo();
        Path folderPath = Paths.get("uploads/business_background");
        try {
            ImageStorageUtils.clearFolder(folderPath);
            businessInfo.setBackgroundPath(null);
            save(businessInfo);
        } catch (IOException e) {
            throw new RuntimeException("error.image.remove");
        }
    }

    @Override
    public boolean isThereBusinessInfo() {
        return businessInfoRepository.existsById(1);
    }
}
