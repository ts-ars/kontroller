package com.exempal.shiftcounter.features.settings.adapter.web;

import com.exempal.shiftcounter.features.settings.application.SettingsGroupService;
import com.exempal.shiftcounter.features.settings.application.UpdateSettingsCommand;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/settings")
public class SettingsRestController {
    private final SettingsGroupService settings;

    public SettingsRestController(SettingsGroupService settings) {
        this.settings = settings;
    }

    @PostMapping("/{groupId}")
    public ResponseEntity<SettingsResponse> updateSettings(@PathVariable String groupId,
                                                            @RequestBody @Valid SettingsRequest request) {
        var updated = settings.update(new UpdateSettingsCommand(groupId, request.hours(),
                request.sensors1To4Plans(), request.sensor6Plans()));
        return ResponseEntity.ok(SettingsResponse.from(groupId, updated));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<SettingsResponse> getSettings(@PathVariable String groupId) {
        return ResponseEntity.ok(SettingsResponse.from(groupId, settings.getSnapshot(groupId)));
    }
}
