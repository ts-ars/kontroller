package com.exempal.shiftcounter.features.signal.adapter.adam;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"prod", "test-adam"})
@EnableConfigurationProperties(AdamProperties.class)
public class AdamConfiguration {}
