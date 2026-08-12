package com.exempal.shiftcounter.features.comment;

import com.exempal.shiftcounter.common.adapter.SpringEventPublisherAdapter;
import com.exempal.shiftcounter.features.comment.application.event.CommentsUpdatedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

class CommentsAfterCommitPublicationTest {
    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void sensorScopedCommentEventIsInvisibleUntilCommit() {
        ApplicationEventPublisher spring = mock(ApplicationEventPublisher.class);
        SpringEventPublisherAdapter adapter = new SpringEventPublisherAdapter(spring);
        CommentsUpdatedEvent event = new CommentsUpdatedEvent(LocalDate.of(2026, 8, 7), "sensor-4");
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();

        adapter.publish(event);
        verifyNoInteractions(spring);

        TransactionSynchronizationManager.getSynchronizations().forEach(synchronization -> synchronization.afterCommit());
        verify(spring).publishEvent(event);
    }

    @Test
    void rollbackDoesNotPublishSensorScopedCommentEvent() {
        ApplicationEventPublisher spring = mock(ApplicationEventPublisher.class);
        SpringEventPublisherAdapter adapter = new SpringEventPublisherAdapter(spring);
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();

        adapter.publish(new CommentsUpdatedEvent(LocalDate.of(2026, 8, 7), "sensor-2"));
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(synchronization -> synchronization.afterCompletion(1));

        verifyNoInteractions(spring);
    }
}
