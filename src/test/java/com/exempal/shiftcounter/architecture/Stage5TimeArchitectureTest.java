package com.exempal.shiftcounter.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class Stage5TimeArchitectureTest {
    @Test
    void currentTimeAndSystemZoneHaveOneApprovedBoundary() throws IOException {
        try (var files = Files.walk(Path.of("src/main/java"))) {
            for (Path path : files.filter(value -> value.toString().endsWith(".java")).toList()) {
                String source = Files.readString(path);
                if (path.endsWith("ProductionDayService.java") || path.endsWith("TimeConfiguration.java")) continue;
                assertThat(source).as(path.toString()).doesNotContain(
                        "LocalDate.now(", "LocalDateTime.now(", "Instant.now(", "ZoneId.systemDefault(");
            }
        }
    }

    @Test
    void obsoleteTimeHelperIsRemoved() {
        assertThat(Path.of("src/main/java/com/exempal/shiftcounter/features/shift/application/ShiftTimeHelper.java"))
                .doesNotExist();
    }
}
