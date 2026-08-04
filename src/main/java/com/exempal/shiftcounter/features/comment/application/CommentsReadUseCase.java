package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.StoppageEntry;
import com.exempal.shiftcounter.features.shift.domain.Shift;

import java.time.LocalDate;
import java.util.List;

public interface CommentsReadUseCase {
    Data read(LocalDate date);

    /** Чистые доменные объекты, без DTO и человекочитаемых строк. */
    record Data(Shift shift, List<StoppageEntry> rows, List<StoppageEntry> missing) {}
}