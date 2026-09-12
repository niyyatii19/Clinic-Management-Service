package com.clinic.repository;

import com.clinic.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findByPatientNameIgnoreCase(String name);
    boolean existsByPatientNameIgnoreCase(String name);
    boolean existsById(Long id);
}
