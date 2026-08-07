package com.exempal.shiftcounter.features.signal.adapter.http;

import com.exempal.shiftcounter.features.signal.domain.SignalInputPort;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Profile;

@RestController
@RequestMapping("/api/signal")
@Profile("test")
public class HttpSignalAdapter {

    private final SignalInputPort signalInput;

    public HttpSignalAdapter(SignalInputPort signalInput) {
        this.signalInput = signalInput;
    }

    @PostMapping("/product")
    public void productSignal() {
        signalInput.onProductSensorTriggered();
    }
}
