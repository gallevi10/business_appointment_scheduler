package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.model.Service;
import com.javaworkshop.business_scheduler.repository.ServiceRepository;
import com.javaworkshop.business_scheduler.util.ImageStorageUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ServiceServiceTest {

    @MockitoBean
    private ServiceRepository serviceRepository;

    @MockitoBean
    private ImageStorageUtils imageStorageUtils;

    @Autowired
    private ServiceService serviceService;

    private Service firstService, secondService, thirdService;

    @BeforeEach
    void setUp() {
        firstService = new Service(UUID.randomUUID(), "first", BigDecimal.valueOf(100),
            30, null, true);
        secondService = new Service(UUID.randomUUID(), "second", BigDecimal.valueOf(200),
            60, null, true);
        thirdService = new Service(UUID.randomUUID(), "third", BigDecimal.valueOf(150),
            45, null, false);
    }

    @DisplayName("Find All Services")
    @Test
    void findAll() {
        List<Service> expected = List.of(firstService, secondService, thirdService);

        when(serviceRepository.findAll()).thenReturn(expected);

        List<Service> foundServices = serviceService.findAll();

        assertAll(
            () -> assertNotNull(foundServices, "Service list should not be null"),
            () -> assertEquals(3, foundServices.size(), "Service list size should be 3"),
            () -> assertIterableEquals(expected, foundServices, "Service lists should be equal")
        );

        verify(serviceRepository).findAll();
    }

    @DisplayName("Find Service by ID")
    @Test
    void findById() {
        Map<UUID, Optional<Service>> serviceMap = Map.of(
            firstService.getId(), Optional.of(firstService),
            secondService.getId(), Optional.of(secondService),
            thirdService.getId(), Optional.of(thirdService)
        );

        serviceMap.forEach(
            (id, optionalService) -> {
                when(serviceRepository.findById(id)).thenReturn(optionalService);
                assertEquals(optionalService.orElse(null), serviceService.findById(id),
                    "Found service should match the expected service");
                verify(serviceRepository).findById(id);
            }
        );

        UUID nonExistentId = UUID.randomUUID();
        when(serviceRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        assertNull(serviceService.findById(nonExistentId),
            "Should return null for non-existent service ID");
        verify(serviceRepository).findById(nonExistentId);
    }

    @DisplayName("Save a Service")
    @Test
    void save() {
        List<Service> services = List.of(firstService, secondService, thirdService);

        services.forEach(
            service -> {
                when(serviceRepository.save(service)).thenReturn(service);
                assertEquals(service, serviceService.save(service),
                    "Saved service should match the original service");
                verify(serviceRepository).save(service);
            }
        );

    }

    @DisplayName("Delete Service by ID")
    @Test
    void deleteById() {
        UUID id = UUID.randomUUID();

        serviceService.deleteById(id);

        verify(serviceRepository).deleteById(id);
    }

    @DisplayName("Get Service Page")
    @Test
    void getServicePage() {

        List<Service> services = List.of(firstService, secondService);
        PageRequest pageRequest = PageRequest.of(0, 3);
        Page<Service> expectedPage = new PageImpl<>(services, pageRequest, services.size());
        when(serviceRepository.findByIsActiveTrue(pageRequest)).thenReturn(expectedPage);

        Page<Service> foundPage = serviceService.getServicePage(0, 3);
        assertAll(
            () -> assertNotNull(foundPage, "Service page should not be null"),
            () -> assertEquals(2, foundPage.getTotalElements(),
                "Total active elements should be 2"),
            () -> assertEquals(1, foundPage.getTotalPages(),
                "Total pages should be 1"),
            () -> assertIterableEquals(services, foundPage.getContent(),
                "Service content should match")
        );

        verify(serviceRepository).findByIsActiveTrue(pageRequest);
    }

    @DisplayName("Exception on Get Service Page with Greater Page Number")
    @Test
    void exceptionOnGetServicePageWithGreaterPageNumber() {
        PageRequest pageRequest = PageRequest.of(1, 3);
        when(serviceRepository.findByIsActiveTrue(pageRequest)).thenReturn(Page.empty());

        assertThrows(RuntimeException.class, () -> serviceService.getServicePage(1, 3),
            "Expected RuntimeException for greater page number");

        verify(serviceRepository).findByIsActiveTrue(pageRequest);
    }

    @DisplayName("Exception on Get Service Page with Negative Page Number")
    @Test
    void exceptionOnGetServicePageWithNegativePageNumber() {
        assertThrows(IllegalArgumentException.class, () -> serviceService.getServicePage(-1, 3),
            "Expected IllegalArgumentException for negative page number");

    }

    @DisplayName("Exception on Add Service with Existing Name")
    @Test
    void exceptionOnAddServiceWithExistingName() {
        when(serviceRepository.existsByServiceName(null, "someExistingService")).thenReturn(true);

        Exception existingServiceNameException = assertThrows(RuntimeException.class, () ->
                serviceService.addOrUpdateService(
                    null, "someExistingService", BigDecimal.valueOf(100),
                    30, null),
            "Expected RuntimeException for existing service name");

        assertEquals("error.service.service.name.conflict",
            existingServiceNameException.getMessage(),
            "Exception message should match");

        verify(serviceRepository).existsByServiceName(null, "someExistingService");
    }

    @DisplayName("Exception on Update Service with Existing Name")
    @Test
    void exceptionOnUpdateServiceWithExistingName() {
        when(serviceRepository.existsByServiceName(firstService, "someExistingService")).thenReturn(true);

        Exception existingServiceNameException = assertThrows(RuntimeException.class, () ->
                serviceService.addOrUpdateService(
                    firstService, "someExistingService", firstService.getPrice(),
                    firstService.getDuration(), null),
            "Expected RuntimeException for existing service name");

        assertEquals("error.service.service.name.conflict",
            existingServiceNameException.getMessage(),
            "Exception message should match");

        verify(serviceRepository).existsByServiceName(firstService, "someExistingService");
    }

    @DisplayName("Update Existing Service With Its Own Name")
    @Test
    void updateExistingServiceWithItsOwnName() {
        when(serviceRepository.existsByServiceName(firstService, firstService.getServiceName()))
            .thenReturn(false);

        when(serviceRepository.save(firstService)).thenReturn(firstService);

        assertDoesNotThrow(() ->
                serviceService.addOrUpdateService(
                    firstService, firstService.getServiceName(), firstService.getPrice(),
                    firstService.getDuration(), null),
            "Should not throw an exception for its own service name");

        verify(serviceRepository).existsByServiceName(firstService, firstService.getServiceName());
        verify(serviceRepository).save(firstService);
    }

    @DisplayName("Successfully Add New Service")
    @Test
    void successfullyAddNewService() {
        Service newService = new Service(
            "New Service",
            BigDecimal.valueOf(100),
            30,
            null, true
        );

        when(serviceRepository.existsByServiceName(null, "New Service"))
            .thenReturn(false);
        when(serviceRepository.save(any(Service.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() ->
            serviceService.addOrUpdateService(
                null,
                newService.getServiceName(),
                newService.getPrice(),
                newService.getDuration(),
                null
            ), "Should not throw an exception for a new service");

        verify(serviceRepository).existsByServiceName(null, newService.getServiceName());
        verify(serviceRepository).save(any(Service.class));
    }

    @DisplayName("Successfully Update a Service")
    @Test
    void successfullyUpdateAService() {
        Service serviceToUpdate = firstService;
        String newServiceName = "Updated Service Name";
        BigDecimal newPrice = BigDecimal.valueOf(120);
        int newDuration = 45;

        when(serviceRepository.existsByServiceName(serviceToUpdate, newServiceName))
            .thenReturn(false);
        when(serviceRepository.save(serviceToUpdate))
            .thenReturn(serviceToUpdate);

        assertAll(
            () -> assertDoesNotThrow(() ->
                serviceService.addOrUpdateService(
                    serviceToUpdate,
                    newServiceName,
                    newPrice,
                    newDuration,
                    null
                ), "Should not throw an exception for updating a service"),
            () -> assertEquals(newServiceName, serviceToUpdate.getServiceName(),
                "Service name should be updated"),
            () -> assertEquals(newPrice, serviceToUpdate.getPrice(),
                "Service price should be updated"),
            () -> assertEquals(newDuration, serviceToUpdate.getDuration(),
                "Service duration should be updated")
        );

        verify(serviceRepository).existsByServiceName(serviceToUpdate, newServiceName);
        verify(serviceRepository).save(serviceToUpdate);
    }

    @DisplayName("Exception on Remove Service Image with Null Image Path")
    @Test
    void exceptionOnRemoveServiceImageWithNullImagePath() {
        List<Service> services = List.of(firstService, secondService, thirdService);

        services.forEach(
            (service -> {
                when(serviceRepository.findById(service.getId()))
                    .thenReturn(Optional.of(service));
                assertThrows(RuntimeException.class, () ->
                        serviceService.removeServiceImage(service.getId()),
                    "Expected RuntimeException for null image path");
                verify(serviceRepository).findById(service.getId());
            })
        );
    }

    @DisplayName("Remove Service Image Successfully")
    @Test
    void removeServiceImageSuccessfully() {
        List<Service> services = List.of(firstService, secondService, thirdService);

        services.forEach(service -> {
            service.setImagePath("uploads/services/" + service.getId() + "/image.jpg");
            when(serviceRepository.findById(service.getId()))
                .thenReturn(Optional.of(service));

            assertDoesNotThrow(() -> serviceService.removeServiceImage(service.getId()),
                "Should not throw an exception for valid image path");
            try {
                Path expectedPath = Paths.get("uploads/services/" + service.getId());
                verify(imageStorageUtils).clearFolder(expectedPath);
            } catch (IOException ignored) {
            } // we are mocking the IO operation.

            assertNull(service.getImagePath(),
                "Image path should be null after removal");
            verify(serviceRepository).findById(service.getId());
            verify(serviceRepository).save(service);
        });

    }
}