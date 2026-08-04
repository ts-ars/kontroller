package com.exempal.shiftcounter.features.signal.domain;

import java.time.LocalDateTime;
import java.util.List;

public interface SignalStoragePort {
    void save(Signal signal);
    List<Signal> findByRange(LocalDateTime from, LocalDateTime to);
}
