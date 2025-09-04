package com.etpa.energy.web;

import com.etpa.energy.dto.ConsumptionResponse;
import com.etpa.energy.service.ConsumptionService;
import org.springframework.web.bind.annotation.*;

import java.time.Month;

@RestController
@RequestMapping("/api/consumption")
public class ConsumptionController {
    private final ConsumptionService service;

    public ConsumptionController(ConsumptionService service) {
        this.service = service;
    }

    @GetMapping("/{meterId}/{year}/{month}")
    public ConsumptionResponse get(
            @PathVariable String meterId,
            @PathVariable int year,
            @PathVariable Month month) {
        return service.getMonthly(meterId, year, month);
    }
}
