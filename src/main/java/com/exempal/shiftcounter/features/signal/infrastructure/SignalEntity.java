package com.exempal.shiftcounter.features.signal.infrastructure;

import com.exempal.shiftcounter.features.signal.domain.SignalSource;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "signals", uniqueConstraints = @UniqueConstraint(name = "ux_signal_source_identity",
        columnNames = {"sensor_id", "source", "source_identity"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignalEntity {
    @Id
    private UUID id;

    @Column(name = "sensor_id", nullable = false, length = 64)
    private String sensorId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "production_date", nullable = false)
    private LocalDate productionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private SignalSource source;

    @Column(name = "source_identity", nullable = false, length = 255)
    private String sourceIdentity;

    public SignalEntity(UUID id, LocalDateTime occurredAt) {
        this(id, "sensor-1", occurredAt,
                occurredAt.toLocalTime().isBefore(java.time.LocalTime.of(7, 0))
                        ? occurredAt.toLocalDate().minusDays(1) : occurredAt.toLocalDate(),
                SignalSource.LEGACY, id.toString());
    }
}
