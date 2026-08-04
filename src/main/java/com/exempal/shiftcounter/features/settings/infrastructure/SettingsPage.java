package com.exempal.shiftcounter.features.settings.infrastructure;

import com.exempal.shiftcounter.core.PageModel;
import com.exempal.shiftcounter.features.settings.domain.SettingsPort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/settings")
public class SettingsPage implements PageModel {

    private final SettingsPort settings;

    public SettingsPage(SettingsPort settings) {
        this.settings = settings;
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

        settings.updateHours(hours);

        List<String> planStrings = plans.stream()
                .map(String::valueOf)
                .toList();
        settings.updateHourlyPlans(planStrings);

        return "redirect:/page/settings";
    }
}