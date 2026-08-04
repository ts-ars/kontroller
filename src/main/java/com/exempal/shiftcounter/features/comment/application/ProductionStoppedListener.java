package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.calculator.StoppageCalculator;
import com.exempal.shiftcounter.features.comment.domain.StoppageEntry;
import com.exempal.shiftcounter.features.comment.domain.StoppageRepository;
import com.exempal.shiftcounter.features.settings.infrastructure.ShiftSettingsProvider;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftMetrics;
import com.exempal.shiftcounter.features.shift.domain.ShiftMetricsCalculator;
import com.exempal.shiftcounter.features.shift.infrastructure.JpaShiftAdapter;
import com.exempal.shiftcounter.features.signal.application.SignalService;
import com.exempal.shiftcounter.features.signal.domain.Signal;
import com.exempal.shiftcounter.shared.event.ProductionStoppedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductionStoppedListener {

    private final StoppageRepository stoppageRepository;
    private final JpaShiftAdapter shiftAdapter;
    private final StoppageCalculator stoppageCalculator;
    private final ShiftSettingsProvider settingsProvider;
    private final ShiftMetricsCalculator shiftMetricsCalculator;
    private final SignalService signalService;

    @EventListener
    public void onProductionStopped(ProductionStoppedEvent event) {
        LocalDateTime timestamp = event.getTime();
        LocalDate date = timestamp.toLocalDate();

        Shift shift = shiftAdapter.findByDate(date)
                .orElseThrow(() -> new IllegalStateException("Смена не найдена"));

        ShiftMetrics metrics = shiftMetricsCalculator.calculateFor(
                settingsProvider.get(),
                shift.getHourlyLabels()
        );

        // Поиск hourIndex
        int hourIndex = findHourIndex(timestamp.toLocalTime(), shift, metrics);
        if (hourIndex == -1) {
            return; // время вне смены
        }

        LocalDateTime from = LocalDateTime.of(date, LocalTime.parse(shift.getHourlyLabels().get(hourIndex)));
        LocalDateTime to = from.plus(metrics.duration(hourIndex));

        List<Signal> signals = signalService.getSignalsBetween(from, to);

        List<StoppageEntry> newEntries = stoppageCalculator.recalculate(
                shift,
                hourIndex,
                signals,
                metrics,
                timestamp
        );

        for (StoppageEntry entry : newEntries) {
            Optional<StoppageEntry> existing = stoppageRepository.findByShiftDateAndHourIndexAndMinutes(
                    entry.getShift().getDate(),
                    entry.getHourIndex(),
                    entry.getMinutes()
            );
            if (existing.isEmpty()) {
                stoppageRepository.save(entry);
            }
        }
    }

    private int findHourIndex(LocalTime time, Shift shift, ShiftMetrics metrics) {
        List<String> labels = shift.getHourlyLabels();

        for (int i = 0; i < labels.size(); i++) {
            LocalTime start = LocalTime.parse(labels.get(i));
            LocalTime end = start.plus(metrics.duration(i));
            if (!time.isBefore(start) && time.isBefore(end)) {
                return i;
            }
        }

        return -1; // вне диапазона
    }
}