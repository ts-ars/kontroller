package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.adapter.dto.CommentRowDto;
import com.exempal.shiftcounter.features.comment.calculator.StoppageCalculator;
import com.exempal.shiftcounter.features.comment.domain.StoppageEntry;
import com.exempal.shiftcounter.features.shift.application.ShiftPlannerUseCase;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftMetrics;
import com.exempal.shiftcounter.features.shift.domain.ShiftMetricsCalculator;
import com.exempal.shiftcounter.features.settings.infrastructure.ShiftSettingsProvider;
import com.exempal.shiftcounter.features.shift.infrastructure.ShiftEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final ShiftPlannerUseCase shiftPlanner;
    private final ShiftMetricsCalculator metricsCalculator;
    private final ShiftSettingsProvider settingsProvider;
    private final StoppageCalculator stoppageCalculator;
    // ✅ добавляем репозиторий для гарантированного сохранения
    private final com.exempal.shiftcounter.features.comment.domain.StoppageRepository stoppageRepository;

    @org.springframework.transaction.annotation.Transactional
    @Override
    public void saveComments(List<CommentRowDto> rows, LocalDate date) {
        Shift shift = shiftPlanner.getOrCreateShift(date);
        ShiftEntity entity = shift.getEntity();
        if (entity == null) {
            // на всякий случай, если где-то не проставили
            entity = com.exempal.shiftcounter.features.shift.infrastructure.ShiftEntity.fromDomain(shift);
            shift.setEntity(entity);
        }

        // метрики для расчёта cans при необходимости
        ShiftMetrics metrics = metricsCalculator.calculateFor(
                settingsProvider.get(),
                shift.getHourlyLabels()
        );

        // Текущее состояние user-editable в памяти
        List<StoppageEntry> currentUser = entity.getStoppages().stream()
                .filter(e -> e.getType() != null && e.getType().isUserEditable())
                .toList();

        java.util.Map<Long, StoppageEntry> byId = currentUser.stream()
                .filter(e -> e.getId() != null)
                .collect(java.util.stream.Collectors.toMap(StoppageEntry::getId, e -> e));

        java.util.List<StoppageEntry> toSaveOrUpdate = new java.util.ArrayList<>();

        for (CommentRowDto row : rows) {
            // парсим и валидируем тип
            if (row.type() == null) throw new IllegalArgumentException("type is required");
            var typeEnum = com.exempal.shiftcounter.features.comment.domain.StoppageType.valueOf(row.type().toUpperCase());
            if (!typeEnum.isUserEditable()) {
                throw new IllegalArgumentException("Only user-editable types are allowed");
            }

            int hourIndex = row.hourIndex();
            double minutes = row.minutes();
            int cans = row.cans() > 0
                    ? row.cans()
                    : (int) Math.round(minutes * metrics.canPerMinute().get(hourIndex));

            if (row.id() != null) {
                // UPDATE
                StoppageEntry e = byId.get(row.id());
                if (e == null) {
                    throw new IllegalArgumentException("Stoppage not found or not user-editable: id=" + row.id());
                }

                // Разрешаем менять час, минуты, банки, тип, комментарий
                e.setHourIndex(hourIndex);
                e.setMinutes(minutes);
                e.setCans(cans);
                e.setType(typeEnum);
                e.setComment(row.comment());
                // time/minuteOffset UI не присылает — стартовым временем остаётся label + текущий offset (обычно 0)

                toSaveOrUpdate.add(e);

            } else {
                // CREATE — создаём новую запись из DTO
                StoppageEntry e = new StoppageEntry();
                e.setShift(entity);
                e.setHourIndex(hourIndex);
                e.setMinutes(minutes);
                e.setCans(cans);
                e.setType(typeEnum);
                e.setComment(row.comment());
                toSaveOrUpdate.add(e);
            }
        }

        // 💾 гарантированно сохраняем (update/insert по id)
        stoppageRepository.saveAll(toSaveOrUpdate);

        // Сохраняем смену (если у тебя тут ещё что-то обновляется по Shift)
        shiftPlanner.updateShift(shift);
    }
}