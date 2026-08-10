package com.exempal.shiftcounter.features.settings.infrastructure;

import com.exempal.shiftcounter.core.PageModel;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.features.settings.application.SettingsGroupService;
import com.exempal.shiftcounter.features.settings.application.UpdateSettingsGroupCommand;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/settings")
public class SettingsPage implements PageModel {

    private final SettingsGroupService settings;

    public SettingsPage(SettingsGroupService settings) {
        this.settings = settings;
    }

    @Override
    public String getPageName() {
        return "settings";
    }

    @Override
    public void populateModel(Model model, Map<String, String> params) {
        String groupId = params.getOrDefault("groupId", SensorCatalog.GROUP_1);
        var group = settings.get(groupId);
        model.addAttribute("groupId", group.id());
        model.addAttribute("groupName", group.name());
        model.addAttribute("enabled", group.enabled());
        model.addAttribute("groups", List.of(SensorCatalog.GROUP_1, SensorCatalog.GROUP_2));
        model.addAttribute("plans", group.intervals().stream().map(value -> value.plan()).toList());
        model.addAttribute("hours", group.intervals().stream().map(value -> value.startTime().toString()).toList());
    }

    @Override
    public void populateModel(Model model) {
        populateModel(model, Map.of());
    }

    @PostMapping
    public String updateSettings(@RequestParam("groupId") String groupId,
                                 @RequestParam("hours") List<String> hours,
                                 @RequestParam("plans") List<Integer> plans) {
        var current = settings.get(groupId);
        settings.update(new UpdateSettingsGroupCommand(groupId, current.name(), current.enabled(), hours, plans));
        return "redirect:/page/settings?groupId=" + groupId;
    }
}
