package com.exempal.shiftcounter.features.settings.infrastructure;

import com.exempal.shiftcounter.core.PageModel;
import com.exempal.shiftcounter.features.settings.domain.SettingsPort;
import com.exempal.shiftcounter.features.shift.application.ShiftIntervalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/settings")
public class SettingsPage implements PageModel {

    private final SettingsPort settings;
    private final ShiftIntervalService intervals;
    private final ShiftSettingsApplier settingsApplier;

    public SettingsPage(SettingsPort settings, ShiftIntervalService intervals,
                        ShiftSettingsApplier settingsApplier) {
        this.settings = settings;
        this.intervals = intervals;
        this.settingsApplier = settingsApplier;
    }

    @Override
    public String getPageName() {
        return "settings";
    }

    @Override
    public void populateModel(Model model, Map<String, String> params) {
        List<Integer> plans = settings.getHourlyPlans().stream()
                .map(Integer::parseInt)
                .toList();

        model.addAttribute("plans", plans);
        model.addAttribute("hours", settings.getHours());
    }

    @Override
    public void populateModel(Model model) {
        populateModel(model, Map.of());
    }

    @PostMapping
    public String updateSettings(@RequestParam("hours") List<String> hours,
                                 @RequestParam("plans") List<Integer> plans) {
        if (hours == null || hours.isEmpty() || plans == null || hours.size() != plans.size()) {
            throw new IllegalArgumentException("Time and Plan must have the same non-empty size");
        }
        if (plans.stream().anyMatch(value -> value == null || value < 0)) {
            throw new IllegalArgumentException("Plan must not be negative");
        }
        intervals.resolve(LocalDate.of(2000, 1, 1), hours, plans.size());

        settings.updateHours(hours);

        List<String> planStrings = plans.stream()
                .map(String::valueOf)
                .toList();
        settings.updateHourlyPlans(planStrings);
        settingsApplier.applySettingsToCurrentShift();

        return "redirect:/page/settings";
    }
}
