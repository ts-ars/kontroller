package com.exempal.shiftcounter.features.settings.integration;

import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftFactory;
import com.exempal.shiftcounter.features.shift.domain.ShiftUpdatedEvent;
import com.exempal.shiftcounter.features.shift.infrastructure.JpaShiftAdapter;
import com.exempal.shiftcounter.features.settings.api.SettingsRequest;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
public class ShiftSettingsIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ShiftFactory shiftFactory;

    @Autowired
    private JpaShiftAdapter shiftAdapter;

    private WebSocketStompClient stompClient;
    private CountDownLatch latch;
    private ShiftUpdatedEvent[] receivedEvent;

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
    void postSettings_shouldUpdateShift_andSendShiftUpdatedEvent() throws Exception {
        // 1. Подготовка смены на дату
        LocalDate testDate = LocalDate.of(2025, 7, 11);
        Shift shift = shiftFactory.createNewShift(testDate);
        shiftAdapter.save(shift);

        // 2. Подключение к WebSocket и подписка
        CountDownLatch subscribeLatch = new CountDownLatch(1); // 💡 для подтверждения подписки

        StompSession session = stompClient
                .connect("ws://localhost:" + port + "/ws", new StompSessionHandlerAdapter() {
                    @Override
                    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                        session.subscribe("/topic/shift-updates", new StompFrameHandler() {
                            @Override
                            public Type getPayloadType(StompHeaders headers) {
                                return ShiftUpdatedEvent.class;
                            }

                            @Override
                            public void handleFrame(StompHeaders headers, Object payload) {
                                System.out.println("📬 Получено событие: " + payload);
                                receivedEvent[0] = (ShiftUpdatedEvent) payload;
                                latch.countDown();
                            }
                        });
                        subscribeLatch.countDown(); // 🔔 подписка установлена
                    }
                })
                .get(3, TimeUnit.SECONDS);

        assertThat(subscribeLatch.await(3, TimeUnit.SECONDS)).as("Подписка не установлена").isTrue();

// 🔥 теперь можно безопасно отправлять POST
        SettingsRequest request = new SettingsRequest(
                List.of("08:00", "09:00"),
                List.of("123", "456")
        );
        new RestTemplate().postForEntity("http://localhost:" + port + "/api/settings", request, Void.class);

        // 4. Проверка события
        boolean received = latch.await(10, TimeUnit.SECONDS);
        assertThat(received).as("Событие не получено через WebSocket").isTrue();

        ShiftUpdatedEvent event = receivedEvent[0];
        assertThat(event).isNotNull();
        assertThat(event.date()).isEqualTo(testDate);
        assertThat(event.hours()).containsExactly("08:00", "09:00");
        assertThat(event.plan()).containsExactly(123, 456);
    }
}