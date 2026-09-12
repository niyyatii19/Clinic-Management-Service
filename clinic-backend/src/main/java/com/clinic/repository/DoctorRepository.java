package com.clinic.repository;

import com.clinic.models.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @Query(value = """
            SELECT d.*
            FROM t_doctor d
            JOIN t_doctor_schedule ds
            ON d.doc_id = ds.doc_id AND ds.shift_date = CURRENT_DATE
            where d.doc_name = :docName
    """, nativeQuery = true)
    List<Doctor> findByDoctorNameIgnoreCase(@Param("docName")String docName);

    @Query(value = """
            SELECT d.*
            FROM t_doctor d
            WHERE d.doc_specialization = :specialization
    """, nativeQuery = true)
    List<Doctor> findBySpecializationsIgnoreCase(@Param("specialization")String specialization);

    @Query(value = """
            SELECT d.*
            FROM t_doctor d
            LEFT JOIN t_doctor_schedule ds
            ON d.doc_id = ds.doc_id AND ds.shift_date = CURRENT_DATE
    """, nativeQuery = true)
    List<Doctor> findAllDoctorsWithAvailabilityForToday();
}
