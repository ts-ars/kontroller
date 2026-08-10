package com.exempal.shiftcounter.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class Stage4ReconcileArchitectureTest {
    @Test
    void triggersDelegateToUnifiedUseCaseWithoutCalculatingOrPersisting() throws IOException {
        assertThinTrigger("src/main/java/com/exempal/shiftcounter/features/comment/adapter/web/StoppageController.java");
        assertThinTrigger("src/main/java/com/exempal/shiftcounter/features/comment/application/ProductionStoppedListener.java");
        assertThinTrigger("src/main/java/com/exempal/shiftcounter/features/shift/application/ShiftProductRegistrar.java");
    }

    @Test
    void legacyEquivalenceAndIndependentRecalculationPathsAreRemoved() throws IOException {
        String repository = Files.readString(Path.of(
                "src/main/java/com/exempal/shiftcounter/features/comment/application/StoppageRepository.java"));
        assertThat(repository).doesNotContain("findActiveEquivalent");
        String controller = Files.readString(Path.of(
                "src/main/java/com/exempal/shiftcounter/features/comment/adapter/web/StoppageController.java"));
        assertThat(controller).doesNotContain("StoppageCalculator", "SignalService", "repository.save");
        String service = Files.readString(Path.of(
                "src/main/java/com/exempal/shiftcounter/features/comment/application/StoppageReconcilesService.java"));
        assertThat(service).doesNotContain(".infrastructure.", "JpaRepository");
    }

    private void assertThinTrigger(String path) throws IOException {
        String source = Files.readString(Path.of(path));
        assertThat(source).contains("ReconcileStoppagesUseCase");
        assertThat(source).doesNotContain("StoppageCalculator", "findActiveEquivalent");
    }
}
