package com.exempal.shiftcounter.features.settings.infrastructure;

import com.exempal.shiftcounter.core.PageModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/settings")
public class SettingsPage implements PageModel {

    private final SettingsStorage storage;

    public SettingsPage(SettingsStorage storage) {
        this.storage = storage;
    }

    @Override
    public String getPageName() {
        return "settings";
    }

    // Основной метод, вызывается через PageModelResolver
    @Override
    public void populateModel(Model model, Map<String, String> params) {
        model.addAttribute("hours", storage.getHours());
        model.addAttribute("plans", storage.getHourlyPlans());
        model.addAttribute("ppm", storage.getPpm());
    }

    // Обязательная заглушка для старого контракта
    @Override
    public void populateModel(Model model) {
        populateModel(model, Map.of()); // можно оставить пустым или дублировать данные
    }

    @PostMapping
    public String updateSettings(@RequestParam("ppm") int ppm,
                                 @RequestParam("hours") List<String> hours,
                                 @RequestParam("plans") List<Integer> plans) {
        storage.setPpm(ppm);
        storage.setHours(hours);
        storage.setHourlyPlans(plans);
        return "redirect:/page/settings";
    }
}
