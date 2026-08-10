package com.exempal.shiftcounter.features.comment.adapter.persistence;

import com.exempal.shiftcounter.features.comment.application.ReconcileShiftRepository;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.infrastructure.ShiftEntity;
import com.exempal.shiftcounter.features.shift.infrastructure.ShiftJpaRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaReconcileShiftAdapter implements ReconcileShiftRepository {
    private final ShiftJpaRepository shifts;

    @Override
    @Transactional
    public Optional<Shift> findForUpdateByDate(LocalDate date) {
        return shifts.findForUpdateByDate(date).map(this::toDomain);
    }

    private Shift toDomain(ShiftEntity entity) {
        Hibernate.initialize(entity.getHourlyLabels());
        Hibernate.initialize(entity.getHourlyPlanValues());
        Hibernate.initialize(entity.getHourlyActualValues());
        return entity.toDomain();
    }
}
