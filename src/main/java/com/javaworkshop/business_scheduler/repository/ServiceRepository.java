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

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {

    List<Service> findByIsActiveTrue();

    Page<Service> findByIsActiveTrue(Pageable pageable);

    @Query("""
            SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
            FROM Service s
            WHERE (:id IS NULL OR s.id <> :id)
            AND s.serviceName = :serviceName
            """)
    boolean existsByServiceName(@Param("id") UUID id,
                                @Param("serviceName") String serviceName);
}
