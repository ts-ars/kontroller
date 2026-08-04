package com.exempal.shiftcounter.features.comment.adapter.web;

import com.exempal.shiftcounter.features.comment.adapter.dto.CommentRowDto;
import com.exempal.shiftcounter.features.comment.adapter.dto.StoppageViewDto;
import com.exempal.shiftcounter.features.comment.adapter.mapper.CommentRowMapper;
import com.exempal.shiftcounter.features.comment.adapter.mapper.StoppageViewMapper;
import com.exempal.shiftcounter.features.comment.calculator.StoppageCalculator;
import com.exempal.shiftcounter.features.comment.domain.StoppageEntry;
import com.exempal.shiftcounter.features.comment.domain.StoppageRepository;
import com.exempal.shiftcounter.features.comment.domain.StoppageType;
import com.exempal.shiftcounter.features.settings.infrastructure.ShiftSettingsProvider;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftMetrics;
import com.exempal.shiftcounter.features.shift.domain.ShiftMetricsCalculator;
import com.exempal.shiftcounter.features.shift.infrastructure.JpaShiftAdapter;
import com.exempal.shiftcounter.features.signal.application.SignalService;
import com.exempal.shiftcounter.features.signal.domain.Signal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/stoppages")
@RequiredArgsConstructor
@Slf4j
public class StoppageController {

    private final StoppageRepository repository;
    private final JpaShiftAdapter shiftAdapter;
    private final SignalService signalService;
    private final StoppageCalculator stoppageCalculator;
    private final ShiftMetricsCalculator shiftMetricsCalculator;
    private final ShiftSettingsProvider settingsProvider;

    @PostMapping("/recalculate")
    public ResponseEntity<Void> recalculate() {
        LocalDate today = LocalDate.now();

        Shift shift = shiftAdapter.findByDate(today)
                .orElseThrow(() -> new IllegalStateException("Смена не найдена"));

        ShiftMetrics metrics = shiftMetricsCalculator.calculateFor(
                settingsProvider.get(),
                shift.getHourlyLabels()
        );

        for (int hourIndex = 0; hourIndex < shift.getHourlyLabels().size(); hourIndex++) {
            String label = shift.getHourlyLabels().get(hourIndex);
            Duration duration = metrics.duration(hourIndex);

            LocalDateTime from = LocalDateTime.of(shift.getDate(), LocalTime.parse(label));
            LocalDateTime to = from.plus(duration);

            List<Signal> signals = signalService.getSignalsBetween(from, to);

            List<StoppageEntry> newEntries = stoppageCalculator.recalculate(
                    shift,
                    hourIndex,
                    signals,
                    metrics,
                    LocalDateTime.now()
            );

            for (StoppageEntry newEntry : newEntries) {
                Optional<StoppageEntry> existing = repository.findByShiftDateAndHourIndexAndMinutes(
                        newEntry.getShift().getDate(),
                        newEntry.getHourIndex(),
                        newEntry.getMinutes()
                );

                if (existing.isEmpty()) {
                    repository.save(newEntry);
                }
            }
        }

        log.info("✅ Потери успешно пересчитаны и сохранены");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/range")
    public List<StoppageViewDto> getRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return repository.findByShiftDateBetween(from, to).stream()
                .map(StoppageViewMapper::toDto)
                .toList();
    }

    @PatchMapping("/{id}/type")
    public ResponseEntity<Void> updateType(
            @PathVariable Long id,
            @RequestBody String type
    ) {
        if (!StoppageType.isValid(type)) {
            log.warn("⛔️ Неверный тип остановки: {}", type);
            return ResponseEntity.badRequest().build();
        }

        StoppageEntry entry = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Остановка не найдена: " + id));

        entry.setType(StoppageType.valueOf(type.toUpperCase()));
        repository.save(entry);

        log.info("✏️ Тип остановки [{}] изменён на {}", id, type);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/report")
    public List<CommentRowDto> getCommentRows(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return repository.findByShiftDate(date).stream()
                .filter(entry -> entry.getType() != null && entry.getType().isUserEditable())
                .map(CommentRowMapper::toDto)
                .toList();
    }
}