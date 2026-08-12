package com.exempal.shiftcounter.architecture;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SettingsV9MigrationContractTest {
    @Test
    void v9ForcesApprovedRowsAndOwnershipWithoutEditingEarlierMigrations() throws Exception {
        Path migrations = Path.of("src/main/resources/db/migration");
        String v9 = Files.readString(migrations.resolve("V9__replace_settings_ownership.sql"));

        assertThat(v9).contains("id IN ('sensor-1', 'sensor-2', 'sensor-3', 'sensor-4', 'sensor-5')",
                "WHEN id = 'sensor-6' THEN 'settings-group-2'", "DELETE FROM interval_settings",
                "TIME '07:00', 450, 1600", "TIME '22:30', 300,  960");
        assertThat(v9.split("TIME '", -1)).hasSize(17);
        assertThat(Files.list(migrations).map(path -> path.getFileName().toString()).sorted().toList())
                .containsExactly("V1__create_settings_table.sql", "V2__align_stoppage_minutes_with_domain.sql",
                        "V3__create_loss_explanations.sql", "V4__introduce_stoppage_model.sql",
                        "V5__support_unified_reconcile.sql", "V6__introduce_sensors_and_signal_identity.sql",
                        "V7__add_counter_state.sql", "V8__introduce_settings_groups.sql",
                        "V9__replace_settings_ownership.sql");
    }
}
