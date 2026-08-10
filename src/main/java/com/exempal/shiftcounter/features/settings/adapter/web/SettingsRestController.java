package com.exempal.shiftcounter.features.settings.adapter.web;

import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.features.settings.application.SettingsGroupService;
import com.exempal.shiftcounter.features.settings.application.UpdateSettingsGroupCommand;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/settings")
public class SettingsRestController {

    private final SettingsGroupService settings;

    public SettingsRestController(SettingsGroupService settings) {
        this.settings = settings;
    }

    @PostMapping({"", "/{groupId}"})
    public void updateSettings(@PathVariable(required = false) String groupId,
                               @RequestBody @Valid SettingsRequest request) {
        String selected = groupId == null ? SensorCatalog.GROUP_1 : groupId;
        List<Integer> plans = request.hourlyPlans() == null ? null
                : request.hourlyPlans().stream().map(Integer::parseInt).toList();
        settings.update(new UpdateSettingsGroupCommand(selected,
                request.name() == null ? selected : request.name(),
                request.enabled() == null || request.enabled(), request.hours(),
                plans));
    }

    @GetMapping({"", "/{groupId}"})
    public ResponseEntity<SettingsResponse> getSettings(@PathVariable(required = false) String groupId) {
        String selected = groupId == null ? SensorCatalog.GROUP_1 : groupId;
        var current = settings.get(selected);
        return ResponseEntity.ok(new SettingsResponse(current.id(), current.name(), current.enabled(),
                current.intervals().stream().map(value -> value.startTime().toString()).toList(),
                current.intervals().stream().map(value -> String.valueOf(value.plan())).toList()));
    }
}
