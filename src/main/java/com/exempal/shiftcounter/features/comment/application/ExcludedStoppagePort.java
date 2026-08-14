package com.exempal.shiftcounter.features.comment.application;

import java.time.LocalDateTime;

public interface ExcludedStoppagePort {
    void exclude(long id, String actor, String reason, LocalDateTime at);
    void restore(long id);
}
