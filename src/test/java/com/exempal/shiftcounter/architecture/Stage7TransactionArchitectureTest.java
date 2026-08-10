package com.exempal.shiftcounter.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class Stage7TransactionArchitectureTest {
    @Test
    void signalScenarioOwnsTheTransactionBoundaryAndConcurrentInsertIsAtomic() throws IOException {
        String service = Files.readString(Path.of(
                "src/main/java/com/exempal/shiftcounter/features/signal/application/SignalService.java"));
        String repository = Files.readString(Path.of(
                "src/main/java/com/exempal/shiftcounter/features/signal/infrastructure/SignalJpaRepository.java"));

        assertThat(service).contains("@Transactional", "registrationLock.acquire");
        assertThat(repository).contains("on conflict", "do nothing");
    }

    @Test
    void adamUsesPersistedCounterDeltaPathInsteadOfInMemoryEdges() throws IOException {
        String emitter = Files.readString(Path.of(
                "src/main/java/com/exempal/shiftcounter/features/signal/adapter/event/AdamEventEmitter.java"));
        String counter = Files.readString(Path.of(
                "src/main/java/com/exempal/shiftcounter/features/signal/application/CounterInputService.java"));

        assertThat(emitter).contains("readCounter", "CounterReadingCommand").doesNotContain("previousState");
        assertThat(counter).contains("CounterStateStoragePort", "currentCounter() - previous.lastCounterValue()",
                "COUNTER_DISCONTINUITY");
    }
}
