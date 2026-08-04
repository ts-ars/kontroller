package com.exempal.shiftcounter.features.settings.api;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

public record SingleSettingRequest(
        @Schema(description = "Новое значение настройки", example = "250")
        @NotBlank(message = "Поле 'value' обязательно и не может быть пустым")
        String value
) {}
