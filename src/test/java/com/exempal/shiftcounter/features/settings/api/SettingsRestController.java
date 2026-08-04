package com.exempal.shiftcounter.features.settings.api;

import com.exempal.shiftcounter.common.domain.EventPublisherPort;
import com.exempal.shiftcounter.features.settings.domain.SettingUpdatedEvent;
import com.exempal.shiftcounter.features.settings.domain.SettingsPort;
import com.exempal.shiftcounter.features.settings.infrastructure.ShiftSettingsApplier;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


    @RestController
    @RequestMapping("/api/settings")
    public class SettingsRestController {

        private final SettingsPort settings;
        private final EventPublisherPort events;
        private final ShiftSettingsApplier shiftSettingsApplier;

        public SettingsRestController(
                SettingsPort settings,
                EventPublisherPort events,
                ShiftSettingsApplier shiftSettingsApplier
        ) {
            this.settings = settings;
            this.events = events;
            this.shiftSettingsApplier = shiftSettingsApplier;
        }

        @PostMapping("/{key}")
        public void updateSetting(
                @PathVariable String key,
                @RequestBody @Valid SingleSettingRequest request
        ) {
            settings.update(key, request.value());
            events.publish(new SettingUpdatedEvent(key, request.value()));
        }

        @PostMapping
        public void updateSettings(@RequestBody @Valid SettingsRequest request) {
            settings.updateHours(request.hours());
            settings.updateHourlyPlans(request.hourlyPlans());

            events.publish(new SettingUpdatedEvent("allSettings", "updated"));
            shiftSettingsApplier.applySettingsToCurrentShift();
        }

        @GetMapping
        public SettingsResponse getSettings() {
            return new SettingsResponse(
                    settings.getHours(),
                    settings.getHourlyPlans()
            );
        }
    }

