package com.exempal.shiftcounter.features.shift.adapter.web;

import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.shift.application.projection.ShiftProjectionUseCase;
import com.exempal.shiftcounter.features.shift.application.projection.ShiftView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ShiftPageController {

    private final ShiftProjectionUseCase shiftProjectionUseCase;
    private final ProductionDayService productionDays;

    @GetMapping("/shift")
    public String showShift(Model model) {
        ShiftView view = shiftProjectionUseCase.buildView(productionDays.current().date());

        model.addAttribute("hours", view.hours());
        model.addAttribute("plan", view.plan());
        model.addAttribute("actual", view.actual());
        model.addAttribute("planSupplied", view.planSupplied());

        return "features/shift/shift"; // HTML шаблон
    }
}
