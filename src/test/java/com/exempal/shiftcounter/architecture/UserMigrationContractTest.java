package com.exempal.shiftcounter.architecture;
import org.junit.jupiter.api.Test; import java.nio.file.*; import static org.assertj.core.api.Assertions.assertThat;
class UserMigrationContractTest {@Test void migrationDefinesUserSecurityConstraints() throws Exception {String sql=Files.readString(Path.of("src/main/resources/db/migration/V10__introduce_local_users.sql"));assertThat(sql).contains("CREATE TABLE app_user","pin_hash VARCHAR(255) NOT NULL","USER', 'ADMIN', 'OWNER","ACTIVE', 'BLOCKED","failed_attempts");assertThat(sql.toLowerCase()).doesNotContain("pin varchar","pin char");}}
