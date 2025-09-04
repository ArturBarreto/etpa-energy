package com.etpa.energy.web;

import com.etpa.energy.dto.ConsumptionResponse;
import com.etpa.energy.service.ConsumptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.Month;

@RestController
@RequestMapping("/consumption")
@Tag(name = "Consumption")
public class ConsumptionController {
    private final ConsumptionService service;

    public ConsumptionController(ConsumptionService service) { this.service = service; }

    @GetMapping("/{meterId}/{year}/{month}")
    @Operation(summary = "Get monthly consumption",
            description = "Returns kWh consumed during the given month (difference to previous month)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ConsumptionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Reading not found",
                    content = @Content(schema = @Schema(implementation = com.etpa.energy.error.ApiError.class)))
    })
    public ConsumptionResponse get(
            @Parameter(example = "0001") @PathVariable String meterId,
            @Parameter(example = "2024") @PathVariable int year,
            @Parameter(example = "MARCH") @PathVariable Month month) {
        return service.getMonthly(meterId, year, month);
    }
}
