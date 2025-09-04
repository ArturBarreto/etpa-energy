package com.etpa.energy.service;

import com.etpa.energy.domain.Meter;
import com.etpa.energy.domain.MeterReading;
import com.etpa.energy.dto.MeterReadingDTO;
import com.etpa.energy.repo.MeterReadingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.util.*;

@Service
public class MeterReadingService {
    private final MeterReadingRepository readings;
    private final MeterService meters;

    public MeterReadingService(MeterReadingRepository readings, MeterService meters){
        this.readings = readings; this.meters = meters;
    }

    @Transactional
    public MeterReading upsert(MeterReadingDTO dto, String meterId, int year){
        Meter meter = meters.getOrThrow(meterId);

        Month prevM = dto.month().minus(1);
        BigDecimal prevVal = BigDecimal.ZERO;
        if (dto.month() != Month.JANUARY) {
            prevVal = readings.findByMeter_IdAndYearAndMonth(meterId, year, prevM)
                    .map(MeterReading::getReading)
                    .orElse(BigDecimal.ZERO);
        }
        if (dto.reading().compareTo(prevVal) < 0)
            throw new IllegalArgumentException("Reading for " + dto.month() +
                    " cannot be lower than previous month (" + prevM + ")");

        MeterReading mr = readings.findByMeter_IdAndYearAndMonth(meterId, year, dto.month())
                .orElse(new MeterReading(meter, year, dto.month(), dto.reading()));
        mr.setReading(dto.reading());
        MeterReading saved = readings.save(mr);

        List<MeterReading> yearList = readings.findByMeter_IdAndYearOrderByMonthAsc(meterId, year);
        if (yearList.size() == 12) validateYearTolerance(meter, yearList);
        return saved;
    }

    @Transactional
    public void importYear(String meterId, int year, List<MeterReadingDTO> list){
        if (list.size() != 12) throw new IllegalArgumentException("Expected 12 readings for the year");
        Meter meter = meters.getOrThrow(meterId);

        // Sort by calendar order and validate non-decreasing readings
        list.sort(Comparator.comparingInt(r -> r.month().getValue()));
        BigDecimal prev = BigDecimal.ZERO;
        for (MeterReadingDTO r : list){
            if (r.month() != Month.JANUARY && r.reading().compareTo(prev) < 0)
                throw new IllegalArgumentException("Non-decreasing rule violated at " + r.month());
            prev = r.reading();
        }

        // Upsert each month
        for (MeterReadingDTO r : list){
            MeterReading mr = readings.findByMeter_IdAndYearAndMonth(meterId, year, r.month())
                    .orElse(new MeterReading(meter, year, r.month(), r.reading()));
            mr.setReading(r.reading());
            readings.save(mr);
        }

        // With full year, validate ±25% tolerance against profile fractions
        List<MeterReading> yearReadings = readings.findByMeter_IdAndYearOrderByMonthAsc(meterId, year);
        validateYearTolerance(meter, yearReadings);
    }

    private void validateYearTolerance(Meter meter, List<MeterReading> year){
        // Annual total = December reading (largest month)
        BigDecimal annual = year.stream()
                .max(Comparator.comparing(MeterReading::getMonth))
                .orElseThrow()
                .getReading();

        Map<Month, BigDecimal> fractions = new EnumMap<>(Month.class);
        meter.getProfile().getFractions().forEach(f -> fractions.put(f.getMonth(), f.getValue()));
        if (fractions.size() != 12) throw new IllegalStateException("Profile fractions incomplete");

        BigDecimal prev = BigDecimal.ZERO;
        for (Month m : Month.values()){
            MeterReading curr = year.stream().filter(r -> r.getMonth() == m).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Missing reading for " + m));
            BigDecimal cons = curr.getReading().subtract(prev);
            prev = curr.getReading();

            BigDecimal expected = annual.multiply(fractions.get(m));
            BigDecimal lower = expected.multiply(new BigDecimal("0.75")).setScale(3, RoundingMode.HALF_UP);
            BigDecimal upper = expected.multiply(new BigDecimal("1.25")).setScale(3, RoundingMode.HALF_UP);

            if (cons.compareTo(lower) < 0 || cons.compareTo(upper) > 0) {
                throw new IllegalArgumentException(
                        "Consumption for " + m + " (" + cons + ") out of allowed range [" + lower + ", " + upper + "]"
                );
            }
        }
    }
}
