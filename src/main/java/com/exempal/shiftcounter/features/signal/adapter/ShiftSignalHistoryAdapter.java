package com.exempal.shiftcounter.features.signal.adapter;

import com.exempal.shiftcounter.features.signal.application.SignalStoragePort;
import com.exempal.shiftcounter.features.signal.domain.Signal;
import com.exempal.shiftcounter.features.comment.application.ReconcileSignalQueryPort;
import com.exempal.shiftcounter.features.shift.application.ShiftSignalHistoryPort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ShiftSignalHistoryAdapter implements ShiftSignalHistoryPort, ReconcileSignalQueryPort {
    private final SignalStoragePort signals;

    public ShiftSignalHistoryAdapter(SignalStoragePort signals) {
        this.signals = signals;
    }

    @Override
    public List<LocalDateTime> findTimestamps(String sensorId, LocalDateTime from, LocalDateTime to) {
        return signals.findBySensorAndRange(sensorId, from, to).stream().map(Signal::timestamp).toList();
    }
}
