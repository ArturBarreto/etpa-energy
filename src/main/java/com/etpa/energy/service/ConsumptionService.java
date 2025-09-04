package com.etpa.energy.service;

import com.etpa.energy.domain.MeterReading;
import com.etpa.energy.dto.ConsumptionResponse;
import com.etpa.energy.repo.MeterReadingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Month;

@Service
public class ConsumptionService {
    private final MeterReadingRepository readings;
    public ConsumptionService(MeterReadingRepository readings){ this.readings = readings; }

    public ConsumptionResponse getMonthlyConsumption(String meterId, int year, Month month) {
        MeterReading curr = readings.findByMeter_IdAndYearAndMonth(meterId, year, month)
                .orElseThrow(() -> new IllegalArgumentException("Reading not found"));
        BigDecimal prevVal = BigDecimal.ZERO;
        if (month != Month.JANUARY) {
            prevVal = readings.findByMeter_IdAndYearAndMonth(meterId, year, month.minus(1))
                    .map(MeterReading::getReading).orElseThrow(() -> new IllegalArgumentException("Previous month missing"));
        }
        BigDecimal cons = curr.getReading().subtract(prevVal);
        return new ConsumptionResponse(meterId, year, month, cons);
    }
}
