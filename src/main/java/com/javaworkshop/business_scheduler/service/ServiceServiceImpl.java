package com.javaworkshop.business_scheduler.service;


import com.javaworkshop.business_scheduler.repository.ServiceRepository;
import com.javaworkshop.business_scheduler.util.ImageStorageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.javaworkshop.business_scheduler.model.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@org.springframework.stereotype.Service
public class ServiceServiceImpl implements ServiceService{


    private final ServiceRepository serviceRepository;

    @Autowired
    public ServiceServiceImpl(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Override
    public List<Service> findAll() {
        return serviceRepository.findAll();
    }

    @Override
    public Service findById(UUID id) throws RuntimeException {

        Optional<Service> service = serviceRepository.findById(id);
        return service.orElse(null);
    }

    @Override
    public Service save(Service service) {
        return serviceRepository.save(service);
    }

    @Override
    public void deleteById(UUID id) {
        serviceRepository.deleteById(id);
    }

    @Override
    public Page<Service> getServicePage(int page, int size) {
        Page<Service> servicePage = serviceRepository.findByIsActiveTrue(PageRequest.of(page, size));
        if (page >= servicePage.getTotalPages()) {
            throw new RuntimeException();
        }
        return servicePage;
    }

    @Override
    public synchronized void addOrUpdateService(Service existingService, String serviceName, BigDecimal price,
                                   int duration, MultipartFile serviceImage) {

        // validates service name
        if (serviceRepository.existsByServiceName(existingService, serviceName)) {
            throw new RuntimeException("error.service.service.name.conflict");
        }
        Service service = existingService != null ? existingService : new Service();

        // setting the service properties
        service.setServiceName(serviceName);
        service.setPrice(price);
        service.setDuration(duration);
        service.setIsActive(service.getIsActive());
        service = save(service); // save the service first to generate an ID if it's a new service

        // if an image is provided, validate and upload it
        if (serviceImage != null && !serviceImage.isEmpty()) {
            Path uploadPath = Paths.get("uploads/services/" + service.getId());
            String fileName = String.join("_", serviceName.split(" ")).toLowerCase(Locale.ROOT);
            String imagePath = ImageStorageUtils.saveImage(serviceImage, fileName, uploadPath);
            service.setImagePath(imagePath);
            save(service);
        }
    }

    @Override
    public synchronized void removeServiceImage(UUID serviceId) throws IOException{
        Service service = findById(serviceId);
        if (service.getImagePath() == null) { // if the removal is attempted on a service without an image
            throw new RuntimeException();
        }
        Path folderPath = Paths.get("uploads/services/" + serviceId);

        // clears the folder where the service image is stored
        ImageStorageUtils.clearFolder(folderPath);

        // updates the service to remove the image path
        service.setImagePath(null);
        save(service);
    }

}