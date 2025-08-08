package com.javaworkshop.business_scheduler.repository;

import com.javaworkshop.business_scheduler.model.BusinessHour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

// This interface defines the repository for managing BusinessHour entities.
@Repository
public interface BusinessHourRepository extends JpaRepository<BusinessHour, UUID> {

    // retrieves all business hours ordered by day of the week and start time
    List<BusinessHour> findAllByOrderByDayOfWeekAscStartTimeAsc();

    // retrieves all business hours that are open on a specific day of the week, ordered by start time
    List<BusinessHour> findByDayOfWeekOrderByStartTime(byte dayOfWeek);

    // retrieves day of the week for a specific business hour by its id
    @Query("""
        SELECT bh.dayOfWeek
        FROM BusinessHour bh
        WHERE bh.id = :id
        """)
    Byte findDayOfWeekById(@Param("id") UUID id);

    // checks if there is an overlapping business hour for a specific day of the week excluding a specific business hour id
    @Query("""
        SELECT CASE WHEN COUNT(bh) > 0 THEN true ELSE false END
        FROM BusinessHour bh
        WHERE (:bhid IS NULL OR bh.id <> :bhid)
        AND bh.dayOfWeek = :dayOfWeek
        AND bh.isOpen = true
        AND bh.startTime < :endTime
        AND bh.endTime > :startTime
    """)
    boolean isOverlapping(@Param("bhid") UUID bhid,
                          @Param("dayOfWeek") byte dayOfWeek,
                          @Param("startTime") LocalTime startTime,
                          @Param("endTime") LocalTime endTime);

}
