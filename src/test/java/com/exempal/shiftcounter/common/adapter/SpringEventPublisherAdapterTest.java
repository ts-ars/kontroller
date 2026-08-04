package com.exempal.shiftcounter.common.adapter;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

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

    @Test
    void shouldDelegateToSpringPublisher() {
        Object event = new Object();

        adapter.publish(event);

        verify(mockPublisher, times(1)).publishEvent(event);
    }
}
