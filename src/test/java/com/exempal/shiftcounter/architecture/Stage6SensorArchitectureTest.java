package com.exempal.shiftcounter.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class Stage6SensorArchitectureTest {
    @Test
    void historicalPrimaryLiteralIsAbsentFromProductionJava() throws IOException {
        try (var files = Files.walk(Path.of("src/main/java"))) {
            for (Path path : files.filter(value -> value.toString().endsWith(".java")).toList()) {
                assertThat(Files.readString(path)).as(path.toString()).doesNotContain("\"primary\"");
            }
        }
    }

    @Test
    void registrationRemainsNonTransactionalUntilStageSeven() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/exempal/shiftcounter/features/signal/application/SignalService.java"));
        assertThat(source).doesNotContain("@Transactional");
    }

    @Test
    void everyCurrentInputAdapterUsesTheRegistrationCommand() throws IOException {
        for (String file : new String[] {
                "adapter/event/AdamEventEmitter.java",
                "adapter/http/HttpSignalAdapter.java",
                "adapter/web/SignalController.java"}) {
            String source = Files.readString(Path.of(
                    "src/main/java/com/exempal/shiftcounter/features/signal/" + file));
            assertThat(source).contains("RegisterSignalCommand", ".register(");
        }
    }
}
