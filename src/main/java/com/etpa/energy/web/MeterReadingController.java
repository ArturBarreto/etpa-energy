package com.etpa.energy.web;

import com.etpa.energy.dto.MeterReadingDTO;
import com.etpa.energy.dto.YearImportRequest;
import com.etpa.energy.service.MeterReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Month;
import java.util.List;

@RestController
@RequestMapping("/readings")
@Tag(name = "Readings")
public class MeterReadingController {
    private final MeterReadingService service;
    public MeterReadingController(MeterReadingService service){ this.service=service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Import full year readings",
            description = "Imports 12 cumulative monthly readings for a given meter/year",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = YearImportRequest.class),
                            examples = @ExampleObject(
                                    name = "Year import",
                                    value = """
                           {
                             "meterId": "0001",
                             "year": 2024,
                             "readings": [
                               {"month":"JANUARY","reading":10},
                               {"month":"FEBRUARY","reading":20},
                               {"month":"MARCH","reading":30},
                               {"month":"APRIL","reading":40},
                               {"month":"MAY","reading":50},
                               {"month":"JUNE","reading":60},
                               {"month":"JULY","reading":70},
                               {"month":"AUGUST","reading":80},
                               {"month":"SEPTEMBER","reading":90},
                               {"month":"OCTOBER","reading":100},
                               {"month":"NOVEMBER","reading":110},
                               {"month":"DECEMBER","reading":120}
                             ]
                           }
                           """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Year imported"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = com.etpa.energy.error.ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Meter not found",
                    content = @Content(schema = @Schema(implementation = com.etpa.energy.error.ApiError.class)))
    })
    public void importYear(@RequestBody @Valid YearImportRequest req){
        service.importYear(req.meterId(), req.year(), req.readings());
    }

    @GetMapping("/{meterId}/{year}")
    @Operation(summary = "Get readings for a year")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MeterReadingDTO.class)))),
            @ApiResponse(responseCode = "404", description = "No readings for meter/year",
                    content = @Content(schema = @Schema(implementation = com.etpa.energy.error.ApiError.class)))
    })
    public List<MeterReadingDTO> getYear(
            @Parameter(example = "0001") @PathVariable String meterId,
            @Parameter(example = "2024") @PathVariable int year) {
        return service.getYear(meterId, year);
    }

    @GetMapping("/{meterId}/{year}/{month}")
    @Operation(summary = "Get a single month's reading")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Reading not found",
                    content = @Content(schema = @Schema(implementation = com.etpa.energy.error.ApiError.class)))
    })
    public MeterReadingDTO getOne(
            @Parameter(example = "0001") @PathVariable String meterId,
            @Parameter(example = "2024") @PathVariable int year,
            @Parameter(example = "MARCH") @PathVariable Month month) {
        return service.getOne(meterId, year, month);
    }
}
