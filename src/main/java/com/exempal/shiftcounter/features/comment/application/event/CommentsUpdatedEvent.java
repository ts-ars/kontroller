package com.exempal.shiftcounter.features.comment.application.event;

import java.time.LocalDate;
public record CommentsUpdatedEvent(LocalDate date, String sensorId) {
}
