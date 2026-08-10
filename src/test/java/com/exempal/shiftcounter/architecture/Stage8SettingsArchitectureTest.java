package com.exempal.shiftcounter.architecture;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;

class Stage8SettingsArchitectureTest {
    private static final Path ROOT = Path.of("src/main/java/com/exempal/shiftcounter");

    @Test
    void groupUpdateIsTransactionalAndLegacyGlobalStorageAndCacheAreGone() throws Exception {
        String service = Files.readString(ROOT.resolve("features/settings/application/SettingsGroupService.java"));
        String provider = Files.readString(ROOT.resolve("features/settings/infrastructure/ShiftSettingsProvider.java"));
        assertThat(service).contains("@Transactional", "findByIdForUpdate", "signalLocks.acquire");
        assertThat(provider).doesNotContain("volatile", "current =");
        assertThat(Files.exists(ROOT.resolve("features/settings/domain/SettingsPort.java"))).isFalse();
        assertThat(Files.exists(ROOT.resolve("features/settings/adapter/jpa/SettingEntity.java"))).isFalse();
    }
}
