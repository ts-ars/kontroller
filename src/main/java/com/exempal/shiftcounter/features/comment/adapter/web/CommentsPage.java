package com.exempal.shiftcounter.features.comment.adapter.web;

import com.exempal.shiftcounter.core.PageModel;
import com.exempal.shiftcounter.features.comment.adapter.dto.LossExplanationResponse;
import com.exempal.shiftcounter.features.comment.adapter.dto.LossRowDto;
import com.exempal.shiftcounter.features.comment.application.CommentsReadUseCase;
import com.exempal.shiftcounter.features.comment.application.StoppageTimeService;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.features.shift.application.projection.ShiftSlice;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;

@Component
public class CommentsPage implements PageModel {
    private final CommentsReadUseCase useCase;
    private final StoppageTimeService timeService;
    private final ProductionDayService productionDays;

    public CommentsPage(CommentsReadUseCase useCase, StoppageTimeService timeService,
                        ProductionDayService productionDays) {
        this.useCase = useCase;
        this.timeService = timeService;
        this.productionDays = productionDays;
    }

    @Override public String getPageName() { return "comment"; }

    @Override
    public void populateModel(Model model) {
        populateModel(model, Map.of());
    }

    @Override
    public void populateModel(Model model, Map<String, String> params) {
        String sensorId = params.getOrDefault("sensorId", SensorCatalog.SENSOR_1);
        SensorCatalog.require(sensorId);
        ShiftSlice slice = ShiftSlice.from(params.get("shift"));
        var productionDate = productionDays.current().date();
        var data = useCase.read(productionDate, sensorId);
        model.addAttribute("sensorId", sensorId);
        model.addAttribute("shiftSlice", slice.id());
        model.addAttribute("shiftLabel", slice.label());
        model.addAttribute("sensors", SensorCatalog.all());
        model.addAttribute("readOnlyAggregation", SensorCatalog.SENSOR_5.equals(sensorId));
        model.addAttribute("sourceComments", data.sourceComments().stream()
                .map(source -> new CommentsReadUseCase.SourceComments(source.sensorId(), source.rows().stream()
                        .filter(row -> slice.contains(productionDate, row.time())).toList()))
                .toList());
        if (data.shift() == null && !SensorCatalog.SENSOR_5.equals(sensorId)) {
            model.addAttribute("losses", List.of());
            model.addAttribute("alerts", List.of("No shift found for today"));
            return;
        }
        if (data.shift() != null) model.addAttribute("shiftId", data.shift().getId());
        List<LossRowDto> rows = data.rows().stream()
                .filter(stoppage -> slice.contains(productionDate, stoppage.startedAt()))
                .map(stoppage -> {
            var childRows = stoppage.explanations().stream().map(LossExplanationResponse::from).toList();
            long allocated = stoppage.allocatedMinutes();
            return new LossRowDto(stoppage.id(), stoppage.detectionKey(), timeService.getPreciseTime(stoppage),
                    stoppage.roundedMinutes(), stoppage.lostCans(), stoppage.detectionType().name(),
                    stoppage.state(), stoppage.explanationStatus(), allocated,
                    Math.max(0, stoppage.roundedMinutes() - allocated), stoppage.version(), childRows);
        }).toList();
        var alerts = data.missing().stream()
                .filter(value -> slice.contains(productionDate, value.startedAt()))
                .map(value -> "Missing explanation for stoppage at " + timeService.getPreciseTime(value))
                .toList();
        model.addAttribute("losses", rows);
        model.addAttribute("alerts", alerts);
    }
}
