package com.etpa.energy.web;

import com.etpa.energy.domain.Profile;
import com.etpa.energy.dto.FractionDTO;
import com.etpa.energy.dto.ProfileDTO;
import com.etpa.energy.repo.ProfileRepository;
import com.etpa.energy.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final ProfileService service;
    private final ProfileRepository repo;

    public ProfileController(ProfileService service, ProfileRepository repo) {
        this.service = service; this.repo = repo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileDTO createOrUpdate(@RequestBody @Valid ProfileDTO dto) {
        Profile saved = service.upsert(dto);
        return toDto(saved);
    }

    @GetMapping
    public List<ProfileDTO> list() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    @GetMapping("/{code}")
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
