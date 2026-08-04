package com.exempal.shiftcounter.features.settings.infrastructure;

import com.exempal.shiftcounter.common.domain.EventPublisherPort;
import com.exempal.shiftcounter.features.settings.domain.Settings;
import com.exempal.shiftcounter.features.shift.domain.*;
import com.exempal.shiftcounter.features.shift.projection.ShiftProjectionUseCase;
import com.exempal.shiftcounter.features.shift.projection.ShiftView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public ShiftSettingsApplier(
            ShiftInitializer shiftInitializer,
            ShiftSettingsProvider settingsProvider,
            EventPublisherPort events,
            ShiftProjectionUseCase projectionUseCase,
            ActualDataPort actualDataPort,
            ShiftMetricsCalculator metricsCalculator
    ) {
        this.shiftInitializer = shiftInitializer;
        this.settingsProvider = settingsProvider;
        this.events = events;
        this.projectionUseCase = projectionUseCase;
        this.actualDataPort = actualDataPort;
        this.metricsCalculator = metricsCalculator;
    }

    public void applySettingsToCurrentShift() {
        log.info("🔄 Перезагрузка настроек перед применением к текущей смене");
        settingsProvider.reload();

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        log.info("🧮 [{}] Применяем настройки к смене: {}", now, today);

        Shift current = actualDataPort.findByDate(today).orElse(null);

        if (current == null) {
            shiftInitializer.createNewShift(today);
            log.info("📅 [{}] Смены не было — создана новая.", now);
        } else {
            Shift updated = applyIfChanged(current);
            actualDataPort.save(updated);
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
        Settings settings = settingsProvider.get();
        ShiftMetrics metrics = metricsCalculator.calculateFor(settings);

        // Единый помощник: дополняем/обрезаем список факта до нужной длины
        List<Integer> updatedActuals = normalize(shift.getHourlyActualValues(), metrics.labels().size());

        log.info("🔧 Обновлены часы смены: {}", metrics.labels());
        log.info("📊 Применяем планы: {}", metrics.plans());

        return shift.withUpdatedStructure(metrics.labels(), metrics.plans(), updatedActuals);
    }

    /** Применение настроек только если что-то изменилось (порядок меток/планы). */
    public Shift applyIfChanged(Shift current) {
        settingsProvider.reload();
        Settings settings = settingsProvider.get();
        ShiftMetrics metrics = metricsCalculator.calculateFor(settings); // содержит labels() и plans()

        List<String> oldLabels = current.getHourlyLabels();
        List<Integer> oldPlans  = normalize(current.getHourlyPlanValues(),  metrics.labels().size());
        List<Integer> oldActual = normalize(current.getHourlyActualValues(), metrics.labels().size());

        boolean labelsChanged = !oldLabels.equals(metrics.labels());
        boolean planChanged   = !oldPlans.equals(metrics.plans());

        if (!labelsChanged && !planChanged) {
            log.debug("✅ Настройки не изменились, смена актуальна: {}", current.getDate());
            return current;
        }

        // Если порядок меток меняется — переносим факт по названиям меток
        List<Integer> remappedActual = labelsChanged
                ? remapByLabel(oldLabels, metrics.labels(), oldActual)
                : oldActual;

        log.info("🔧 Обновлены часы смены: {}", metrics.labels());
        log.info("📊 Применяем планы: {}", metrics.plans());

        Shift updated = current.withUpdatedStructure(metrics.labels(), metrics.plans(), remappedActual);

        // Сохраняем и пушим View
        actualDataPort.save(updated);
        ShiftView view = projectionUseCase.buildView(updated.getDate());
        events.publish(new ShiftUpdatedEvent(view.date(), view.actual(), view.plan(), view.hours()));

        return updated;
    }

    /** Перенос значений факта, если поменялся порядок меток. */
    private List<Integer> remapByLabel(List<String> fromLabels, List<String> toLabels, List<Integer> fromValues) {
        Map<String, Integer> pos = new HashMap<>();
        for (int i = 0; i < fromLabels.size(); i++) pos.put(fromLabels.get(i), i);

        List<Integer> out = new ArrayList<>(toLabels.size());
        for (String lbl : toLabels) {
            Integer i = pos.get(lbl);
            out.add(i != null && i < fromValues.size() ? fromValues.get(i) : 0);
        }
        return out;
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