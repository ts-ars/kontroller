package com.exempal.shiftcounter.features.shift.adapter.persistence;

import com.exempal.shiftcounter.core.PageModel;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.shift.application.projection.ShiftProjectionUseCase;
import com.exempal.shiftcounter.features.shift.application.projection.ShiftSlice;
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

    @Override public String getPageName() { return "shift"; }

    @Override
    public void populateModel(Model model) {
        populateModel(model, Map.of());
    }

    @Override
    public void populateModel(Model model, Map<String, String> params) {
        String sensorGroup = "5-6".equals(params.get("sensors")) ? "5-6" : "1-4";
        ShiftSlice slice = ShiftSlice.from(params.get("shift"));
        var date = productionDays.current().date();
        var views = SensorCatalog.all().stream()
                .map(sensor -> projection.buildView(date, sensor.id().value(), slice))
                .filter(view -> sensorGroup.equals("1-4")
                        ? !view.sensorId().equals(SensorCatalog.SENSOR_5)
                            && !view.sensorId().equals(SensorCatalog.SENSOR_6)
                        : view.sensorId().equals(SensorCatalog.SENSOR_5)
                            || view.sensorId().equals(SensorCatalog.SENSOR_6))
                .toList();
        model.addAttribute("date", date);
        model.addAttribute("views", views);
        model.addAttribute("sensorGroup", sensorGroup);
        model.addAttribute("shiftSlice", slice.id());
        model.addAttribute("shiftLabel", slice.label());
    }
}
