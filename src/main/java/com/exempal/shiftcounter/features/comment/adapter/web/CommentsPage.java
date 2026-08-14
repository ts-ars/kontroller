package com.exempal.shiftcounter.features.comment.adapter.web;

import com.exempal.shiftcounter.core.PageModel;
import com.exempal.shiftcounter.features.comment.adapter.dto.LossExplanationResponse;
import com.exempal.shiftcounter.features.comment.adapter.dto.LossRowDto;
import com.exempal.shiftcounter.features.comment.application.CommentsReadUseCase;
import com.exempal.shiftcounter.features.comment.application.StoppageTimeService;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.shift.application.ShiftIntervalService;
import com.exempal.shiftcounter.features.shift.application.ShiftSettingsPort;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.features.shift.application.projection.ShiftSlice;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.stream.IntStream;

@Component
public class CommentsPage implements PageModel {
    private final CommentsReadUseCase useCase;
    private final StoppageTimeService timeService;
    private final ProductionDayService productionDays;
    private final ShiftSettingsPort settings;
    private final ShiftIntervalService intervals;

    public CommentsPage(CommentsReadUseCase useCase, StoppageTimeService timeService,
                        ProductionDayService productionDays, ShiftSettingsPort settings,
                        ShiftIntervalService intervals) {
        this.useCase = useCase;
        this.timeService = timeService;
        this.productionDays = productionDays;
        this.settings = settings;
        this.intervals = intervals;
    }

    @Override public String getPageName() { return "comment"; }

    @Override
    public void populateModel(Model model) {
        populateModel(model, Map.of());
    }

    @Override
    public void populateModel(Model model, Map<String, String> params) {
        String sensorId = params.getOrDefault("sensorId", SensorCatalog.SENSOR_5);
        SensorCatalog.require(sensorId);
        String requestedShift = params.get("shift");
        ShiftSlice slice = requestedShift == null || requestedShift.isBlank()
                ? ShiftSlice.current(productionDays.now().toLocalTime())
                : ShiftSlice.from(requestedShift);
        var productionDate = parseDate(params.get("date"), productionDays.current().date());
        var data = useCase.read(productionDate, sensorId);
        model.addAttribute("sensorId", sensorId);
        model.addAttribute("shiftSlice", slice.id());
        model.addAttribute("shiftLabel", slice.label());
        model.addAttribute("productionDate", productionDate);
        model.addAttribute("sensors", SensorCatalog.all());
        model.addAttribute("readOnlyAggregation", SensorCatalog.SENSOR_5.equals(sensorId));
        model.addAttribute("sourceComments", data.sourceComments().stream()
                .map(source -> new CommentsReadUseCase.SourceComments(source.sensorId(), source.rows().stream()
                        .filter(row -> slice.contains(productionDate, row.time())).toList()))
                .toList());
        if (data.shift() != null) model.addAttribute("shiftId", data.shift().getId());
        var now = productionDays.now();
        if (!SensorCatalog.SENSOR_5.equals(sensorId)) {
            var configured = settings.getForSensor(sensorId);
            var labels = data.shift() == null ? configured.labels() : data.shift().getHourlyLabels();
            var plans = data.shift() == null ? configured.plans() : data.shift().getHourlyPlanValues();
            var timeline = intervals.resolve(productionDate, labels, plans.size());
            model.addAttribute("intervalOptions", IntStream.range(0, timeline.size())
                    .filter(index -> index < plans.size())
                    .filter(index -> slice.contains(productionDate, timeline.get(index).start()))
                    .filter(index -> !productionDate.equals(productionDays.current().date())
                            || !timeline.get(index).start().isAfter(now))
                    .mapToObj(index -> new IntervalOption(index, labels.get(index), plans.get(index),
                            Math.toIntExact(timeline.get(index).duration().toMinutes())))
                    .toList());
        }
        List<LossRowDto> rows = data.rows().stream()
                .filter(stoppage -> !stoppage.endedAt().isAfter(now))
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
                .filter(value -> !value.endedAt().isAfter(now))
                .filter(value -> slice.contains(productionDate, value.startedAt()))
                .map(value -> "Missing explanation for stoppage at " + timeService.getPreciseTime(value))
                .toList();
        model.addAttribute("losses", rows);
        model.addAttribute("alerts", alerts);
    }

    private LocalDate parseDate(String raw, LocalDate fallback) {
        try { return raw == null ? fallback : LocalDate.parse(raw); }
        catch (DateTimeParseException ignored) { return fallback; }
    }

    public record IntervalOption(int index, String label, int plan, int durationMinutes) {}
}
