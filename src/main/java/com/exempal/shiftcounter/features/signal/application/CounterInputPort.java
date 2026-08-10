package com.exempal.shiftcounter.features.signal.application;

import com.exempal.shiftcounter.features.signal.domain.CounterProcessingResult;
import com.exempal.shiftcounter.features.signal.domain.CounterReadingCommand;

public interface CounterInputPort {
    CounterProcessingResult process(CounterReadingCommand command);
}
