package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.StoppageEntry;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class StoppageTimeService {

    /**
     * Возвращает строку точного времени начала остановки,
     * например "08:13", рассчитанное по hourIndex и minuteOffset.
     */
    public String getPreciseTime(StoppageEntry entry, Shift shift) {
        int hourIndex = entry.getHourIndex();
        int minuteOffset = entry.getMinuteOffset();

        List<String> labels = shift.getHourlyLabels();
        if (hourIndex < 0 || hourIndex >= labels.size()) {
            throw new IllegalArgumentException("Invalid hourIndex: " + hourIndex);
        }

        String baseTimeLabel = labels.get(hourIndex); // например: "08:00"
        LocalTime baseTime = LocalTime.parse(baseTimeLabel);
        LocalTime preciseTime = baseTime.plusMinutes(minuteOffset);

        return preciseTime.format(DateTimeFormatter.ofPattern("HH:mm")); // например: "08:13"
    }

    /**
     * Алиас для getPreciseTime, если хочется вызывать как .format(...)
     */
    public String format(StoppageEntry entry, Shift shift) {
        return getPreciseTime(entry, shift);
    }
}
