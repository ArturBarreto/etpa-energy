package com.etpa.energy.web;

import com.etpa.energy.dto.FractionDTO;
import com.etpa.energy.dto.ProfileDTO;
import com.etpa.energy.domain.Profile;
import com.etpa.energy.repo.ProfileRepository;
import com.etpa.energy.service.ProfileService;
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
@RequestMapping("/profiles")
@Tag(name = "Profiles")
public class ProfileController {
    private final ProfileService service;
    private final ProfileRepository repo;

    public ProfileController(ProfileService service, ProfileRepository repo) {
        this.service = service; this.repo = repo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create or update a profile",
            description = "Upserts a profile with 12 monthly fractions that must sum to 1.0",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProfileDTO.class),
                            examples = @ExampleObject(
                                    name = "Uniform",
                                    value = """
                           {
                             "code": "A",
                             "description": "Uniform",
                             "fractions": [
                               {"month":"JANUARY","value":0.083333},
                               {"month":"FEBRUARY","value":0.083333},
                               {"month":"MARCH","value":0.083333},
                               {"month":"APRIL","value":0.083333},
                               {"month":"MAY","value":0.083333},
                               {"month":"JUNE","value":0.083333},
                               {"month":"JULY","value":0.083333},
                               {"month":"AUGUST","value":0.083333},
                               {"month":"SEPTEMBER","value":0.083333},
                               {"month":"OCTOBER","value":0.083333},
                               {"month":"NOVEMBER","value":0.083333},
                               {"month":"DECEMBER","value":0.083337}
                             ]
                           }
                           """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profile upserted", content = @Content(
                    schema = @Schema(implementation = ProfileDTO.class),
                    examples = @ExampleObject(
                            name = "Uniform",
                            value = """
                           {
                             "code": "A",
                             "description": "Uniform",
                             "fractions": [
                               {"month":"JANUARY","value":0.083333},
                               {"month":"FEBRUARY","value":0.083333},
                               {"month":"MARCH","value":0.083333},
                               {"month":"APRIL","value":0.083333},
                               {"month":"MAY","value":0.083333},
                               {"month":"JUNE","value":0.083333},
                               {"month":"JULY","value":0.083333},
                               {"month":"AUGUST","value":0.083333},
                               {"month":"SEPTEMBER","value":0.083333},
                               {"month":"OCTOBER","value":0.083333},
                               {"month":"NOVEMBER","value":0.083333},
                               {"month":"DECEMBER","value":0.083337}
                             ]
                           }
                           """
                    )
            )),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = com.etpa.energy.error.ApiError.class)))
    })
    public ProfileDTO createOrUpdate(@RequestBody @Valid ProfileDTO dto) {
        Profile saved = service.upsert(dto);
        return toDto(saved);
    }

    @GetMapping
    @Operation(summary = "List all profiles")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK", content = @Content(
            schema = @Schema(implementation = ProfileDTO.class),
            examples = @ExampleObject(
                    name = "Uniform",
                    value = """
                           {
                             "code": "A",
                             "description": "Uniform",
                             "fractions": [
                               {"month":"JANUARY","value":0.083333},
                               {"month":"FEBRUARY","value":0.083333},
                               {"month":"MARCH","value":0.083333},
                               {"month":"APRIL","value":0.083333},
                               {"month":"MAY","value":0.083333},
                               {"month":"JUNE","value":0.083333},
                               {"month":"JULY","value":0.083333},
                               {"month":"AUGUST","value":0.083333},
                               {"month":"SEPTEMBER","value":0.083333},
                               {"month":"OCTOBER","value":0.083333},
                               {"month":"NOVEMBER","value":0.083333},
                               {"month":"DECEMBER","value":0.083337}
                             ]
                           }
                           """
            )
    )) })
    public List<ProfileDTO> list() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get a profile by code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(
                    schema = @Schema(implementation = ProfileDTO.class),
                    examples = @ExampleObject(
                            name = "Uniform",
                            value = """
                           {
                             "code": "A",
                             "description": "Uniform",
                             "fractions": [
                               {"month":"JANUARY","value":0.083333},
                               {"month":"FEBRUARY","value":0.083333},
                               {"month":"MARCH","value":0.083333},
                               {"month":"APRIL","value":0.083333},
                               {"month":"MAY","value":0.083333},
                               {"month":"JUNE","value":0.083333},
                               {"month":"JULY","value":0.083333},
                               {"month":"AUGUST","value":0.083333},
                               {"month":"SEPTEMBER","value":0.083333},
                               {"month":"OCTOBER","value":0.083333},
                               {"month":"NOVEMBER","value":0.083333},
                               {"month":"DECEMBER","value":0.083337}
                             ]
                           }
                           """
                    )
            )),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content(schema = @Schema(implementation = com.etpa.energy.error.ApiError.class)))
    })
    public ProfileDTO get(@PathVariable String code) {
        return repo.findById(code).map(this::toDto).orElseThrow();
    }

    private ProfileDTO toDto(Profile p) {
        var fs = p.getFractions().stream()
                .map(f -> new FractionDTO(f.getMonth(), f.getValue()))
                .toList();
        return new ProfileDTO(p.getCode(), p.getDescription(), fs);
    }
}
