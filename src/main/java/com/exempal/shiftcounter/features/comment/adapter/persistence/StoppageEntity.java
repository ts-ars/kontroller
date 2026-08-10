package com.exempal.shiftcounter.features.comment.adapter.persistence;

import com.exempal.shiftcounter.features.comment.domain.DetectionType;
import com.exempal.shiftcounter.features.comment.domain.StoppageState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "stoppages")
@Getter
@Setter
@NoArgsConstructor
public class StoppageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shift_id", nullable = false)
    private Long shiftId;

    @Column(name = "hour_index", nullable = false)
    private int intervalIndex;

    @Column(name = "minutes", nullable = false)
    private double legacyMinutes;

    @Column(name = "cans", nullable = false)
    private int lostCans;

    @Column(name = "type", length = 32)
    private String legacyType;

    @Column(name = "minute_offset")
    private int legacyMinuteOffset;

    @Column(name = "reason")
    private String legacyReason;

    @Column(name = "detection_key")
    private UUID detectionKey;

    @Column(name = "incident_key")
    private UUID incidentKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "detection_type", length = 16)
    private DetectionType detectionType;

    @Column(name = "sensor_key", length = 64)
    private String sensorKey;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "exact_duration_nanos")
    private Long exactDurationNanos;

    @Column(name = "rounded_minutes")
    private Integer roundedMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 16)
    private StoppageState state;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @OneToMany(mappedBy = "stoppage", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<LossExplanationEntity> explanations = new ArrayList<>();

    public void addExplanation(LossExplanationEntity explanation) {
        explanation.setStoppage(this);
        explanations.add(explanation);
    }
}
