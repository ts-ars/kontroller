package com.exempal.shiftcounter.features.comment.adapter.persistence;

import com.exempal.shiftcounter.features.comment.application.StoppageRepository;
import com.exempal.shiftcounter.features.comment.domain.LossExplanation;
import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import com.exempal.shiftcounter.features.comment.domain.StoppageState;
import com.exempal.shiftcounter.features.shift.infrastructure.ShiftJpaRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
@Transactional
public class JpaStoppageAdapter implements StoppageRepository {
    private final StoppageJpaRepository repository;
    private final ShiftJpaRepository shifts;

    @Override
    @Transactional(readOnly = true)
    public Optional<Stoppage> findById(long id) {
        return repository.findById(id).filter(this::isSystemLoss).map(this::toDomain);
    }

    @Override
    public Optional<Stoppage> findForUpdateById(long id) {
        return repository.findForUpdateById(id).filter(this::isSystemLoss).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Stoppage> findByShiftDate(LocalDate date) {
        return repository.findSystemByShiftDate(date).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Stoppage> findByShiftDateBetween(LocalDate from, LocalDate to) {
        return repository.findSystemByShiftDateBetween(from, to).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Stoppage> findActiveByShiftAndInterval(long shiftId, int intervalIndex) {
        return repository.findActiveByShiftAndInterval(shiftId, intervalIndex, StoppageState.ACTIVE)
                .stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Stoppage> findActiveByShiftSensorAndIntervalRange(long shiftId, String sensorKey,
                                                                   int fromInterval, int toInterval) {
        return repository.findActiveByShiftSensorAndIntervalRange(shiftId, sensorKey, fromInterval,
                        toInterval, StoppageState.ACTIVE)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Stoppage save(Stoppage stoppage) {
        StoppageEntity entity = stoppage.id() == null ? new StoppageEntity() : repository.findById(stoppage.id())
                .orElseThrow(() -> new IllegalArgumentException("stoppage " + stoppage.id() + " not found"));
        if (stoppage.id() != null && entity.getVersion() != stoppage.version()) {
            throw new OptimisticLockException("stoppage version is stale");
        }
        apply(stoppage, entity);
        return toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public List<Stoppage> saveAll(List<Stoppage> stoppages) {
        return stoppages.stream().map(this::save).toList();
    }

    private void apply(Stoppage source, StoppageEntity target) {
        target.setShift(shifts.getReferenceById(source.shiftId()));
        target.setIntervalIndex(source.intervalIndex());
        target.setLegacyMinutes(source.roundedMinutes());
        target.setLostCans(source.lostCans());
        target.setLegacyType(source.detectionType().name());
        target.setLegacyMinuteOffset(0);
        target.setLegacyReason("");
        target.setDetectionKey(source.detectionKey());
        target.setIncidentKey(source.incidentKey());
        target.setDetectionType(source.detectionType());
        target.setSensorKey(source.sensorKey());
        target.setStartedAt(source.startedAt());
        target.setExactDurationNanos(source.exactDuration().toNanos());
        target.setRoundedMinutes(source.roundedMinutes());
        target.setState(source.state());
        syncExplanations(source, target);
    }

    private void syncExplanations(Stoppage source, StoppageEntity target) {
        Map<Long, LossExplanationEntity> existing = new HashMap<>();
        target.getExplanations().stream().filter(value -> value.getId() != null)
                .forEach(value -> existing.put(value.getId(), value));
        Set<Long> retained = new HashSet<>();
        for (LossExplanation explanation : source.explanations()) {
            LossExplanationEntity entity;
            if (explanation.id() == null) {
                entity = new LossExplanationEntity();
                target.addExplanation(entity);
            } else {
                entity = Optional.ofNullable(existing.get(explanation.id()))
                        .orElseThrow(() -> new OptimisticLockException("explanation is stale or missing"));
                if (entity.getVersion() != explanation.version()) {
                    throw new OptimisticLockException("explanation version is stale");
                }
                retained.add(explanation.id());
            }
            entity.setCategory(explanation.category());
            entity.setComment(explanation.comment());
            entity.setAllocatedMinutes(explanation.allocatedMinutes());
            entity.setAllocatedCans(explanation.allocatedCans());
        }
        target.getExplanations().removeIf(value -> value.getId() != null && !retained.contains(value.getId()));
    }

    private boolean isSystemLoss(StoppageEntity entity) {
        return entity.getDetectionKey() != null && entity.getIncidentKey() != null
                && entity.getDetectionType() != null
                && entity.getSensorKey() != null && entity.getStartedAt() != null
                && entity.getExactDurationNanos() != null && entity.getRoundedMinutes() != null
                && entity.getState() != null;
    }

    private Stoppage toDomain(StoppageEntity entity) {
        List<LossExplanation> explanations = entity.getExplanations().stream()
                .map(value -> new LossExplanation(value.getId(), entity.getId(), value.getCategory(),
                        value.getComment(), value.getAllocatedMinutes(), value.getAllocatedCans(), value.getVersion()))
                .toList();
        return new Stoppage(entity.getId(), entity.getDetectionKey(), entity.getIncidentKey(),
                entity.getShift().getId(),
                entity.getSensorKey(), entity.getIntervalIndex(), entity.getStartedAt(),
                Duration.ofNanos(entity.getExactDurationNanos()), entity.getRoundedMinutes(), entity.getLostCans(),
                entity.getDetectionType(), entity.getState(), explanations, entity.getVersion());
    }
}
