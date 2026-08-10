package com.exempal.shiftcounter.architecture;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class Stage10ProductionReleaseTest {
    @Test
    void productionConfigurationProtectsReleaseBoundaries() throws Exception {
        String production = Files.readString(Path.of("src/main/resources/application-prod.yml"));
        String main = Files.readString(Path.of("src/main/resources/application.yml"));
        assertThat(production)
                .contains("${DB_URL}", "ddl-auto: validate", "include: health,info")
                .contains("address: 127.0.0.1", "include: adam")
                .doesNotContain("include-stacktrace: always");
        assertThat(main).contains("shutdown: graceful", "timeout-per-shutdown-phase: 30s");
    }

    @Test
    void releaseWorkflowProducesCommitIdentifiedArtifact() throws Exception {
        String workflow = Files.readString(Path.of(".github/workflows/ci.yml"));
        String dockerfile = Files.readString(Path.of("Dockerfile"));
        assertThat(workflow).contains("clean verify", "github.sha", "upload-artifact");
        assertThat(dockerfile).contains("USER 10001", "--spring.profiles.active=prod");
    }

    @Test
    void operationalProceduresCoverMandatoryRecoveryGates() throws Exception {
        String release = Files.readString(Path.of("docs/operations/PRODUCTION_RUNBOOK.md"));
        String backup = Files.readString(Path.of("docs/operations/BACKUP_RESTORE.md"));
        assertThat(release).contains("verified backup", "Flyway", "rollback", "six sensors", "HTTPS");
        assertThat(backup).contains("pg_dump", "pg_restore", "separate", "checksum");
    }
}
