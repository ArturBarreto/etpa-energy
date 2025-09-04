package com.etpa.energy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Month;

@Schema(name = "Fraction", description = "Monthly fraction of annual consumption")
public record FractionDTO(
        @Schema(description = "Month", example = "JANUARY") Month month,
        @Schema(description = "Fraction value [0..1]", example = "0.083333") BigDecimal value
) {

}
