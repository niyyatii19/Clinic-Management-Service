package com.clinic.repository;

import com.clinic.models.DoctorScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface DoctorScheduleRepository extends JpaRepository<DoctorScheduleEntity, Long> {

    @Query(value = """
            select * from t_doctor_schedule tds
            where tds.doc_id = :docId
            and tds.shift_date = :date
            """, nativeQuery = true)
    Optional<DoctorScheduleEntity> findByDocIdAndShiftDate(@Param("docId")Long docId, @Param("date")LocalDate shiftDate);

    List<DoctorScheduleEntity> findByShiftDateAndIsAvailableTrueAndShiftEndTimeBefore(LocalDate currentDate, LocalTime currentTime);
}
