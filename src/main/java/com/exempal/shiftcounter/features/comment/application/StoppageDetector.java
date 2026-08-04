package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.StoppageEntry;
import com.exempal.shiftcounter.features.shift.infrastructure.ShiftEntity;
import com.exempal.shiftcounter.features.signal.domain.Signal;
import com.exempal.shiftcounter.features.shift.application.ShiftTimeHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class StoppageDetector {

    private final ShiftTimeHelper timeHelper;

    public List<StoppageEntry> detectFixedLosses(
            ShiftEntity shift,
            int hourIndex,
            List<Signal> signals,
            Duration fixedGapThreshold
    ) {
        List<StoppageEntry> result = new ArrayList<>();

        final String label = shift.getHourlyLabels().get(hourIndex);
        final LocalDateTime startOfHour = timeHelper.resolveStartTime(label, shift.getDate());
        final LocalDateTime endOfHour   = timeHelper.resolveEndTime(shift.getHourlyLabels(), hourIndex, shift.getDate());

        if (signals.isEmpty()) {
            Duration full = Duration.between(startOfHour, endOfHour);
            if (full.compareTo(fixedGapThreshold) >= 0) {
                StoppageEntry e = StoppageEntry.fixed(hourIndex, full, shift);
                e.setMinuteOffset(0); // весь час — от начала
                result.add(e);
            }
            return result;
        }

        List<Signal> sorted = new ArrayList<>(signals);
        sorted.sort(Comparator.comparing(Signal::timestamp));

        // leading: от начала часа до первого сигнала
        Duration leadingGap = Duration.between(startOfHour, sorted.get(0).timestamp());
        if (leadingGap.compareTo(fixedGapThreshold) >= 0) {
            StoppageEntry e = StoppageEntry.fixed(hourIndex, leadingGap, shift);
            e.setMinuteOffset(minutesBetween(label, startOfHour.toLocalTime())); // будет 0
            result.add(e);
        }

        // между сигналами
        for (int i = 1; i < sorted.size(); i++) {
            LocalDateTime prev = sorted.get(i - 1).timestamp();
            LocalDateTime cur  = sorted.get(i).timestamp();
            Duration gap = Duration.between(prev, cur);
            if (gap.compareTo(fixedGapThreshold) >= 0) {
                StoppageEntry e = StoppageEntry.fixed(hourIndex, gap, shift);
                e.setMinuteOffset(minutesBetween(label, prev.toLocalTime()));
                result.add(e);
            }
        }

        // trailing: от последнего сигнала до конца часа
        LocalDateTime lastTs = sorted.get(sorted.size() - 1).timestamp();
        Duration trailingGap = Duration.between(lastTs, endOfHour);
        if (trailingGap.compareTo(fixedGapThreshold) >= 0) {
            StoppageEntry e = StoppageEntry.fixed(hourIndex, trailingGap, shift);
            e.setMinuteOffset(minutesBetween(label, lastTs.toLocalTime()));
            result.add(e);
        }

        return result;
    }

    private int minutesBetween(String hourLabel, LocalTime from) {
        // hourLabel вида "15:30"
        LocalTime start = LocalTime.parse(hourLabel);
        long mins = Duration.between(start, from).toMinutes();
        return (int) Math.max(0, mins);
    }
}