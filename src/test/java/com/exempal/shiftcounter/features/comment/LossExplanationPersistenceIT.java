package com.exempal.shiftcounter.features.comment;

import com.exempal.shiftcounter.features.comment.adapter.persistence.LossExplanationEntity;
import com.exempal.shiftcounter.features.comment.adapter.persistence.LossExplanationJpaRepository;
import com.exempal.shiftcounter.features.comment.domain.*;
import com.exempal.shiftcounter.features.shift.infrastructure.ShiftEntity;
import com.exempal.shiftcounter.features.shift.infrastructure.ShiftJpaRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Tag("e2e")
@Transactional
class LossExplanationPersistenceIT {
    @Autowired ShiftJpaRepository shifts;
    @Autowired StoppageRepository stoppages;
    @Autowired LossExplanationJpaRepository explanations;

    @Test
    void persistsSeveralExplanationsForOneSystemLoss() {
        ShiftEntity shift = new ShiftEntity();
        shift.setDate(LocalDate.of(2026, 8, 7));
        shift.setActual(0);
        shift.setHourlyLabels(List.of("08:00"));
        shift.setHourlyPlanValues(List.of(100));
        shift.setHourlyActualValues(List.of(0));
        shift = shifts.saveAndFlush(shift);

        StoppageEntry loss = new StoppageEntry();
        loss.setShift(shift);
        loss.setHourIndex(0);
        loss.setMinutes(10);
        loss.setCans(100);
        loss.setType(StoppageType.FIXED);
        loss = stoppages.saveAndFlush(loss);

        explanations.save(entity(loss.getId(), LossCategory.MATERIAL, 4, 40));
        explanations.save(entity(loss.getId(), LossCategory.QUALITY, 6, 60));
        explanations.flush();

        assertThat(explanations.findByStoppageIdOrderById(loss.getId()))
                .extracting(LossExplanationEntity::getAllocatedMinutes)
                .containsExactly(4, 6);
    }

    private LossExplanationEntity entity(long stoppageId, LossCategory category, int minutes, int cans) {
        LossExplanationEntity entity = new LossExplanationEntity();
        entity.setStoppageId(stoppageId);
        entity.setCategory(category);
        entity.setComment("");
        entity.setAllocatedMinutes(minutes);
        entity.setAllocatedCans(cans);
        return entity;
    }
}
