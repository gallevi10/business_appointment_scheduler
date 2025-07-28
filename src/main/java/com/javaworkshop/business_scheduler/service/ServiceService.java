package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.Service;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ServiceService {

    List<Service> findAll();

    List<Service> findAllActiveServices();

    Service findById(UUID id) throws RuntimeException;

    Service save(Service service);

    void deleteById(UUID id);

    Page<Service> getServicePage(int page, int size);

    void addOrUpdateService(UUID existingServiceId, String serviceName, BigDecimal price,
                            int duration, MultipartFile serviceImage);

    void removeServiceImage(UUID serviceId) throws IOException;

}
