package com.etpa.energy.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "Profile", description = "Load profile with 12 monthly fractions that sum to 1.0")
public record ProfileDTO(
        @Schema(description = "Profile code", example = "A") String code,
        @Schema(description = "Human description", example = "Uniform") String description,
        @ArraySchema(schema = @Schema(implementation = FractionDTO.class),
                minItems = 12, maxItems = 12,
                arraySchema = @Schema(description = "12 fractions ordered Jan..Dec"))
        List<FractionDTO> fractions
) {

}
