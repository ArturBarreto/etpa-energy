package com.etpa.energy.service;

import com.etpa.energy.domain.*;
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

    public MeterReadingService(MeterReadingRepository readings, MeterService meters) {
        this.readings = readings; this.meters = meters;
    }

    @Transactional
    public MeterReading upsert(MeterReadingDTO dto) {
        Meter meter = meters.getOrThrow(dto.meterId()); // validates profile existence via meter
        // ensure non-decreasing vs previous month in same year
        Month prev = dto.month().minus(1);
        BigDecimal prevValue = BigDecimal.ZERO;
        if (dto.month() != Month.JANUARY) {
            MeterReading prevReading = readings.findByMeter_IdAndYearAndMonth(dto.meterId(), dto.year(), prev).orElse(null);
            if (prevReading != null) prevValue = prevReading.getReading();
        }
        if (dto.month() == Month.JANUARY) {
            prevValue = BigDecimal.ZERO; // reset at year start
        }
        if (dto.reading().compareTo(prevValue) < 0) {
            throw new IllegalArgumentException("Reading for " + dto.month() + " cannot be lower than previous month (" + prev + ")");
        }
        MeterReading mr = readings.findByMeter_IdAndYearAndMonth(dto.meterId(), dto.year(), dto.month())
                .orElse(new MeterReading(meter, dto.year(), dto.month(), dto.reading()));
        mr.setReading(dto.reading());
        MeterReading saved = readings.save(mr);
        // if we now have 12 readings for the year, validate tolerance across the year
        List<MeterReading> year = readings.findByMeter_IdAndYearOrderByMonthAsc(dto.meterId(), dto.year());
        if (year.size() == 12) validateYearTolerance(meter, year);
        return saved;
    }

    @Transactional
    public void importYear(String meterId, int year, List<MeterReadingDTO> list) {
        // Expect 12 readings covering all months; validate batch atomically
        if (list.size() != 12) throw new IllegalArgumentException("Expected 12 readings for the year");
        Meter meter = meters.getOrThrow(meterId);
        // sort by month index 1..12
        list.sort(Comparator.comparingInt(r -> r.month().getValue()));
        BigDecimal prev = BigDecimal.ZERO;
        for (MeterReadingDTO r : list) {
            if (r.year() != year) throw new IllegalArgumentException("Mixed years in batch");
            if (r.month() != Month.JANUARY && r.reading().compareTo(prev) < 0)
                throw new IllegalArgumentException("Non-decreasing rule violated at " + r.month());
            prev = r.reading();
        }
        // persist then validate tolerance
        for (MeterReadingDTO r : list) {
            MeterReading mr = readings.findByMeter_IdAndYearAndMonth(meterId, year, r.month())
                    .orElse(new MeterReading(meter, year, r.month(), r.reading()));
            mr.setReading(r.reading());
            readings.save(mr);
        }
        List<MeterReading> yearReadings = readings.findByMeter_IdAndYearOrderByMonthAsc(meterId, year);
        validateYearTolerance(meter, yearReadings);
    }

    private void validateYearTolerance(Meter meter, List<MeterReading> year) {
        // Annual total = December reading (January starts at 0)
        BigDecimal annual = year.stream().max(Comparator.comparing(MeterReading::getMonth))
                .orElseThrow().getReading();
        // obtain profile fractions for each month
        Map<Month, BigDecimal> fractions = new EnumMap<>(Month.class);
        meter.getProfile().getFractions().forEach(f -> fractions.put(f.getMonth(), f.getValue()));
        if (fractions.size() != 12) throw new IllegalStateException("Profile fractions incomplete");
        // iterate months and check consumption vs expected ±25%
        BigDecimal prev = BigDecimal.ZERO;
        for (Month m : Month.values()) {
            MeterReading curr = year.stream().filter(r -> r.getMonth() == m).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Missing reading for " + m));
            BigDecimal cons = curr.getReading().subtract(prev);
            prev = curr.getReading();
            BigDecimal expected = annual.multiply(fractions.get(m));
            BigDecimal lower = expected.multiply(new BigDecimal("0.75")).setScale(3, RoundingMode.HALF_UP);
            BigDecimal upper = expected.multiply(new BigDecimal("1.25")).setScale(3, RoundingMode.HALF_UP);
            if (cons.compareTo(lower) < 0 || cons.compareTo(upper) > 0) {
                throw new IllegalArgumentException("Consumption for " + m + " (" + cons + ") out of allowed range [" + lower + "," + upper + "]");
            }
        }
    }
}
