package com.exempal.shiftcounter.features.signal.application;

import com.exempal.shiftcounter.features.signal.domain.RegisterSignalCommand;
import com.exempal.shiftcounter.features.signal.domain.SignalRegistrationResult;

public interface SignalInputPort {
    SignalRegistrationResult register(RegisterSignalCommand command);
}
