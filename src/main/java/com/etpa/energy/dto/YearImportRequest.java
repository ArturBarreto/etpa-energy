package com.etpa.energy.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import jakarta.validation.constraints.NotEmpty;

@Schema(name = "YearImportRequest", description = "Compact payload to import 12 monthly readings for a year")
public record YearImportRequest(
        @Schema(description = "Meter ID", example = "0001") @NotEmpty String meterId,
        @Schema(description = "Reading year", example = "2024") int year,
        @ArraySchema(minItems = 12, maxItems = 12,
                schema = @Schema(implementation = MeterReadingDTO.class),
                arraySchema = @Schema(description = "12 cumulative monthly readings, Jan..Dec"))
        @NotEmpty List<MeterReadingDTO> readings
) {

}
