package com.clinic.controller;

import com.clinic.dto.PatientDTOs.PatientDto;
import com.clinic.dto.ResponseDto;
import com.clinic.service.DoctorService;
import com.clinic.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/patient/")
@AllArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final DoctorService doctorService;

    @GetMapping
    @Operation(summary = "Get all patients in the clinic")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<PatientDto>> getPatients(){
        var patients = patientService.getAllPatients();
        return ResponseEntity.ok().body(patients);
    }

    @GetMapping(value = "/patientName/{patient}")
    @Operation(summary = "Get patients by name")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public ResponseEntity<List<PatientDto>> getPatientByName(@Valid @PathVariable String patient){
        var patientName = patientService.getPatientByName(patient);
        return ResponseEntity.ok().body(patientName);
    }

    @GetMapping(value = "/{id}")
    @Operation(summary = "get patients by id")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public ResponseEntity<PatientDto> getPatientsById(@Valid @PathVariable Long id){
        var patientId = patientService.getPatientById(id);
        return ResponseEntity.ok().body(patientId);
    }

    @PostMapping(value = "/updatePatient")
    @Operation(summary = "Update patient details")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public ResponseEntity<ResponseDto> updatePatientDetails(@Valid @RequestBody PatientDto patientDetails, Long id){
        var updatePatient = patientService.updatePatientDetails(patientDetails, id);
        return ResponseEntity.ok().body(updatePatient);
    }
}
