package com.exempal.shiftcounter.testsupport;

import com.exempal.shiftcounter.features.signal.adapter.adam.AdamModbusAdapter;
import com.exempal.shiftcounter.features.signal.adapter.event.AdamEventEmitter;
import com.exempal.shiftcounter.features.signal.adapter.http.HttpSignalAdapter;
import com.exempal.shiftcounter.features.signal.adapter.web.SignalController;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.AnnotatedElementUtils;

import static org.assertj.core.api.Assertions.assertThat;

@org.junit.jupiter.api.Tag("unit")
class ProfileGatingTest {

    @Test
    void adamBeansAreProductionOnly() {
        assertProfile(AdamModbusAdapter.class, "prod");
        assertProfile(AdamEventEmitter.class, "prod");
    }

    @Test
    void httpSimulationEndpointsAreTestOnly() {
        assertProfile(HttpSignalAdapter.class, "test");
        assertProfile(SignalController.class, "test");
    }

    private static void assertProfile(Class<?> beanType, String expectedProfile) {
        Profile profile = AnnotatedElementUtils.findMergedAnnotation(beanType, Profile.class);
        assertThat(profile).isNotNull();
        assertThat(profile.value()).containsExactly(expectedProfile);
    }
}
