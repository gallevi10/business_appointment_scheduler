package com.javaworkshop.business_scheduler.repository;

import com.javaworkshop.business_scheduler.model.Service;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ServiceRepositoryTest {

    @Autowired
    private ServiceRepository serviceRepository;

    private Service firstService, secondService, thirdService;

    @BeforeEach
    void setUp() {
        firstService = new Service("First Service", BigDecimal.valueOf(50),
                30, null, true);
        secondService = new Service("Second Service", BigDecimal.valueOf(75),
                45, null, true);
        thirdService = new Service("Third Service", BigDecimal.valueOf(100),
                60, null, false);
        serviceRepository.saveAll(List.of(firstService, secondService, thirdService));
    }

    @AfterEach
    void tearDown() {
        serviceRepository.deleteAll();
    }

    @DisplayName("Find All Active Services As a Paginated List")
    @Test
    void findAllActiveServicesAsAPaginatedList() {
        int PAGE_SIZE = 3;
        Pageable pageable = PageRequest.of(0, PAGE_SIZE);
        List<Service> expectedContent = List.of(firstService, secondService);
        Page<Service> expected = new PageImpl<>(expectedContent, pageable, expectedContent.size());

        Page<Service> actual = serviceRepository.findByIsActiveTrue(pageable);

        assertAll(
                () -> assertEquals(expected.getTotalElements(), actual.getTotalElements(),
                        "The total number of services should match the expected count"),
                () -> assertEquals(expected.getTotalPages(), actual.getTotalPages(),
                        "The total number of pages should match the expected count"),
                () -> assertEquals(expected.getNumber(), actual.getNumber(),
                        "The current page number should match the expected page number"),
                () -> assertEquals(expected.getSize(), actual.getSize(),
                        "The page size should match the expected size"),
                () -> assertEquals(expected.getContent().size(), actual.getContent().size(),
                        "The number of services in the page should match the expected count"),
                () -> assertIterableEquals(expected.getContent(), actual.getContent(),
                        "The content of the page should match the expected services"),
                () -> assertTrue(actual.getContent().containsAll(expected.getContent()),
                        "The content of the page should match the expected services")
        );
    }

    @DisplayName("Existing Service Exists By Its Name")
    @Test
    void existingServiceExistsByServiceName() {
        String serviceName = "First Service";
        boolean exists = serviceRepository.existsByServiceName(null, serviceName);

        assertTrue(exists, "Should be true since the service exists in the database");
    }

    @DisplayName("Not Existing Service Exists By Its Name")
    @Test
    void notExistingServiceExistsByServiceName() {
        String serviceName = "Forth Service";
        boolean exists = serviceRepository.existsByServiceName(null, serviceName);

        assertFalse(exists, "Should be false since the service does not exist in the database");
    }

    @DisplayName("Service Exists By Its Own Name")
    @Test
    void existingServiceProvidingItsOwnNameExistsByServiceName() {
        String serviceName = "First Service";
        boolean exists = serviceRepository.existsByServiceName(firstService, serviceName);

        assertFalse(exists, "Should be false since we are checking against the same service");
    }

    @DisplayName("Existing Service Exists By Its Name When Providing Different Service")
    @Test
    void existingServiceProvidingAnotherExistingServiceExistsByServiceName() {
        String serviceName = "Second Service";
        boolean exists = serviceRepository.existsByServiceName(firstService, serviceName);

        assertTrue(exists, "Should be true since the service exists in the" +
                " database and it is not the same as the provided service");
    }
}