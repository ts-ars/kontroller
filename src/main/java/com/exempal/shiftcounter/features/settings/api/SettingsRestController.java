package com.exempal.shiftcounter.features.settings.api;

import com.exempal.shiftcounter.common.domain.EventPublisherPort;
import com.exempal.shiftcounter.features.settings.domain.SettingUpdatedEvent;
import com.exempal.shiftcounter.features.settings.domain.Settings;
import com.exempal.shiftcounter.features.settings.domain.SettingsPort;
import com.exempal.shiftcounter.features.settings.infrastructure.ShiftHourLabelMapper;
import com.exempal.shiftcounter.features.settings.infrastructure.ShiftSettingsApplier;
import com.exempal.shiftcounter.features.settings.infrastructure.ShiftSettingsProvider;
import com.exempal.shiftcounter.features.shift.application.ShiftIntervalService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/settings")
public class SettingsRestController {

    private final SettingsPort settings;
    private final EventPublisherPort events;
    private final ShiftSettingsProvider settingsProvider;
    private final ShiftSettingsApplier settingsApplier;
    private final ShiftIntervalService intervals;

    public SettingsRestController(
            SettingsPort settings,
            EventPublisherPort events,
            ShiftSettingsProvider settingsProvider,
            ShiftSettingsApplier settingsApplier,
            ShiftIntervalService intervals
    ) {
        this.settings = settings;
        this.events = events;
        this.settingsProvider = settingsProvider;
        this.settingsApplier = settingsApplier;
        this.intervals = intervals;
    }

    @PostMapping
    public void updateSettings(@RequestBody @Valid SettingsRequest request) {
        if (request.hours() == null || request.hours().isEmpty() || request.hourlyPlans() == null
                || request.hours().size() != request.hourlyPlans().size()) {
            throw new IllegalArgumentException("Time and Plan must have the same non-empty size");
        }
        List<Integer> plans = request.hourlyPlans().stream().map(Integer::parseInt).toList();
        if (plans.stream().anyMatch(value -> value < 0)) {
            throw new IllegalArgumentException("Plan must not be negative");
        }
        intervals.resolve(LocalDate.of(2000, 1, 1), request.hours(), plans.size());
        settings.updateHours(request.hours());
        settings.updateHourlyPlans(request.hourlyPlans());

        log.info("Настройки сохранены пользователем. Применяем к текущей production shift.");

        settingsProvider.reload(); // обязательно ДО применения к смене

        settingsApplier.applySettingsToCurrentShift(); // обновление смены

        log.info("📥 Получен POST /settings: hours = {}, plan = {}", request.hours(), request.hourlyPlans());

        events.publish(new SettingUpdatedEvent("allSettings", "updated"));
    }

    @GetMapping
    public ResponseEntity<SettingsResponse> getSettings() {
        Settings current = settingsApplier.getCurrentSettings();

        List<String> hours = ShiftHourLabelMapper.toLabelsStartOnly(current.getHours());

        List<String> plan = current.getHourlyPlans().stream()
                .map(String::valueOf)
                .toList();

        log.info("📤 GET /settings → из ShiftSettingsApplier: hours = {}, plan = {}", hours, plan);

        return ResponseEntity.ok(new SettingsResponse(hours, plan));
    }
}
