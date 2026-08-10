package com.exempal.shiftcounter.features.signal.domain;

import java.time.LocalDate;

public record CounterProcessingResult(CounterProcessingStatus status, long delta,
                                      long acceptedSignals, LocalDate attributedProductionDate) {
}
