package com.exempal.shiftcounter.features.comment.event;

import java.time.LocalDate;
import java.util.List;

public record CommentsUpdatedEvent(LocalDate date, List<String> comments) {
}
