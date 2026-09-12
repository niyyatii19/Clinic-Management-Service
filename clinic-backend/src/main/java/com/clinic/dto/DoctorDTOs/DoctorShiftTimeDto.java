package com.clinic.dto.DoctorDTOs;

import com.clinic.utils.Enums.ScheduleRecordType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorShiftTimeDto {

    private ScheduleRecordType recordType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "H:mm")
    @Schema(type = "string", format = "HH:mm", example = "18:30", description = "Time in HH:mm format")
    private LocalTime shiftStartTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @Schema(type = "string", format = "HH:mm", example = "18:30", description = "Time in HH:mm format")
    private LocalTime shiftEndTime;

    private LocalDate shiftDate;
    private boolean isAvailable;
}
