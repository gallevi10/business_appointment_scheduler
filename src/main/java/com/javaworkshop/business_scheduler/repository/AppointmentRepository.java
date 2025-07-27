package com.javaworkshop.business_scheduler.repository;

import com.javaworkshop.business_scheduler.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByOrderByStartTime();

    List<Appointment> findAppointmentsByIsCompletedFalseOrderByStartTime();

    Optional<List<Appointment>> findAppointmentsByCustomerIdAndIsCompletedFalseOrderByStartTime(UUID customerId);

    List<Appointment> findByEndTimeBeforeAndIsCompletedFalse(LocalDateTime time);

    List<Appointment> findAppointmentsByStartTimeBetweenAndIsCompletedFalse(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM Appointment a
        WHERE a.startTime < :end
        AND a.endTime > :start
    """)
    boolean isOverlapping(@Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end);
}
