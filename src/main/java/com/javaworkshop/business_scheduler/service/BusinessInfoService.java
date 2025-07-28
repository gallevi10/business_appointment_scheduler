package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.BusinessInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface BusinessInfoService {

    BusinessInfo getBusinessInfo();

    BusinessInfo save(BusinessInfo businessInfo);

    void updateBusinessInfo(String businessName,
                                    String description,
                                    MultipartFile backgroundImage);

    void removeBackgroundImage() throws IOException;

    boolean isThereBusinessInfo();
}
