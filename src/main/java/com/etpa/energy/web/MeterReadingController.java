package com.etpa.energy.web;

import com.etpa.energy.dto.MeterReadingDTO;
import com.etpa.energy.dto.YearImportRequest;
import com.etpa.energy.service.MeterReadingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Month;
import java.util.List;

@RestController
@RequestMapping("/api/readings")
public class MeterReadingController {
    private final MeterReadingService service;
    public MeterReadingController(MeterReadingService service){ this.service=service; }

    // Import a full year (12 items) using simplified request
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void importYear(@RequestBody @Valid YearImportRequest req){
        service.importYear(req.meterId(), req.year(), req.readings());
    }

    // Get all readings (cumulative) for a meter/year
    @GetMapping("/{meterId}/{year}")
    public List<MeterReadingDTO> getYear(@PathVariable String meterId, @PathVariable int year) {
        return service.getYear(meterId, year);
    }

    // Get a single month's reading (cumulative) for a meter/year/month
    @GetMapping("/{meterId}/{year}/{month}")
    public MeterReadingDTO getOne(
            @PathVariable String meterId,
            @PathVariable int year,
            @PathVariable Month month
    ) {
        return service.getOne(meterId, year, month);
    }
}
