package com.exempal.shiftcounter.features.settings.infrastructure;

import com.exempal.shiftcounter.common.domain.EventPublisherPort;
import com.exempal.shiftcounter.features.settings.domain.Settings;
import com.exempal.shiftcounter.features.shift.domain.*;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.shift.application.ShiftTimeCorrectionService;
import com.exempal.shiftcounter.features.shift.projection.ShiftProjectionUseCase;
import com.exempal.shiftcounter.features.shift.projection.ShiftView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class ShiftSettingsApplier {

    private final ShiftInitializer shiftInitializer;
    private final ShiftSettingsProvider settingsProvider;
    private final EventPublisherPort events;
    private final ShiftProjectionUseCase projectionUseCase;
    private final ActualDataPort actualDataPort;
    private final ShiftMetricsCalculator metricsCalculator;
    private final ProductionDayService productionDays;
    private final ShiftTimeCorrectionService corrections;

    public ShiftSettingsApplier(
            ShiftInitializer shiftInitializer,
            ShiftSettingsProvider settingsProvider,
            EventPublisherPort events,
            ShiftProjectionUseCase projectionUseCase,
            ActualDataPort actualDataPort,
            ShiftMetricsCalculator metricsCalculator,
            ProductionDayService productionDays,
            ShiftTimeCorrectionService corrections
    ) {
        this.shiftInitializer = shiftInitializer;
        this.settingsProvider = settingsProvider;
        this.events = events;
        this.projectionUseCase = projectionUseCase;
        this.actualDataPort = actualDataPort;
        this.metricsCalculator = metricsCalculator;
        this.productionDays = productionDays;
        this.corrections = corrections;
    }

    public void applySettingsToCurrentShift() {
        log.info("🔄 Перезагрузка настроек перед применением к текущей смене");
        LocalDateTime now = productionDays.now();
        var today = productionDays.resolve(now).date();
        log.info("🧮 [{}] Применяем настройки к смене: {}", now, today);

        Shift current = actualDataPort.findByDate(today).orElse(null);

        if (current == null) {
            shiftInitializer.createNewShift(today);
            log.info("📅 [{}] Смены не было — создана новая.", now);
        } else {
            Shift updated = applyIfChanged(current, now);
            log.info("♻️ [{}] Смена обновлена с новыми настройками: {}", now, updated.getDate());
        }

        ShiftView view = projectionUseCase.buildView(today);
        events.publish(new ShiftUpdatedEvent(view.date(), view.actual(), view.plan(), view.hours()));

        log.info("✅ [{}] Настройки применены и смена отправлена через WS", now);
    }

    public Settings getCurrentSettings() {
        return settingsProvider.get();
    }

    /** Применение текущих настроек без сравнения (используется при явном аплае). */
    public Shift apply(Shift shift) {
        Settings settings = settingsProvider.getForSensor(shift.getSensorId());
        ShiftMetrics metrics = metricsCalculator.calculateFor(settings);

        // Единый помощник: дополняем/обрезаем список факта до нужной длины
        List<String> labels = new ArrayList<>(metrics.labels());
        int oldPlanCount = shift.getHourlyPlanValues().size();
        if (shift.getHourlyLabels().size() > oldPlanCount) {
            labels.addAll(shift.getHourlyLabels().subList(oldPlanCount, shift.getHourlyLabels().size()));
        }
        List<Integer> updatedActuals = normalize(shift.getHourlyActualValues(), labels.size());

        log.info("🔧 Обновлены часы смены: {}", metrics.labels());
        log.info("📊 Применяем планы: {}", metrics.plans());

        return shift.withUpdatedStructure(labels, metrics.plans(), updatedActuals);
    }

    /** Применение настроек только если что-то изменилось (порядок меток/планы). */
    public Shift applyIfChanged(Shift current) {
        return applyIfChanged(current, productionDays.now());
    }

    public Shift applyIfChanged(Shift current, LocalDateTime calculationTime) {
        Settings settings = settingsProvider.getForSensor(current.getSensorId());
        ShiftMetrics metrics = metricsCalculator.calculateFor(settings); // содержит labels() и plans()

        List<String> oldLabels = current.getHourlyLabels();
        int oldPlanCount = current.getHourlyPlanValues().size();
        List<String> oldConfiguredLabels = oldLabels.subList(0, Math.min(oldPlanCount, oldLabels.size()));
        List<Integer> oldPlans = current.getHourlyPlanValues();

        boolean labelsChanged = !oldConfiguredLabels.equals(metrics.labels());
        boolean planChanged   = !oldPlans.equals(metrics.plans());

        if (!labelsChanged && !planChanged) {
            log.debug("✅ Настройки не изменились, смена актуальна: {}", current.getDate());
            return current;
        }

        log.info("🔧 Обновлены часы смены: {}", metrics.labels());
        log.info("📊 Применяем планы: {}", metrics.plans());

        Shift updated = corrections.apply(current, metrics.labels(), metrics.plans(), labelsChanged,
                calculationTime);

        ShiftView view = projectionUseCase.buildView(updated.getDate(), updated.getSensorId());
        events.publish(new ShiftUpdatedEvent(view.date(), updated.getSensorId(), view.actual(), view.plan(), view.hours()));

        return updated;
    }

    /** Универсальный помощник: привести список к нужной длине (дополнить нулями/обрезать). */
    private List<Integer> normalize(List<Integer> source, int size) {
        List<Integer> r = new ArrayList<>(size);
        int n = (source == null ? 0 : source.size());
        for (int i = 0; i < size; i++) {
            r.add(i < n ? source.get(i) : 0);
        }
        return r;
    }
}
