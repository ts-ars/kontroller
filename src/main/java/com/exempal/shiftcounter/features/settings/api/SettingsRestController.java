package com.exempal.shiftcounter.features.settings.api;

import com.exempal.shiftcounter.common.domain.EventPublisherPort;
import com.exempal.shiftcounter.features.settings.domain.SettingUpdatedEvent;
import com.exempal.shiftcounter.features.settings.domain.Settings;
import com.exempal.shiftcounter.features.settings.domain.SettingsPort;
import com.exempal.shiftcounter.features.settings.infrastructure.ShiftHourLabelMapper;
import com.exempal.shiftcounter.features.settings.infrastructure.ShiftSettingsApplier;
import com.exempal.shiftcounter.features.settings.infrastructure.ShiftSettingsProvider;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/settings")
public class SettingsRestController {

    private final SettingsPort settings;
    private final EventPublisherPort events;
    private final ShiftSettingsProvider settingsProvider;
    private final ShiftSettingsApplier settingsApplier;

    public SettingsRestController(
            SettingsPort settings,
            EventPublisherPort events,
            ShiftSettingsProvider settingsProvider,
            ShiftSettingsApplier settingsApplier
    ) {
        this.settings = settings;
        this.events = events;
        this.settingsProvider = settingsProvider;
        this.settingsApplier = settingsApplier;
    }

    @PostMapping
    public void updateSettings(@RequestBody @Valid SettingsRequest request) {
        settings.updateHours(request.hours());
        settings.updateHourlyPlans(request.hourlyPlans());

        log.info("💾 [{}] Настройки сохранены пользователем. Применяем к текущей смене.",
                java.time.LocalDateTime.now());

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