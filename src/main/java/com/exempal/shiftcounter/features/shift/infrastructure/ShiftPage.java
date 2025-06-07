package com.exempal.shiftcounter.features.shift.infrastructure;

import com.exempal.shiftcounter.core.PageModel;
import com.exempal.shiftcounter.features.comment.StoppageEntry;
import com.exempal.shiftcounter.features.comment.StoppageRepository;
import com.exempal.shiftcounter.features.shift.application.ShiftPlannerUseCase;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ShiftPage implements PageModel {

    private final ShiftPlannerUseCase planner;
    private final StoppageRepository stoppages;

    public ShiftPage(ShiftPlannerUseCase planner, StoppageRepository stoppages) {
        this.planner = planner;
        this.stoppages = stoppages;
    }

    @Override
    public String getPageName() {
        return "shift";
    }

    @Override
    public void populateModel(Model model) {
        LocalDate today = LocalDate.now();

       /* Shift shift = planner.buildShift(today);
        int hourlyPlan = planner.getHourlyPlan();
        List<String> hours = shift.hours();
        List<Integer> actualByHour = shift.hourlyActuals();

        model.addAttribute("hours", hours);
        model.addAttribute("plan", hourlyPlan);
        model.addAttribute("actual", actualByHour);

        List<StoppageEntry> entries = stoppages.findByDate(today);
        Map<String, List<StoppageEntry>> reasonsByHour = entries.stream()
                .collect(Collectors.groupingBy(StoppageEntry::getTime));

        Map<String, Boolean> explainedEnough = new HashMap<>();
        for (int i = 0; i < hours.size(); i++) {
            String hour = hours.get(i);
            int actual = actualByHour.get(i);
            int loss = Math.max(0, hourlyPlan - actual);

            int explained = reasonsByHour.getOrDefault(hour, List.of()).stream()
                    .mapToInt(StoppageEntry::getCans)
                    .sum();

            explainedEnough.put(hour, explained >= loss);
        }

        model.addAttribute("reasons", reasonsByHour);
        model.addAttribute("explainedEnough", explainedEnough);

        */

        model.addAttribute("hours", List.of());
        model.addAttribute("plan", 0);
        model.addAttribute("actual", List.of());
        model.addAttribute("reasons", Map.of());
        model.addAttribute("explainedEnough", Map.of());

    }
}
