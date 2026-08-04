package com.exempal.shiftcounter.features.settings.adapter.jpa;

import com.exempal.shiftcounter.features.settings.domain.SettingsPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class JpaSettingsAdapter implements SettingsPort {

    private final SettingRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JpaSettingsAdapter(SettingRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<String> getHourlyPlans() {
        return getList("hourlyPlans");
    }

    @Override
    public List<String> getHours() {
        return getList("hours");
    }

    @Override
    public void updateHourlyPlans(List<String> plans) {
        save("hourlyPlans", toJson(plans));
    }

    @Override
    public void updateHours(List<String> hours) {
        save("hours", toJson(hours));
    }

    @Override
    public void update(String key, String value) {
        save(key, value);
    }

    private List<String> getList(String key) {
        return repository.findById(key)
                .map(setting -> {
                    try {
                        return objectMapper.readValue(setting.getValue(), new TypeReference<List<String>>() {});
                    } catch (IOException e) {
                        throw new IllegalStateException("Invalid JSON for key: " + key, e);
                    }
                })
                .orElseThrow(() -> new IllegalStateException("Missing setting: " + key));
    }

    private void save(String key, String value) {
        repository.save(new SettingEntity(key, value));
    }

    private String toJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize list to JSON", e);
        }
    }
}