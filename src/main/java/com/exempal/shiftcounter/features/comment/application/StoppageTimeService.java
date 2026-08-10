package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class StoppageTimeService {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    public String getPreciseTime(Stoppage stoppage) {
        return stoppage.startedAt().format(TIME);
    }

    public String format(Stoppage stoppage) {
        return getPreciseTime(stoppage);
    }
}
