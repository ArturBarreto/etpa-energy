package com.etpa.energy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Month;

@Schema(name = "ConsumptionResponse", description = "Derived monthly consumption (difference vs previous month)")
public record ConsumptionResponse(
        @Schema(example = "0001") String meterId,
        @Schema(example = "2024") int year,
        @Schema(example = "MARCH") Month month,
        @Schema(description = "kWh consumed during the month", example = "30.000") BigDecimal consumption
) {

}
