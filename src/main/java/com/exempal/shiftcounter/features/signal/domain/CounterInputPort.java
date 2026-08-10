package com.exempal.shiftcounter.features.signal.domain;

public interface CounterInputPort {
    CounterProcessingResult process(CounterReadingCommand command);
}
