package com.clinic.utils.UtilClasses;

import java.time.LocalTime;

public class TimeRange {

    private final LocalTime start;
    private final LocalTime end;

    public TimeRange(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
    }

    public boolean overlaps(LocalTime checkStart, LocalTime checkEnd) {
        return checkStart.isBefore(this.end) && checkEnd.isAfter(this.start);
    }
}

