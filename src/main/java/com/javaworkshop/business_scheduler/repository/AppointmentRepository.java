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

// This interface defines the repository for managing Appointment entities.
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    // retrieves all appointments ordered by their start time
    List<Appointment> findByOrderByStartTime();

    // retrieves all appointments that are not completed, ordered by their start time
    List<Appointment> findAppointmentsByIsCompletedFalseOrderByStartTime();

    // retrieves all appointments for a specific customer that are not completed, ordered by their start time
    Optional<List<Appointment>> findAppointmentsByCustomerIdAndIsCompletedFalseOrderByStartTime(UUID customerId);

    // retrieves all appointments for a specific employee that are not completed, ordered by their start time
    List<Appointment> findByEndTimeBeforeAndIsCompletedFalse(LocalDateTime time);

    // retrieves all appointments that start within a specific time range and are not completed
    List<Appointment> findAppointmentsByStartTimeBetweenAndIsCompletedFalse(LocalDateTime start, LocalDateTime end);

    // checks if there is an overlapping appointment within a given time range
    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM Appointment a
        WHERE a.startTime < :end
        AND a.endTime > :start
    """)
    boolean isOverlapping(@Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end);
}
