package com.exempal.shiftcounter.features.comment;

import com.exempal.shiftcounter.shared.event.ProductionStoppedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class TestUtils {

    public static void stopAt(ApplicationEventPublisher events, String time, int minutes) {
        LocalDate today = LocalDate.now();
        LocalTime t = LocalTime.parse(time);
        events.publishEvent(new ProductionStoppedEvent(LocalDateTime.of(today, t), minutes));
    }

    public static TestModel newModel() {
        return new TestModel();
    }

    public static class TestModel implements Model {
        private final Map<String, Object> map = new HashMap<>();

        @Override
        public Model addAttribute(String key, Object val) {
            map.put(key, val);
            return this;
        }

        @Override
        public Object getAttribute(String name) {
            return map.get(name);
        }

        @Override
        public Map<String, Object> asMap() {
            return map;
        }

        public Model addAttribute(Object value) {
            throw new UnsupportedOperationException();
        }

        public Model addAllAttributes(Collection<?> attributeValues) {
            throw new UnsupportedOperationException();
        }

        public Model addAllAttributes(Map<String, ?> attributes) {
            map.putAll(attributes);
            return this;
        }

        public Model mergeAttributes(Map<String, ?> attributes) {
            map.putAll(attributes);
            return this;
        }

        public boolean containsAttribute(String attributeName) {
            return map.containsKey(attributeName);
        }
    }
}
