package com.etpa.energy.web;

import com.etpa.energy.dto.ImportRequest;
import com.etpa.energy.dto.MeterReadingDTO;
import com.etpa.energy.service.MeterReadingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/readings")
public class MeterReadingController {
    private final MeterReadingService service;

    public MeterReadingController(MeterReadingService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MeterReadingDTO upsert(@RequestBody @Valid MeterReadingDTO dto) {
        var mr = service.upsert(dto);
        return new MeterReadingDTO(
                mr.getMeter().getId(),
                mr.getYear(),
                mr.getMonth(),
                mr.getReading()
        );
    }

    // Batch import for single meter-year (exactly 12 rows)
    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    public void importYear(@RequestBody @Valid ImportRequest req) {
        service.importYear(req.meterId(), req.year(), req.readings());
    }
}
