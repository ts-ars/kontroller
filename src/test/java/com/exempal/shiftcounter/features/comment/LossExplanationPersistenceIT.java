package com.exempal.shiftcounter.features.comment;

import com.exempal.shiftcounter.features.comment.application.StoppageRepository;
import com.exempal.shiftcounter.features.comment.domain.*;
import com.exempal.shiftcounter.features.shift.infrastructure.ShiftEntity;
import com.exempal.shiftcounter.features.shift.infrastructure.ShiftJpaRepository;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Tag("e2e")
class LossExplanationPersistenceIT {
    @Autowired ShiftJpaRepository shifts;
    @Autowired StoppageRepository stoppages;
    @Autowired JdbcTemplate jdbc;

    @Test
    @Transactional
    void persistsAggregateWithSeveralExplanations() {
        ShiftEntity shift = savedShift();
        Stoppage loss = stoppages.save(Stoppage.detected(UUID.randomUUID(), shift.getId(),
                Stoppage.PRIMARY_SENSOR, 0, LocalDateTime.of(2026, 8, 7, 8, 0),
                Duration.ofMinutes(10), 100, DetectionType.FIXED));
        loss = loss.addExplanation(LossCategory.MATERIAL, "roll", 4)
                .addExplanation(LossCategory.QUALITY, "quality", 6);
        Stoppage saved = stoppages.save(loss);

        assertThat(saved.explanations()).extracting(LossExplanation::allocatedMinutes)
                .containsExactly(4, 6);
        assertThat(saved.incidentKey()).isEqualTo(saved.detectionKey());
        assertThat(stoppages.findById(saved.id()).orElseThrow().explanationStatus())
                .isEqualTo(ExplanationStatus.FULLY_EXPLAINED);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void rejectsStaleAggregateVersion() {
        ShiftEntity shift = savedShift();
        Stoppage saved = stoppages.save(Stoppage.detected(UUID.randomUUID(), shift.getId(),
                Stoppage.PRIMARY_SENSOR, 0, LocalDateTime.of(2026, 8, 7, 8, 0),
                Duration.ofMinutes(10), 100, DetectionType.FIXED));
        Stoppage first = stoppages.findById(saved.id()).orElseThrow();
        Stoppage stale = stoppages.findById(saved.id()).orElseThrow();
        stoppages.save(first.resolve());
        assertThatThrownBy(() -> stoppages.save(stale.withLostCans(90)))
                .isInstanceOf(OptimisticLockException.class);
    }

    @Test
    void migrationCreatesVersionedModelAndReport() {
        assertThat(jdbc.queryForObject("select count(*) from information_schema.columns " +
                "where table_name='stoppages' and column_name in " +
                "('detection_key','detection_type','sensor_key','started_at','exact_duration_nanos'," +
                "'rounded_minutes','state','version')", Integer.class)).isEqualTo(8);
        assertThat(jdbc.queryForObject("select count(*) from information_schema.columns " +
                "where table_name='loss_explanations' and column_name='version'", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from information_schema.columns " +
                "where table_name='stoppages' and column_name='incident_key'", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from information_schema.tables " +
                "where table_name='stoppage_model_migration_report'", Integer.class)).isEqualTo(1);
    }

    private ShiftEntity savedShift() {
        ShiftEntity shift = new ShiftEntity();
        shift.setDate(LocalDate.of(2026, 8, 7));
        shift.setSensorId("sensor-1");
        shift.setActual(0);
        shift.setHourlyLabels(List.of("08:00"));
        shift.setHourlyPlanValues(List.of(100));
        shift.setHourlyActualValues(List.of(0));
        return shifts.saveAndFlush(shift);
    }
}
