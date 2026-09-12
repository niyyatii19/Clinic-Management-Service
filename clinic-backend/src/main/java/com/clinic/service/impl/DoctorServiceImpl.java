package com.clinic.service.impl;

import com.clinic.Exception.ClinicException;
import com.clinic.Exception.ClinicResponseException;
import com.clinic.dto.DoctorDTOs.DoctorShiftTimeDto;
import com.clinic.dto.ResponseDto;
import com.clinic.dto.DoctorDTOs.doctorDto;
import com.clinic.models.DoctorScheduleEntity;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.DoctorScheduleRepository;
import com.clinic.utils.Enums.ResponseStatus;
import com.clinic.utils.Enums.ScheduleRecordType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class DoctorServiceImpl implements com.clinic.service.DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;

    @Override
    public List<doctorDto> getAllDoctors() {
        var doctors = doctorRepository.findAllDoctorsWithAvailabilityForToday();
        return doctors.stream()
                .map(doc ->{
                    var todaySchedule = doc.getSchedules().stream()
                            .filter(sch -> LocalDate.now().equals(sch.getShiftDate()))
                            .findFirst();
                    return doctorDto.builder()
                            .doctorName(doc.getDoctorName())
                            .specialization(doc.getSpecializations())
                            .docDOB(doc.getDocDateOfBirth())
                            .contactNumber(doc.getNumber())
                            .isAvailable(todaySchedule.map(DoctorScheduleEntity::getIsAvailable).orElse(false))
                            .build();
                }).toList();
    }

    @Override
    public List<doctorDto> getDoctorsByName(String docName) {
        var docByName = doctorRepository.findByDoctorNameIgnoreCase(docName);
        return docByName.stream()
                .map(doc -> {
                    var todaySchedule = doc.getSchedules().stream()
                            .filter(sch -> LocalDate.now().equals(sch.getShiftDate()))
                            .findFirst();
                    return doctorDto.builder()
                            .doctorName(doc.getDoctorName())
                            .specialization(doc.getSpecializations())
                            .contactNumber(doc.getNumber())
                            .docDOB(doc.getDocDateOfBirth())
                            .isAvailable(todaySchedule.map(DoctorScheduleEntity::getIsAvailable).orElse(false))
                            .build();
                }).toList();
    }

    @Override
    public List<doctorDto> getDoctorsBySpecialization(String docSpecialization) {
        var docBySpecialization = doctorRepository.findBySpecializationsIgnoreCase(docSpecialization);
        return docBySpecialization.stream()
                .map(doc -> {
                    var todaySchedule = doc.getSchedules().stream()
                            .filter(sch -> LocalDate.now().equals(sch.getShiftDate()))
                            .findFirst();
                    return doctorDto.builder()
                            .doctorName(doc.getDoctorName())
                            .specialization(doc.getSpecializations())
                            .contactNumber(doc.getNumber())
                            .isAvailable(todaySchedule.map(DoctorScheduleEntity::getIsAvailable).orElse(false))
                            .build();
                }).toList();
    }

    @Override
    public ResponseDto updateDoctorDetails(doctorDto doctor, Long docId) {
        var existingDoctor = doctorRepository.findById(docId)
                .orElseThrow(() -> new ClinicException(String.format("Doctor not found for id: %d", docId), HttpStatus.NOT_FOUND));
        try{
            Optional.ofNullable(doctor.getDoctorName()).ifPresent(existingDoctor::setDoctorName);
            Optional.ofNullable(doctor.getSpecialization()).ifPresent(existingDoctor::setSpecializations);
            Optional.ofNullable(doctor.getContactNumber()).ifPresent(existingDoctor::setNumber);
            Optional.ofNullable(doctor.getDocDOB()).ifPresent(existingDoctor::setDocDateOfBirth);
            doctorRepository.save(existingDoctor);

            return ResponseDto.builder()
                    .responseMsg(String.format("Doctor details updated successfully for id: %d", docId))
                    .status(ResponseStatus.SUCCESS)
                    .build();
        } catch (Exception e) {
            return ResponseDto.builder()
                    .responseMsg("Error occurred while updating doctor details: " + e.getMessage())
                    .status(ResponseStatus.ERROR)
                    .build();
        }
    }

    @Override
    public ResponseDto recordDoctorShiftTimings(DoctorShiftTimeDto doctorShiftTimeDto, Long docId) {
        var doctor = doctorRepository.findById(docId)
                .orElseThrow(() -> new ClinicException(String.format("Doctor not found with id %d", docId), HttpStatus.NOT_FOUND));
        var existingScheduleOpt = doctor.getSchedules().stream()
                .filter(schedule -> schedule.getShiftDate().equals(doctorShiftTimeDto.getShiftDate()))
                .findFirst();
        try {
            if (doctorShiftTimeDto.getRecordType().equals(ScheduleRecordType.NEW_ENTRY)) {
                if (existingScheduleOpt.isPresent()) {
                    return ResponseDto.builder()
                            .responseMsg(String.format("Shift already exists for date %s", doctorShiftTimeDto.getShiftDate()))
                            .status(ResponseStatus.ERROR)
                            .build();
                }
                var newSchedule = DoctorScheduleEntity.builder()
                        .shiftStartTime(doctorShiftTimeDto.getShiftStartTime())
                        .shiftEndTime(doctorShiftTimeDto.getShiftEndTime())
                        .shiftDate(doctorShiftTimeDto.getShiftDate())
                        .isAvailable(doctorShiftTimeDto.isAvailable())
                        .doctor(doctor)
                        .build();

                doctorScheduleRepository.save(newSchedule);

            } else if (doctorShiftTimeDto.getRecordType().equals(ScheduleRecordType.UPDATE)) {
                if (existingScheduleOpt.isEmpty()) {
                    return ResponseDto.builder()
                            .responseMsg(String.format("No existing shift found for date %s to update", doctorShiftTimeDto.getShiftDate()))
                            .status(ResponseStatus.ERROR)
                            .build();
                }
                DoctorScheduleEntity existingSchedule = existingScheduleOpt.get();
                existingSchedule.setShiftStartTime(doctorShiftTimeDto.getShiftStartTime());
                existingSchedule.setShiftEndTime(doctorShiftTimeDto.getShiftEndTime());
                existingSchedule.setIsAvailable(doctorShiftTimeDto.isAvailable());

                doctorScheduleRepository.save(existingSchedule);
            }
            return ResponseDto.builder().responseMsg("Shift details updated successfully").status(ResponseStatus.SUCCESS).build();
        } catch (Exception e) {
            throw new ClinicResponseException(String.format("Error updating or recording the doctor shift details for shift date %s", doctorShiftTimeDto.getShiftDate()));
        }
    }

    @Override
    public void updateDoctorAvailability() {
        var currentTime = LocalTime.now().withNano(0);
        var currentDate = LocalDate.now();
        try {
            var doctorsToUpdate = doctorScheduleRepository.findByShiftDateAndIsAvailableTrueAndShiftEndTimeBefore(currentDate, currentTime);
            if (!doctorsToUpdate.isEmpty()) {
                log.info("Found {} doctor(s) to set as unavailable whose shift has ended", doctorsToUpdate.size());
                doctorsToUpdate.forEach(doctor -> {
                    log.info("Setting Dr.{} (Id: {}) as unavailable, shift ended at {}", doctor.getDoctor().getDoctorName(), doctor.getDoctor().getId(), doctor.getShiftEndTime());
                    doctor.setIsAvailable(false);
                });
                doctorScheduleRepository.saveAll(doctorsToUpdate);
            } else {
                log.warn("No doctors found to update availability at the time");
            }
        } catch (Exception ex) {
            throw new ClinicResponseException(ex.getMessage());
        }
    }
}
