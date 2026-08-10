package com.exempal.shiftcounter.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class Stage3DomainArchitectureTest {
    @Test
    void commentDomainDoesNotDependOnPersistenceFrameworks() throws IOException {
        Path domain = Path.of("src/main/java/com/exempal/shiftcounter/features/comment/domain");
        try (var files = Files.list(domain)) {
            for (Path file : files.filter(value -> value.toString().endsWith(".java")).toList()) {
                String source = Files.readString(file);
                assertThat(source).as(file.toString())
                        .doesNotContain("jakarta.persistence", "JpaRepository", ".infrastructure.",
                                ".adapter.persistence.");
            }
        }
    }

    @Test
    void stoppageRepositoryIsAnApplicationPort() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/exempal/shiftcounter/features/comment/application/StoppageRepository.java"));
        assertThat(source).doesNotContain("JpaRepository", "org.springframework.data");
    }
}
