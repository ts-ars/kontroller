package com.exempal.shiftcounter.features.shift.infrastructure;

import com.exempal.shiftcounter.core.PageModel;
import com.exempal.shiftcounter.features.shift.projection.ShiftProjectionUseCase;
import com.exempal.shiftcounter.features.shift.projection.ShiftView;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.Map;

@Component
public class ShiftPage implements PageModel {

    private final ShiftProjectionUseCase projection;

    public ShiftPage(ShiftProjectionUseCase projection) {
        this.projection = projection;
    }

    @Override
    public String getPageName() {
        return "shift";
    }

    @Override
    public void populateModel(Model model) {
        LocalDate today = LocalDate.now();
        ShiftView view = projection.buildView(today);

        model.addAttribute("date", view.date());
        model.addAttribute("plan", view.plan());
        model.addAttribute("actual", view.actual());
        model.addAttribute("hours", view.hours());

        // 🔧 Обязательный атрибут для шаблона
        model.addAttribute("contentTemplate", "features/shift/shift");

        // Заглушки для шаблона, если пока не используются
        model.addAttribute("reasons", Map.of());
        model.addAttribute("explainedEnough", Map.of());
    }
}
