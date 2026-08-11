package com.exempal.shiftcounter.features.settings.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SettingsSnapshotTest {
    private static final List<SettingsRow> STANDARD = List.of(
            row("07:00", 450, 1600), row("08:00", 600, 1920),
            row("09:00", 500, 1600), row("10:00", 600, 1920),
            row("11:30", 600, 1920), row("12:30", 600, 1920),
            row("13:30", 500, 1600), row("14:30", 600, 960),
            row("15:00", 300, 1920), row("16:00", 600, 1920),
            row("17:00", 600, 1600), row("18:00", 500, 1920),
            row("19:00", 600, 1920), row("20:30", 600, 1920),
            row("21:30", 500, 1600), row("22:30", 300, 960));

    @Test
    void derivesSensor5AndApprovedTotalsFromSingleSnapshot() {
        SettingsSnapshot snapshot = new SettingsSnapshot(STANDARD);

        assertThat(snapshot.sharedTotal()).isEqualTo(8450);
        assertThat(snapshot.sensor5Total()).isEqualTo(33800);
        assertThat(snapshot.sensor6Total()).isEqualTo(27200);
        assertThat(snapshot.rows()).extracting(SettingsRow::sensor5Plan)
                .containsExactlyElementsOf(STANDARD.stream().map(row -> row.sharedPlan() * 4).toList());
    }

    @Test
    void addAndDeleteRotateHalfTailReversibly() {
        SettingsSnapshot original = new SettingsSnapshot(STANDARD);

        SettingsSnapshot extended = original.addHour();
        assertThat(extended.rows().get(15)).isEqualTo(row("22:30", 600, 1920));
        assertThat(extended.rows().get(16)).isEqualTo(row("23:30", 300, 960));

        SettingsSnapshot extendedAgain = extended.addHour();
        assertThat(extendedAgain.rows().get(16)).isEqualTo(row("23:30", 600, 1920));
        assertThat(extendedAgain.rows().get(17)).isEqualTo(row("00:30", 300, 960));
        assertThat(extendedAgain.deleteLastExtension()).isEqualTo(extended);
        assertThat(extended.deleteLastExtension()).isEqualTo(original);
    }

    @Test
    void cannotDeleteStandardTailOrExtendAcrossProductionDayBoundary() {
        SettingsSnapshot original = new SettingsSnapshot(STANDARD);
        assertThatThrownBy(original::deleteLastExtension)
                .isInstanceOf(IllegalStateException.class);

        SettingsSnapshot current = original;
        while (!current.rows().getLast().hour().equals(LocalTime.of(6, 30))) {
            current = current.addHour();
        }
        SettingsSnapshot atBoundary = current;
        assertThatThrownBy(atBoundary::addHour)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("07:00");
    }

    private static SettingsRow row(String hour, int shared, int sensor6) {
        return new SettingsRow(LocalTime.parse(hour), shared, sensor6);
    }
}
