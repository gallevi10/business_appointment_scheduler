package com.javaworkshop.business_scheduler.service;

import com.javaworkshop.business_scheduler.config.DefaultInitializer;
import com.javaworkshop.business_scheduler.model.BusinessInfo;
import com.javaworkshop.business_scheduler.repository.BusinessInfoRepository;
import com.javaworkshop.business_scheduler.util.ImageStorageUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class BusinessInfoServiceTest {

    @MockitoBean
    private DefaultInitializer defaultInitializer;

    @MockitoBean
    private BusinessInfoRepository businessInfoRepository;

    @Autowired
    private BusinessInfoService businessInfoService;

    private BusinessInfo businessInfo;

    @BeforeEach
    void setUp() {
        businessInfo = new BusinessInfo(1, "Business Name", "Business Description",
                "uploads/business_background/background_image.jpg");
    }

    @DisplayName("Get Business Info")
    @Test
    void getBusinessInfo() {
        when(businessInfoRepository.findById(1)).thenReturn(Optional.of(businessInfo));

        assertEquals(businessInfo, businessInfoService.getBusinessInfo(),
                "Business info should match the expected business info");

        verify(businessInfoRepository).findById(1);
    }

    @DisplayName("Save Business Info")
    @Test
    void saveBusinessInfo() {
        when(businessInfoRepository.save(businessInfo)).thenReturn(businessInfo);

        assertEquals(businessInfo, businessInfoService.save(businessInfo),
                "Saved business info should match the expected business info");

        verify(businessInfoRepository).save(businessInfo);
    }

    @DisplayName("Update Business Info")
    @Test
    void updateBusinessInfo() {
        String newName = "Updated Business Name";
        String newDescription = "Updated Business Description";

        when(businessInfoRepository.findById(1)).thenReturn(Optional.of(businessInfo));
        when(businessInfoRepository.save(businessInfo)).thenReturn(businessInfo);

        businessInfoService.updateBusinessInfo(newName, newDescription, null);

        assertAll(
            () -> assertEquals(newName, businessInfo.getName(), "Business name should be updated"),
            () -> assertEquals(newDescription, businessInfo.getDescription(), "Business description should be updated")
        );

        verify(businessInfoRepository).findById(1);
        verify(businessInfoRepository).save(businessInfo);
    }

    @DisplayName("Exception on Remove Background Image with Null Image Path")
    @Test
    void exceptionOnRemoveBackgroundImageWithNullImagePath() {

        businessInfo.setBackgroundPath(null);
        when(businessInfoRepository.findById(1))
                .thenReturn(Optional.of(businessInfo));

        assertThrows(RuntimeException.class, () ->
                        businessInfoService.removeBackgroundImage(),
                "Expected RuntimeException for null image path");

        verify(businessInfoRepository).findById(1);
    }

    @DisplayName("Remove Background Image Successfully")
    @Test
    void removeBackgroundImageSuccessfully(){

        when(businessInfoRepository.findById(1))
                .thenReturn(Optional.of(businessInfo));

        // mocking the static method ImageStorageUtils.clearFolder
        try (MockedStatic<ImageStorageUtils> mockedStatic = mockStatic(ImageStorageUtils.class)) {
            assertDoesNotThrow(() -> businessInfoService.removeBackgroundImage(),
                    "Should not throw an exception for non-null image path");

            Path expectedPath = Paths.get("uploads/business_background");
            mockedStatic.verify(() -> ImageStorageUtils.clearFolder(expectedPath));
        }

        assertNull(businessInfo.getBackgroundPath(),
                "Background image path should be null after removal");

        verify(businessInfoRepository).findById(1);
        verify(businessInfoRepository).save(businessInfo);

    }

    @DisplayName("Is There Business Info")
    @Test
    void isThereBusinessInfo() {
        when(businessInfoRepository.existsById(1)).thenReturn(true);

        assertTrue(businessInfoService.isThereBusinessInfo(),
                "Business info should exist in the database");

        verify(businessInfoRepository).existsById(1);
    }
}