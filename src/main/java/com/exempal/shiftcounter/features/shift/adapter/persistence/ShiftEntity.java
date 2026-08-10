package com.exempal.shiftcounter.features.shift.adapter.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "shift", uniqueConstraints = {
        @UniqueConstraint(name = "ux_shift_date_sensor", columnNames = {"date", "sensor_id"})
})
public class ShiftEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "sensor_id", nullable = false, length = 64)
    private String sensorId;

    private Integer actual;

    @ElementCollection
    @CollectionTable(name = "shift_hourly_actual", joinColumns = @JoinColumn(name = "shift_id"))
    @OrderColumn(name = "order_index")
    @Column(name = "value")
    private List<Integer> hourlyActualValues = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "shift_hourly_plan", joinColumns = @JoinColumn(name = "shift_id"))
    @OrderColumn(name = "order_index")
    @Column(name = "value")
    private List<Integer> hourlyPlanValues = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "shift_hour_labels", joinColumns = @JoinColumn(name = "shift_id"))
    @OrderColumn(name = "order_index")
    @Column(name = "label")
    private List<String> hourlyLabels = new ArrayList<>();

}
