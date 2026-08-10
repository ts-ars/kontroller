package com.exempal.shiftcounter.features.comment.adapter.persistence;

import com.exempal.shiftcounter.features.comment.application.ReconcileShiftRepository;
import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaReconcileShiftAdapter implements ReconcileShiftRepository {
    private final ActualDataPort shifts;

    @Override
    public Optional<Shift> findForUpdateByDateAndSensorId(LocalDate date, String sensorId) {
        return shifts.findForUpdateByDateAndSensorId(date, sensorId);
    }
}
