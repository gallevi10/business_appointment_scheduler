package com.javaworkshop.business_scheduler.repository;

import com.javaworkshop.business_scheduler.model.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

// This class defines the repository for managing Service entities.
@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {

    // retrieves all active services
    List<Service> findByIsActiveTrue();

    // retrieves a paginated list of active services
    Page<Service> findByIsActiveTrue(Pageable pageable);

    // checks if a service exists by its name excluding a specified service
    @Query("""
            SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
            FROM Service s
            WHERE (:id IS NULL OR s.id <> :id)
            AND s.serviceName = :serviceName
            """)
    boolean existsByServiceName(@Param("id") UUID id,
                                @Param("serviceName") String serviceName);
}
