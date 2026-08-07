package com.exempal.shiftcounter.features.settings.integration;

import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftFactory;
import com.exempal.shiftcounter.features.shift.domain.ShiftUpdatedEvent;
import com.exempal.shiftcounter.features.shift.infrastructure.JpaShiftAdapter;
import com.exempal.shiftcounter.features.settings.api.SettingsRequest;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("e2e")
@Disabled("Stage 8: define settings delivery semantics before enabling")
public class ShiftSettingsDeliveryGuaranteeTest {

    @LocalServerPort
    int port;

    @Autowired
    ShiftFactory shiftFactory;

    @Autowired
    JpaShiftAdapter shiftAdapter;

    WebSocketStompClient stompClient;
    CountDownLatch latch;
    ShiftUpdatedEvent[] receivedEvent;

    @BeforeEach
    void setup() {
        latch = new CountDownLatch(1);
        receivedEvent = new ShiftUpdatedEvent[1];

        stompClient = new WebSocketStompClient(new StandardWebSocketClient());

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.getObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());

        stompClient.setMessageConverter(converter);
    }

    @Test
    void postSettings_shouldDeliverExactlyWhatIsSaved_includingComments() throws Exception {
        // arrange
        LocalDate today = LocalDate.now();
        shiftAdapter.save(shiftFactory.createNewShift(today));

        CountDownLatch subscribeLatch = new CountDownLatch(1);
        stompClient.connect("ws://localhost:" + port + "/ws", new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                session.subscribe("/topic/shift-updates", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return ShiftUpdatedEvent.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        receivedEvent[0] = (ShiftUpdatedEvent) payload;
                        latch.countDown();
                    }
                });
                subscribeLatch.countDown();
            }
        }).get(3, TimeUnit.SECONDS);

        assertThat(subscribeLatch.await(3, TimeUnit.SECONDS)).isTrue();

        // act
        SettingsRequest request = new SettingsRequest(
                List.of("08:00", "09:00"),
                List.of("123", "456")
        );
        new RestTemplate().postForEntity("http://localhost:" + port + "/api/settings", request, Void.class);

        // wait for event
        boolean received = latch.await(5, TimeUnit.SECONDS);
        assertThat(received).as("Событие не получено").isTrue();

        // assert: данные из БД и события должны быть полностью идентичны
        ShiftUpdatedEvent event = receivedEvent[0];
        assertThat(event).isNotNull();

        LocalDate date;
        Shift shift = shiftAdapter.findByDate(today).orElseThrow();

        assertThat(event.date()).isEqualTo(shift.getDate());
        assertThat(event.hours()).isEqualTo(shift.getHourlyLabels());
        assertThat(event.plan()).isEqualTo(shift.getHourlyPlanValues());
        assertThat(event.actual()).isEqualTo(shift.getHourlyActualValues());
    }

    @AfterEach
    void cleanup() {
        shiftAdapter.deleteByDate(LocalDate.now());
    }
}
