package com.etpa.energy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Month;
import jakarta.validation.constraints.NotNull;

@Schema(name = "MeterReading", description = "Cumulative reading at end of month")
public record MeterReadingDTO(
        @Schema(description = "Month", example = "JANUARY") @NotNull Month month,
        @Schema(description = "Cumulative kWh (non-decreasing)", example = "120.000") @NotNull BigDecimal reading
) {

}
