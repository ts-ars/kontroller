package com.exempal.shiftcounter.testsupport;

import com.exempal.shiftcounter.features.signal.adapter.http.HttpSignalAdapter;
import com.exempal.shiftcounter.features.signal.adapter.web.SignalController;
import com.exempal.shiftcounter.features.signal.domain.SignalInputPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {HttpSignalAdapter.class, SignalController.class})
@ActiveProfiles("test")
@org.junit.jupiter.api.Tag("web")
class SignalSimulationWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SignalInputPort signalInputPort;

    @Test
    void exposesBothSimulationEndpointsInTestProfile() throws Exception {
        mockMvc.perform(post("/api/signal/product"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/signal")
                        .param("shiftDate", "2026-08-07")
                        .param("sensor", "sensor-1"))
                .andExpect(status().isOk());

        verify(signalInputPort, times(2)).onProductSensorTriggered();
    }
}
