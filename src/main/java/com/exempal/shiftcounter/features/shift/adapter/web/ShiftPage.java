package com.exempal.shiftcounter.features.shift.adapter.persistence;

import com.exempal.shiftcounter.core.PageModel;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.shift.application.projection.ShiftProjectionUseCase;
import com.exempal.shiftcounter.features.shift.application.projection.ShiftView;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.Map;

@Component
public class ShiftPage implements PageModel {

    private final ShiftProjectionUseCase projection;
    private final ProductionDayService productionDays;

    public ShiftPage(ShiftProjectionUseCase projection, ProductionDayService productionDays) {
        this.projection = projection;
        this.productionDays = productionDays;
    }

    @Override
    public String getPageName() {
        return "shift";
    }

    @Override
    public void populateModel(Model model) {
        ShiftView view = projection.buildView(productionDays.current().date());

        model.addAttribute("date", view.date());
        model.addAttribute("plan", view.plan());
        model.addAttribute("actual", view.actual());
        model.addAttribute("hours", view.hours());
        model.addAttribute("planSupplied", view.planSupplied());

        // 🔧 Обязательный атрибут для шаблона
        model.addAttribute("contentTemplate", "features/shift/shift");

        // Заглушки для шаблона, если пока не используются
        model.addAttribute("reasons", Map.of());
        model.addAttribute("explainedEnough", Map.of());
    }
}
