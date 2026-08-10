package com.exempal.shiftcounter.features.signal.adapter.persistence;

import com.exempal.shiftcounter.features.signal.domain.CounterContinuity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "counter_states")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CounterStateEntity {
    @Id
    @Column(name = "sensor_id", length = 64)
    private String sensorId;

    @Column(name = "last_counter_value", nullable = false)
    private long lastCounterValue;

    @Column(name = "last_read_at", nullable = false)
    private LocalDateTime lastReadAt;

    @Column(name = "production_date", nullable = false)
    private LocalDate productionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CounterContinuity continuity;
}
