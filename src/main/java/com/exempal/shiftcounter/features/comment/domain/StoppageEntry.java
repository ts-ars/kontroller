package com.exempal.shiftcounter.features.comment.domain;

import com.exempal.shiftcounter.features.shift.infrastructure.ShiftEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.Objects;

@Entity
@Table(name = "stoppages")
public class StoppageEntry {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    private int hourIndex;

    @Getter
    @Setter
    private double minutes;

    @Getter
    @Setter
    private int cans;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private StoppageType type;

    @Getter
    @Setter
    private int minuteOffset;

    @Getter
    @Setter
    @Column(name = "reason")
    private String comment;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    @JsonBackReference
    private ShiftEntity shift;

    // --- Фабричные методы ---

    public static StoppageEntry fixed(int hourIndex, Duration duration, ShiftEntity shift) {
        StoppageEntry entry = new StoppageEntry();
        entry.setType(StoppageType.FIXED);
        entry.setHourIndex(hourIndex);
        entry.setMinutes(duration.toMinutes());
        entry.setComment("");
        entry.setShift(shift);  // обязательно, чтобы получить label через shift
        return entry;
    }

    public static StoppageEntry tempo(int hourIndex, Duration duration, ShiftEntity shift) {
        StoppageEntry entry = new StoppageEntry();
        entry.setType(StoppageType.TEMPO);
        entry.setHourIndex(hourIndex);
        entry.setMinutes(duration.toMinutes());
        entry.setComment("");
        entry.setShift(shift);  // обязательно
        return entry;
    }

    @Transient
    public String getLabel() {
        if (shift == null) {
            throw new IllegalStateException("StoppageEntry not linked to shift");
        }
        if (shift.getHourlyLabels() == null) {
            throw new IllegalStateException("Shift does not contain hourly labels");
        }
        if (hourIndex < 0 || hourIndex >= shift.getHourlyLabels().size()) {
            throw new IllegalStateException("Invalid hour index: " + hourIndex);
        }
        return shift.getHourlyLabels().get(hourIndex);
    }
    // --- equals, hashCode, toString ---

    @Transient
    public LocalTime getTime() {
        if (shift == null || shift.getHourlyLabels() == null) return null;
        if (hourIndex < 0 || hourIndex >= shift.getHourlyLabels().size()) return null;

        String label = shift.getHourlyLabels().get(hourIndex); // например "08:00"
        return LocalTime.parse(label).plusMinutes(minuteOffset); // label + offset
    }

    @Transient
    public boolean isUserEditable() {
        return type == null || type.isUserEditable();
    }
}