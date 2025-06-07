package com.exempal.shiftcounter.features.comment;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "stoppage_entries", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"date", "time"})
})
public class StoppageEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String time;
    private double minutes;
    private int cans;
    private String type;
    private String comment;
    private LocalDate date;

    public StoppageEntry() {
    }

    public StoppageEntry(String time, double minutes, int cans, String type, String comment, LocalDate date) {
        this.time = time;
        this.minutes = minutes;
        this.cans = cans;
        this.type = type;
        this.comment = comment;
        this.date = date;
    }

    // геттеры и сеттеры

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getMinutes() {
        return minutes;
    }

    public void setMinutes(double minutes) {
        this.minutes = minutes;
    }

    public int getCans() {
        return cans;
    }

    public void setCans(int cans) {
        this.cans = cans;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
