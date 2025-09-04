package com.etpa.energy.web;

import com.etpa.energy.dto.MeterDTO;
import com.etpa.energy.domain.Meter;
import com.etpa.energy.repo.MeterRepository;
import com.etpa.energy.service.MeterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/meters")
@Tag(name = "Meters")
public class MeterController {
    private final MeterService service;
    private final MeterRepository repo;

    public MeterController(MeterService service, MeterRepository repo) {
        this.service = service; this.repo = repo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create or update a meter",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = MeterDTO.class),
                            examples = @ExampleObject(
                                    name = "Link meter to profile",
                                    value = """
                           {
                             "id": "0001",
                             "profileCode": "A"
                           }
                           """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Meter upserted"),
            @ApiResponse(responseCode = "404", description = "Profile not found",
                    content = @Content(schema = @Schema(implementation = com.etpa.energy.error.ApiError.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = com.etpa.energy.error.ApiError.class)))
    })
    public MeterDTO createOrUpdate(@RequestBody @Valid MeterDTO dto) {
        Meter m = service.upsert(dto);
        return new MeterDTO(m.getId(), m.getProfile().getCode());
    }

    @GetMapping
    @Operation(summary = "List all meters")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public List<MeterDTO> list() {
        return repo.findAll().stream()
                .map(m -> new MeterDTO(m.getId(), m.getProfile().getCode()))
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a meter by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content(schema = @Schema(implementation = com.etpa.energy.error.ApiError.class)))
    })
    public MeterDTO get(@PathVariable String id) {
        Meter m = repo.findById(id).orElseThrow();
        return new MeterDTO(m.getId(), m.getProfile().getCode());
    }
}
