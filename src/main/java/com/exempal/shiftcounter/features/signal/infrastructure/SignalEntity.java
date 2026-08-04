package com.exempal.shiftcounter.features.signal.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "signals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignalEntity {

    @Id
    private UUID id;

    private LocalDateTime timestamp;

    public static SignalEntity from(LocalDateTime timestamp) {
        return new SignalEntity(UUID.randomUUID(), timestamp);
    }
}