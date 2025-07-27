package com.javaworkshop.business_scheduler.repository;

import com.javaworkshop.business_scheduler.model.BusinessHour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface BusinessHourRepository extends JpaRepository<BusinessHour, Long> {

    List<BusinessHour> findAllByOrderByDayOfWeekAscStartTimeAsc();

    List<BusinessHour> findByDayOfWeekOrderByStartTime(byte dayOfWeek);

    @Query("""
        SELECT bh.dayOfWeek
        FROM BusinessHour bh
        WHERE bh.id = :id
        """)
    byte findDayOfWeekById(@Param("id") Long id);

    @Query("""
        SELECT CASE WHEN COUNT(bh) > 0 THEN true ELSE false END
        FROM BusinessHour bh
        WHERE (:bhid IS NULL OR bh.id <> :bhid)
        AND bh.dayOfWeek = :dayOfWeek
        AND bh.isOpen = true
        AND bh.startTime < :endTime
        AND bh.endTime > :startTime
    """)
    boolean isOverlapping(@Param("bhid") Long bhid,
                          @Param("dayOfWeek") byte dayOfWeek,
                          @Param("startTime") LocalTime startTime,
                          @Param("endTime") LocalTime endTime);
}
