package com.exempal.shiftcounter.features.signal.domain;

public interface SignalInputPort {
    SignalRegistrationResult register(RegisterSignalCommand command);
}
