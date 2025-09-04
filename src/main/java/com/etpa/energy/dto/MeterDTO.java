package com.etpa.energy.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Meter", description = "Meter identifier linked to a profile")
public record MeterDTO(
        @Schema(description = "Meter ID", example = "0001") String id,
        @Schema(description = "Linked profile code", example = "A") String profileCode
) {

}
