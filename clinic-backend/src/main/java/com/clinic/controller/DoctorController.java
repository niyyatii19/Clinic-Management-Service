package com.clinic.controller;

import com.clinic.dto.DoctorDTOs.DoctorShiftTimeDto;
import com.clinic.dto.PatientDTOs.PatientDto;
import com.clinic.dto.ResponseDto;
import com.clinic.dto.DoctorDTOs.doctorDto;
import com.clinic.service.DoctorService;
import com.clinic.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor/")
@AllArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final PatientService patientService;

    @GetMapping
    @Operation(summary = "Get all doctors in the clinic")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<doctorDto>> getDoctors(){
        var doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok().body(doctors);
    }

    @GetMapping(value = "/doctorName")
    @Operation(summary = "Get doctors by name")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<doctorDto>> getAllDoctorsByName(@RequestParam(name = "docName") String docName){
        var doctorName = doctorService.getDoctorsByName(docName);
        return ResponseEntity.ok().body(doctorName);
    }

    @GetMapping(value = "/specialization")
    @Operation(summary = "Get doctors by specialization")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<doctorDto>> getAllDoctorsBySpecialization(@RequestParam(name = "docSpecialization") String docSpecialization){
        var doctorSpecialization = doctorService.getDoctorsBySpecialization(docSpecialization);
        return ResponseEntity.ok().body(doctorSpecialization);
    }

    @PostMapping(value = "/updateDoctor")
    @Operation(summary = "Updates the details of the doctor")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<ResponseDto> updateDoctorDetails(@Valid @RequestBody doctorDto doctor, Long docId){
        var updateDoctor = doctorService.updateDoctorDetails(doctor, docId);
        return ResponseEntity.ok().body(updateDoctor);
    }

    @GetMapping(value = "/getPatientsForDoctors/{patientName}")
    @Operation(summary = "Get patients for doctors view")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<List<PatientDto>> getPatientsForDoctor(@Valid @PathVariable String patientName){
        var patients = patientService.getPatientsForDoctors(patientName);
        return ResponseEntity.ok().body(patients);
    }

    @PostMapping(value = "/recordDoctorShift/{id}")
    @Operation(summary = "Record the shift of doctor")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<ResponseDto> recordDoctorShiftTimings(@Valid @RequestBody DoctorShiftTimeDto doctor, @Valid @PathVariable("id") Long docId){
        var updateDoctor = doctorService.recordDoctorShiftTimings(doctor, docId);
        return ResponseEntity.ok().body(updateDoctor);
    }
}
