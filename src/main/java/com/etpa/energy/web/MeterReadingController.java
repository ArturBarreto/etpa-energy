package com.etpa.energy.web;

import com.etpa.energy.dto.*;
import com.etpa.energy.service.MeterReadingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/readings")
public class MeterReadingController {
    private final MeterReadingService service;
    public MeterReadingController(MeterReadingService service){ this.service=service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void importYear(@RequestBody @Valid YearImportRequest req){
        service.importYear(req.meterId(), req.year(), req.readings());
    }
}
