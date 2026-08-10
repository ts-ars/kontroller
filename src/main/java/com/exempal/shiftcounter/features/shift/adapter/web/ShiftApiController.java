package com.exempal.shiftcounter.features.shift.adapter.web;

import com.exempal.shiftcounter.features.shift.application.ShiftExtenderService;
import com.exempal.shiftcounter.features.shift.application.ShiftPlannerUseCase;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.projection.ShiftProjectionUseCase;
import com.exempal.shiftcounter.features.shift.projection.ShiftView;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/shift")
public class ShiftApiController {

    private final ShiftPlannerUseCase shiftPlanner;
    private final ShiftExtenderService shiftExtender;
    private final ShiftProjectionUseCase shiftProjection;
    private final ProductionDayService productionDays;

    public ShiftApiController(ShiftPlannerUseCase shiftPlanner, ShiftExtenderService shiftExtender,
                              ShiftProjectionUseCase shiftProjection, ProductionDayService productionDays) {
        this.shiftPlanner = shiftPlanner;
        this.shiftExtender = shiftExtender;
        this.shiftProjection = shiftProjection;
        this.productionDays = productionDays;
    }

    @GetMapping("/current")
    public ShiftView getCurrentShift(@RequestParam(defaultValue = "sensor-1") String sensorId) {
        LocalDateTime timestamp = productionDays.now();
        var day = productionDays.resolve(timestamp);
        Shift shift = shiftPlanner.getOrCreateShift(day.date(), sensorId);
        Shift extended = shiftExtender.extendIfNeeded(timestamp, shift);
        if (!extended.getHourlyLabels().equals(shift.getHourlyLabels())) shiftPlanner.updateShift(extended);
        return shiftProjection.buildView(day.date(), sensorId);
    }
}
