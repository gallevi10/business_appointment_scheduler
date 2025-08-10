package com.javaworkshop.business_scheduler.config;

import com.javaworkshop.business_scheduler.model.BusinessInfo;
import com.javaworkshop.business_scheduler.service.BusinessInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// This class is used to add global model attributes that can be accessed in all templates.
@ControllerAdvice
public class GlobalModelAttributes {

    private final BusinessInfoService businessInfoService;

    @Autowired
    public GlobalModelAttributes(BusinessInfoService businessInfoService) {
        this.businessInfoService = businessInfoService;
    }

    @ModelAttribute("backgroundPath")
    public String getBackgroundPath() {
        BusinessInfo businessInfo = businessInfoService.getBusinessInfo();
        return businessInfo != null ? businessInfo.getBackgroundPath() : null;
    }
}
