package com.javaworkshop.business_scheduler.repository;

import com.javaworkshop.business_scheduler.model.BusinessInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessInfoRepository extends JpaRepository<BusinessInfo, Integer> {
}
