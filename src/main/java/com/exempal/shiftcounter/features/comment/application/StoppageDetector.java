package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.DetectionType;
import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import com.exempal.shiftcounter.features.shift.application.ShiftTimeHelper;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.signal.domain.Signal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StoppageDetector {
    private final ShiftTimeHelper timeHelper;

    public List<Stoppage> detectFixedLosses(Shift shift, int intervalIndex, List<Signal> signals,
                                            Duration fixedGapThreshold) {
        long shiftId = requireShiftId(shift);
        LocalDateTime intervalStart = timeHelper.resolveStartTime(
                shift.getHourlyLabels().get(intervalIndex), shift.getDate());
        LocalDateTime intervalEnd = timeHelper.resolveEndTime(
                shift.getHourlyLabels(), intervalIndex, shift.getDate());
        List<Stoppage> result = new ArrayList<>();
        if (signals.isEmpty()) {
            addIfLongEnough(result, shiftId, intervalIndex, intervalStart,
                    Duration.between(intervalStart, intervalEnd), fixedGapThreshold);
            return result;
        }

        List<Signal> sorted = new ArrayList<>(signals);
        sorted.sort(Comparator.comparing(Signal::timestamp));
        addIfLongEnough(result, shiftId, intervalIndex, intervalStart,
                Duration.between(intervalStart, sorted.getFirst().timestamp()), fixedGapThreshold);
        for (int index = 1; index < sorted.size(); index++) {
            LocalDateTime previous = sorted.get(index - 1).timestamp();
            addIfLongEnough(result, shiftId, intervalIndex, previous,
                    Duration.between(previous, sorted.get(index).timestamp()), fixedGapThreshold);
        }
        LocalDateTime last = sorted.getLast().timestamp();
        addIfLongEnough(result, shiftId, intervalIndex, last,
                Duration.between(last, intervalEnd), fixedGapThreshold);
        return result;
    }

    private void addIfLongEnough(List<Stoppage> target, long shiftId, int intervalIndex,
                                 LocalDateTime startedAt, Duration duration, Duration threshold) {
        if (duration.compareTo(threshold) >= 0) {
            target.add(Stoppage.detected(UUID.randomUUID(), shiftId, Stoppage.PRIMARY_SENSOR,
                    intervalIndex, startedAt, duration, 0, DetectionType.FIXED));
        }
    }

    private long requireShiftId(Shift shift) {
        Long id = shift.getId() != null ? shift.getId()
                : shift.getEntity() == null ? null : shift.getEntity().getId();
        if (id == null) throw new IllegalArgumentException("persisted shift is required");
        return id;
    }
}
