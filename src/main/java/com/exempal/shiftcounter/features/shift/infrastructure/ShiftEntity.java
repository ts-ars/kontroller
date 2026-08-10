package com.exempal.shiftcounter.features.shift.infrastructure;

import com.exempal.shiftcounter.features.shift.domain.Shift;
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
        @UniqueConstraint(name = "uc_shift_date", columnNames = {"date"})
})
public class ShiftEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

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

    public static ShiftEntity fromDomain(Shift shift) {
        ShiftEntity entity = new ShiftEntity();
        entity.setDate(shift.getDate());
        entity.setActual(shift.getActual());
        entity.setHourlyActualValues(new ArrayList<>(shift.getHourlyActualValues()));
        entity.setHourlyPlanValues(new ArrayList<>(shift.getHourlyPlanValues()));
        entity.setHourlyLabels(new ArrayList<>(shift.getHourlyLabels()));
        return entity;
    }

    public Shift toDomain() {
        Shift shift = new Shift(id, date, new ArrayList<>(hourlyPlanValues), actual,
                new ArrayList<>(hourlyActualValues), new ArrayList<>(hourlyLabels));
        shift.setEntity(this);
        return shift;
    }
}
