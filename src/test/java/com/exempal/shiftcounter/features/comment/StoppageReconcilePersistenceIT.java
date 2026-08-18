package com.exempal.shiftcounter.features.comment;

import com.exempal.shiftcounter.features.comment.application.*;
import com.exempal.shiftcounter.features.comment.domain.*;
import com.exempal.shiftcounter.features.shift.adapter.persistence.ShiftEntity;
import com.exempal.shiftcounter.features.shift.adapter.persistence.ShiftJpaRepository;
import com.exempal.shiftcounter.features.signal.domain.Signal;
import com.exempal.shiftcounter.features.signal.application.SignalStoragePort;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.time.Instant;
import java.util.UUID;
import com.exempal.shiftcounter.features.user.adapter.persistence.AppUserEntity;
import com.exempal.shiftcounter.features.user.adapter.persistence.AppUserRepository;
import com.exempal.shiftcounter.features.user.domain.UserRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Tag("e2e")
class StoppageReconcilePersistenceIT {
    private static final LocalDate DATE = LocalDate.of(2026, 8, 9);
    private static final LocalDateTime END = LocalDateTime.of(2026, 8, 9, 9, 0);

    @Autowired ReconcileStoppagesUseCase reconcile;
    @Autowired StoppageRepository stoppages;
    @Autowired ShiftJpaRepository shifts;
    @Autowired LossExplanationUseCase explanations;
    @Autowired SignalStoragePort signals;
    @Autowired AppUserRepository users;
    private String actorName;

    @BeforeEach
    void authenticateCommentAuthor() {
        actorName = "Reconcile author " + UUID.randomUUID();
        users.saveAndFlush(new AppUserEntity(UUID.randomUUID(), actorName, "{noop}unused", UserRole.USER,
                Instant.parse("2026-08-09T07:00:00Z")));
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                actorName, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void repeatedIdenticalRunKeepsOneIdentityAndPerformsNoSecondWrite() {
        ShiftEntity shift = savedShift();
        ReconcileResult first = reconcile.reconcile(command());
        ReconcileResult second = reconcile.reconcile(command());

        List<Stoppage> active = stoppages.findActiveByShiftAndInterval(shift.getId(), 0);
        assertThat(active).hasSize(1);
        assertThat(active.getFirst().detectionKey()).isEqualTo(first.activeStoppages().getFirst().detectionKey());
        assertThat(second.changedRows()).isZero();
    }

    @Test
    void concurrentRunsAreSerializedAndCreateNoDuplicate() throws Exception {
        ShiftEntity shift = savedShift();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<ReconcileResult> left = executor.submit(() -> { start.await(); return reconcile.reconcile(command()); });
            Future<ReconcileResult> right = executor.submit(() -> { start.await(); return reconcile.reconcile(command()); });
            start.countDown();
            assertThat(List.of(left.get(30, TimeUnit.SECONDS), right.get(30, TimeUnit.SECONDS)))
                    .allMatch(ReconcileResult::persisted);
        } finally {
            executor.shutdownNow();
        }
        assertThat(stoppages.findActiveByShiftAndInterval(shift.getId(), 0)).hasSize(1);
    }

    @Test
    @Transactional
    void updateInPlacePreservesOperatorFieldsAndExposesShrinkConflict() {
        ShiftEntity shift = savedShift();
        Stoppage first = reconcile.reconcile(command()).activeStoppages().getFirst();
        explanations.create(first.id(), LossCategory.BREAKDOWN, "belt", 60);
        signals.save(new Signal(LocalDateTime.of(2026, 8, 9, 8, 59, 1)));

        ReconcileResult result = reconcile.reconcile(command());
        Stoppage updated = stoppages.findActiveByShiftAndInterval(shift.getId(), 0).stream()
                .filter(value -> value.detectionKey().equals(first.detectionKey())).findFirst().orElseThrow();

        assertThat(updated.detectionKey()).isEqualTo(first.detectionKey());
        assertThat(updated.explanations()).extracting(LossExplanation::category,
                        LossExplanation::comment, LossExplanation::allocatedMinutes)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(LossCategory.BREAKDOWN, "belt", 60));
        assertThat(updated.explanations()).singleElement()
                .satisfies(value -> assertThat(value.authorDisplayName()).isEqualTo(actorName));
        assertThat(updated.explanationStatus()).isEqualTo(ExplanationStatus.ALLOCATION_CONFLICT);
        assertThat(result.diagnostics()).extracting(ReconcileDiagnostic::code)
                .contains(ReconcileDiagnosticCode.ALLOCATION_CONFLICT);
    }

    private ReconcileStoppagesCommand command() {
        return new ReconcileStoppagesCommand(DATE, Stoppage.PRIMARY_SENSOR, 0, END);
    }

    private ShiftEntity savedShift() {
        ShiftEntity shift = new ShiftEntity();
        shift.setDate(DATE);
        shift.setSensorId("sensor-1");
        shift.setActual(20);
        shift.setHourlyLabels(List.of("08:00"));
        shift.setHourlyPlanValues(List.of(100));
        shift.setHourlyActualValues(List.of(20));
        return shifts.saveAndFlush(shift);
    }
}
