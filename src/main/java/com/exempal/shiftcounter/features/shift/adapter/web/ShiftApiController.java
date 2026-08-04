package com.exempal.shiftcounter.features.shift.adapter.web;

import com.exempal.shiftcounter.features.shift.application.ShiftExtenderService;
import com.exempal.shiftcounter.features.shift.application.ShiftPlannerUseCase;
import com.exempal.shiftcounter.features.shift.application.ShiftTimeHelper;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.projection.ShiftProjectionUseCase;
import com.exempal.shiftcounter.features.shift.projection.ShiftView;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/shift")
public class ShiftApiController {

    private final ShiftPlannerUseCase shiftPlanner;
    private final ShiftExtenderService shiftExtender;
    private final ShiftProjectionUseCase shiftProjection;
    private final ShiftTimeHelper shiftTimeHelper;

    public ShiftApiController(ShiftPlannerUseCase shiftPlanner, ShiftExtenderService shiftExtender, ShiftProjectionUseCase shiftProjection, ShiftTimeHelper shiftTimeHelper) {
        this.shiftPlanner = shiftPlanner;
        this.shiftExtender = shiftExtender;
        this.shiftProjection = shiftProjection;
        this.shiftTimeHelper = shiftTimeHelper;
    }

    @GetMapping("/current")
    public ShiftView getCurrentShift() {
        // единый момент времени
        LocalDateTime ts = LocalDateTime.now();
        LocalDate today = ts.toLocalDate();

        // 1) Сегодня: берём/создаём, продлеваем по ts
        Shift todayShift = shiftPlanner.getOrCreateShift(today);
        Shift todayExt = shiftExtender.extendIfNeeded(ts, todayShift);
        if (todayExt != todayShift) {
            // фиксируем расширение, чтобы buildView(date) увидел новые labels
            shiftPlanner.applySettingsAndUpdate(todayExt);
        }

        // Если ts попадает в сегодняшние интервалы — отдаём сегодняшнюю смену
        if (shiftTimeHelper.contains(today, todayExt.getHourlyLabels(), ts)) {
            return shiftProjection.buildView(today);
        }

        // 2) Иначе, если время до первого сегодняшнего слота — пробуем "вчера"
        String firstLabel = todayExt.getHourlyLabels().get(0);
        LocalTime firstToday = shiftTimeHelper.resolveStartTime(firstLabel, today).toLocalTime();
        if (ts.toLocalTime().isBefore(firstToday)) {
            LocalDate y = today.minusDays(1);

            // ВАРИАНТ 1 (если можно создавать задним числом):
            Shift yShift = shiftPlanner.getOrCreateShift(y);

            // ВАРИАНТ 2 (если нельзя создавать задним числом) — раскомментируй и инжектни порт:
            // Optional<Shift> opt = actualDataPort.findByDate(y);
            // if (opt.isEmpty()) return shiftProjection.buildView(today);
            // Shift yShift = opt.get();

            Shift yExt = shiftExtender.extendIfNeeded(ts, yShift);
            if (yExt != yShift) {
                shiftPlanner.applySettingsAndUpdate(yExt);
            }
            if (shiftTimeHelper.contains(y, yExt.getHourlyLabels(), ts)) {
                return shiftProjection.buildView(y);
            }
        }

        // 3) ВНЕ ОКОН — отдаём сегодня как дефолт
        return shiftProjection.buildView(today);
    }
}