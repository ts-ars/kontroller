package com.exempal.shiftcounter.features.shift.infrastructure;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "shift")
public class ShiftEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private int actual;

    public ShiftEntity() {
    }

    public ShiftEntity(Long id, LocalDate date, int actual) {
        this.id = id;
        this.date = date;
        this.actual = actual;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getActual() {
        return actual;
    }

    public void setActual(int actual) {
        this.actual = actual;
    }

    // ✅ Преобразование из доменной модели
    public static ShiftEntity fromDomain(com.exempal.shiftcounter.features.shift.domain.Shift domain) {
        return new ShiftEntity(
                domain.getId(),
                domain.getDate(),
                domain.getActual()
        );
    }

    // ✅ Преобразование в доменную модель
    public com.exempal.shiftcounter.features.shift.domain.Shift toDomain() {
        return new com.exempal.shiftcounter.features.shift.domain.Shift(
                this.id,
                this.date,
                this.actual
        );
    }
}
