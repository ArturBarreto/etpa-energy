package com.etpa.energy.service;

import com.etpa.energy.domain.Meter;
import com.etpa.energy.domain.MeterReading;
import com.etpa.energy.domain.Profile;
import com.etpa.energy.dto.ConsumptionResponse;
import com.etpa.energy.repo.MeterReadingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Month;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumptionServiceTest {

    @Mock MeterReadingRepository readings;

    @Test
    void january_uses_zero_as_previous() {
        var service = new ConsumptionService(readings);
        var meter = new Meter("0001", new Profile("A", "x"));

        when(readings.findByMeter_IdAndYearAndMonth("0001", 2024, Month.JANUARY))
                .thenReturn(Optional.of(new MeterReading(meter, 2024, Month.JANUARY, new BigDecimal("15"))));

        ConsumptionResponse res = service.getMonthly("0001", 2024, Month.JANUARY);
        assertEquals(new BigDecimal("15"), res.consumption());
    }

    @Test
    void february_diff_is_curr_minus_prev() {
        var service = new ConsumptionService(readings);
        var meter = new Meter("0001", new Profile("A", "x"));

        when(readings.findByMeter_IdAndYearAndMonth("0001", 2024, Month.FEBRUARY))
                .thenReturn(Optional.of(new MeterReading(meter, 2024, Month.FEBRUARY, new BigDecimal("30"))));
        when(readings.findByMeter_IdAndYearAndMonth("0001", 2024, Month.JANUARY))
                .thenReturn(Optional.of(new MeterReading(meter, 2024, Month.JANUARY, new BigDecimal("10"))));

        var res = service.getMonthly("0001", 2024, Month.FEBRUARY);
        assertEquals(new BigDecimal("20"), res.consumption());
    }

    @Test
    void missing_prev_month_throws() {
        var service = new ConsumptionService(readings);
        var meter = new Meter("0001", new Profile("A", "x"));

        when(readings.findByMeter_IdAndYearAndMonth("0001", 2024, Month.MARCH))
                .thenReturn(Optional.of(new MeterReading(meter, 2024, Month.MARCH, new BigDecimal("18"))));
        when(readings.findByMeter_IdAndYearAndMonth("0001", 2024, Month.FEBRUARY))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.getMonthly("0001", 2024, Month.MARCH));
    }
}
