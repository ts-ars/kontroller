package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import com.exempal.shiftcounter.features.shift.domain.Shift;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CommentsReadUseCase {
    Data read(LocalDate date, String sensorId);

    default Data read(LocalDate date) {
        return read(date, com.exempal.shiftcounter.features.sensor.domain.SensorCatalog.SENSOR_1);
    }

    /** Чистые доменные объекты, без DTO и человекочитаемых строк. */
    record ExplanationRow(String sourceSensorId, LocalDateTime time, LossCategory category,
                          String comment, int minutes, String authorDisplayName) {
        public ExplanationRow(String sourceSensorId, LocalDateTime time, LossCategory category,
                              String comment, int minutes) {
            this(sourceSensorId, time, category, comment, minutes, "");
        }
    }

    record SourceComments(String sensorId, List<ExplanationRow> rows) {
        public int totalMinutes() {
            return rows.stream().mapToInt(ExplanationRow::minutes).sum();
        }
    }

    record Data(Shift shift, List<Stoppage> rows, List<Stoppage> missing,
                List<SourceComments> sourceComments) {
        public Data(Shift shift, List<Stoppage> rows, List<Stoppage> missing) {
            this(shift, rows, missing, List.of());
        }
    }
}
