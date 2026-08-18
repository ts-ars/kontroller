package com.exempal.shiftcounter.features.comment.adapter.web;

import com.exempal.shiftcounter.features.comment.adapter.dto.ReconcileResponse;
import com.exempal.shiftcounter.features.comment.adapter.dto.StoppageViewDto;
import com.exempal.shiftcounter.features.comment.adapter.mapper.StoppageViewMapper;
import com.exempal.shiftcounter.features.comment.application.*;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import com.exempal.shiftcounter.features.comment.application.ManualStoppageService;
import com.exempal.shiftcounter.features.comment.application.StoppageExclusionService;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/stoppages")
public class StoppageController {
    private final StoppageQueryUseCase stoppages;
    private final ReconcileStoppagesUseCase reconcile;
    private final ProductionDayService productionDays;
    private final ManualStoppageService manualStoppages;
    private final StoppageExclusionService exclusions;

    @Autowired
    public StoppageController(StoppageQueryUseCase stoppages, ReconcileStoppagesUseCase reconcile,
                              ProductionDayService productionDays, ManualStoppageService manualStoppages,
                              StoppageExclusionService exclusions) {
        this.stoppages = stoppages; this.reconcile = reconcile; this.productionDays = productionDays;
        this.manualStoppages = manualStoppages; this.exclusions = exclusions;
    }

    public StoppageController(StoppageQueryUseCase stoppages, ReconcileStoppagesUseCase reconcile,
                       ProductionDayService productionDays) {
        this(stoppages, reconcile, productionDays, null, null);
    }

    public record ManualRequest(LocalDate date, String sensorId, int intervalIndex,
                                LossCategory category, String comment, int minutes, int cans) {}
    public record ExclusionRequest(String reason) {}

    @PostMapping("/manual")
    public StoppageViewDto createManual(@RequestBody ManualRequest request) {
        return StoppageViewMapper.toDto(manualStoppages.create(request.date(), request.sensorId(),
                request.intervalIndex(), request.category(), request.comment(), request.minutes(), request.cans()));
    }

    @PostMapping("/{id}/exclude")
    public void exclude(@PathVariable long id, @RequestBody(required = false) ExclusionRequest request) {
        exclusions.exclude(id, request == null ? "" : request.reason());
    }

    @PostMapping("/{id}/restore")
    public void restore(@PathVariable long id) { exclusions.restore(id); }

    @PostMapping("/recalculate")
    public ResponseEntity<ReconcileResponse> recalculate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer intervalIndex,
            @RequestParam(defaultValue = "sensor-1") String sensorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime calculationTime) {
        LocalDateTime suppliedTime = calculationTime == null ? productionDays.now() : calculationTime;
        LocalDate suppliedDate = date == null ? productionDays.resolve(suppliedTime).date() : date;
        ReconcileResult result = reconcile.reconcile(new ReconcileStoppagesCommand(suppliedDate,
                sensorId, intervalIndex, suppliedTime));
        return result.hasFatalDiagnostic()
                ? ResponseEntity.unprocessableEntity().body(ReconcileResponse.from(sensorId, result))
                : ResponseEntity.ok(ReconcileResponse.from(sensorId, result));
    }

    @GetMapping("/range")
    public List<StoppageViewDto> getRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "sensor-1") String sensorId) {
        return stoppages.findBetween(from, to, sensorId).stream()
                .map(StoppageViewMapper::toDto).toList();
    }
}
