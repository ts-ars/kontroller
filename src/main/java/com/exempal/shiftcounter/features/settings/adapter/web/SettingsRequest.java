package com.exempal.shiftcounter.features.settings.adapter.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public record SettingsRequest(@NotEmpty List<@NotBlank String> hours,
                              @NotEmpty List<@NotNull @PositiveOrZero Integer> sensors1To4Plans,
                              @NotEmpty List<@NotNull @PositiveOrZero Integer> sensor6Plans) {
}
