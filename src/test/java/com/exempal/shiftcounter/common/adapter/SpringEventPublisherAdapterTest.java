package com.exempal.shiftcounter.common.adapter;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.mockito.Mockito.*;

@SpringBootTest
class SpringEventPublisherAdapterTest {

    @TestConfiguration
    static class MockPublisherConfig {
        @Bean
        @Primary // ✅ важный момент
        public ApplicationEventPublisher applicationEventPublisher() {
            return mock(ApplicationEventPublisher.class);
        }
    }

    @Autowired
    private SpringEventPublisherAdapter adapter;

    @Autowired
    private ApplicationEventPublisher mockPublisher;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void shouldDelegateToSpringPublisher() {
        Object event = new Object();

        adapter.publish(event);

        verify(mockPublisher, times(1)).publishEvent(event);
    }

    @Test
    void defersPublicationUntilCommit() {
        Object event = new Object();

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            adapter.publish(event);
            verify(mockPublisher, never()).publishEvent(event);
        });

        verify(mockPublisher).publishEvent(event);
    }

    @Test
    void discardsPublicationOnRollback() {
        Object event = new Object();

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            adapter.publish(event);
            status.setRollbackOnly();
        });

        verify(mockPublisher, never()).publishEvent(event);
    }
}
