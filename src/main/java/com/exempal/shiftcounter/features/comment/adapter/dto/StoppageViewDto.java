package com.exempal.shiftcounter.features.comment.adapter.dto;

public class StoppageViewDto {
    private Long id;
    private String time;
    private double minutes;
    private String type;
    private String comment;

    public StoppageViewDto(Long id, String time, double minutes, String type, String comment) {
        this.id = id;
        this.time = time;
        this.minutes = minutes;
        this.type = type;
        this.comment = comment;
    }

    public Long getId() { return id; }
    public String getTime() { return time; }
    public double getMinutes() { return minutes; }
    public String getType() { return type; }
    public String getComment() { return comment; }
}
