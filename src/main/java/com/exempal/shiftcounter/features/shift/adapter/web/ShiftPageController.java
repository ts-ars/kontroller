package com.exempal.shiftcounter.features.shift.adapter.web;

import com.exempal.shiftcounter.features.shift.projection.ShiftProjectionUseCase;
import com.exempal.shiftcounter.features.shift.projection.ShiftView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class ShiftPageController {

    private final ShiftProjectionUseCase shiftProjectionUseCase;

    @GetMapping("/shift")
    public String showShift(Model model) {
        ShiftView view = shiftProjectionUseCase.buildView(LocalDate.now());

        model.addAttribute("hours", view.hours());
        model.addAttribute("plan", view.plan());
        model.addAttribute("actual", view.actual());

        return "features/shift/shift"; // HTML шаблон
    }
}
