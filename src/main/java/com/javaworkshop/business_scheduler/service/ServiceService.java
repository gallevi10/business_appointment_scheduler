package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.Service;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// This interface defines the contract for service-related operations in the business scheduler application.
public interface ServiceService {

    List<Service> findAll();

    Service findById(UUID id) throws RuntimeException;

    Service save(Service service);

    void deleteById(UUID id);

    // retrieves a paginated list of services
    Page<Service> getServicePage(int page, int size);

    // adds or updates a service entry
    void addOrUpdateService(Service existingService, String serviceName, BigDecimal price,
                            int duration, MultipartFile serviceImage);

    // removes the service image associated with a service
    void removeServiceImage(UUID serviceId) throws IOException;

}
