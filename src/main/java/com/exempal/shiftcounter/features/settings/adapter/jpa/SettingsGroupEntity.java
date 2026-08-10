package com.exempal.shiftcounter.features.settings.adapter.jpa;

import jakarta.persistence.*;

@Entity
@Table(name = "settings_groups")
public class SettingsGroupEntity {
    @Id private String id;
    private String name;
    private boolean enabled;
    @Version private long version;

    protected SettingsGroupEntity() {}
    public SettingsGroupEntity(String id, String name, boolean enabled) {
        this.id = id; this.name = name; this.enabled = enabled;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public boolean isEnabled() { return enabled; }
    public void update(String name, boolean enabled) { this.name = name; this.enabled = enabled; }
}
