package com.etpa.energy.web;

import com.etpa.energy.domain.Meter;
import com.etpa.energy.dto.MeterDTO;
import com.etpa.energy.repo.MeterRepository;
import com.etpa.energy.service.MeterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meters")
public class MeterController {
    private final MeterService service;
    private final MeterRepository repo;

    public MeterController(MeterService service, MeterRepository repo) {
        this.service = service; this.repo = repo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MeterDTO createOrUpdate(@RequestBody @Valid MeterDTO dto) {
        Meter m = service.upsert(dto);
        return new MeterDTO(m.getId(), m.getProfile().getCode());
        // if you later add more meter fields, map them here
    }

    @GetMapping
    public List<MeterDTO> list() {
        return repo.findAll().stream()
                .map(m -> new MeterDTO(m.getId(), m.getProfile().getCode()))
                .toList();
    }

    @GetMapping("/{id}")
    public MeterDTO get(@PathVariable String id) {
        Meter m = repo.findById(id).orElseThrow();
        return new MeterDTO(m.getId(), m.getProfile().getCode());
    }
}
