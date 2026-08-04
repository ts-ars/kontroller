package com.exempal.shiftcounter.features.settings.adapter.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "settings")
public class SettingEntity {

    @Id
    @Column(name = "setting_key") // 🟢 добавили это!
    private String key;

    @Column(name = "setting_value") // 🟢 переименовали
    private String value;


    public SettingEntity() {}

    public SettingEntity(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() { return key; }
    public String getValue() { return value; }
}
