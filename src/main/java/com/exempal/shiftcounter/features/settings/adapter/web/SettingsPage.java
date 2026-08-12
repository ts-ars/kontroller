package com.exempal.shiftcounter.features.settings.adapter.web;

import com.exempal.shiftcounter.core.PageModel;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.features.settings.application.SettingsGroupService;
import com.exempal.shiftcounter.features.settings.application.UpdateSettingsCommand;
import com.exempal.shiftcounter.features.settings.domain.SettingsSnapshot;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        String groupId = params.getOrDefault("groupId", SensorCatalog.SHARED_SETTINGS_GROUP);
        SettingsSnapshot snapshot = settings.getSnapshot(groupId);
        model.addAttribute("groupId", groupId);
        model.addAttribute("rows", snapshot.rows());
        model.addAttribute("standardRowCount", SettingsSnapshot.STANDARD_ROW_COUNT);
        model.addAttribute("sharedTotal", snapshot.sharedTotal());
        model.addAttribute("sensor5Total", snapshot.sensor5Total());
        model.addAttribute("sensor6Total", snapshot.sensor6Total());
    }

    @Override
    public void populateModel(Model model) {
        populateModel(model, Map.of());
    }

    @PostMapping
    public String updateSettings(@RequestParam("groupId") String groupId,
                                 @RequestParam("hours") List<String> hours,
                                 @RequestParam("sharedPlans") List<Integer> sharedPlans,
                                 @RequestParam("sensor6Plans") List<Integer> sensor6Plans) {
        settings.update(new UpdateSettingsCommand(groupId, hours, sharedPlans, sensor6Plans));
        return "redirect:/page/settings?groupId=" + groupId;
    }
}
