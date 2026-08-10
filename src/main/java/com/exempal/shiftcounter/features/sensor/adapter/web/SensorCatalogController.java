package com.exempal.shiftcounter.features.sensor.adapter.web;

import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.features.sensor.domain.SensorDefinition;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sensors")
public class SensorCatalogController {
    @GetMapping
    public List<SensorDefinition> list() {
        return SensorCatalog.all();
    }
}
