package com.exempal.shiftcounter.features.comment.adapter.web;

import com.exempal.shiftcounter.features.comment.adapter.dto.ReconcileResponse;
import com.exempal.shiftcounter.features.comment.adapter.dto.StoppageViewDto;
import com.exempal.shiftcounter.features.comment.adapter.mapper.StoppageViewMapper;
import com.exempal.shiftcounter.features.comment.application.*;
import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/stoppages")
@RequiredArgsConstructor
public class StoppageController {
    private final StoppageRepository repository;
    private final ReconcileStoppagesUseCase reconcile;

    @PostMapping("/recalculate")
    public ResponseEntity<ReconcileResponse> recalculate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer intervalIndex,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime calculationTime) {
        LocalDateTime suppliedTime = calculationTime == null ? LocalDateTime.now() : calculationTime;
        LocalDate suppliedDate = date == null ? suppliedTime.toLocalDate() : date;
        ReconcileResult result = reconcile.reconcile(new ReconcileStoppagesCommand(suppliedDate,
                Stoppage.PRIMARY_SENSOR, intervalIndex, suppliedTime));
        return result.hasFatalDiagnostic()
                ? ResponseEntity.unprocessableEntity().body(ReconcileResponse.from(result))
                : ResponseEntity.ok(ReconcileResponse.from(result));
    }

    @GetMapping("/range")
    public List<StoppageViewDto> getRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return repository.findByShiftDateBetween(from, to).stream().map(StoppageViewMapper::toDto).toList();
    }
}
