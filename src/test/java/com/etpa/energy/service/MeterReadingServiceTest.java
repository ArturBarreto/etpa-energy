package com.etpa.energy.service;

import com.etpa.energy.domain.Fraction;
import com.etpa.energy.domain.Meter;
import com.etpa.energy.domain.MeterReading;
import com.etpa.energy.domain.Profile;
import com.etpa.energy.dto.MeterReadingDTO;
import com.etpa.energy.repo.MeterReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;   // << add
import org.mockito.junit.jupiter.MockitoSettings; // << add

import java.math.BigDecimal;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  // << make class lenient
class MeterReadingServiceTest {

    @Mock MeterReadingRepository readings;
    @Mock MeterService meters;

    @InjectMocks MeterReadingService service;

    private Profile uniformProfile;
    private Meter meter;

    @BeforeEach
    void setUp() {
        // Uniform profile: 1/12 each month (sums to 1)
        uniformProfile = new Profile("A", "Uniform");
        var fractions = Stream.of(Month.values())
                .map(m -> new Fraction(uniformProfile, m, BigDecimal.valueOf(1.0/12.0)))
                .collect(Collectors.toList());
        uniformProfile.getFractions().addAll(fractions);

        meter = new Meter("0001", uniformProfile);
        when(meters.getOrThrow("0001")).thenReturn(meter); // used by importYear/upsert tests
    }

    @Test
    void upsert_rejects_decreasing_reading() {
        when(readings.findByMeter_IdAndYearAndMonth("0001", 2024, Month.JANUARY))
                .thenReturn(Optional.of(new MeterReading(meter, 2024, Month.JANUARY, BigDecimal.TEN)));

        var feb = new MeterReadingDTO(Month.FEBRUARY, new BigDecimal("9"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.upsert(feb, "0001", 2024));
        assertTrue(ex.getMessage().toLowerCase().contains("cannot be lower"));
        verify(readings, never()).save(any());
    }

    @Test
    void importYear_accepts_uniform_profile_with_10_each_month() {
        List<MeterReadingDTO> list = new ArrayList<>();
        BigDecimal v = BigDecimal.ZERO;
        for (Month m : Month.values()) {
            v = v.add(BigDecimal.TEN);
            list.add(new MeterReadingDTO(m, v));
        }

        when(readings.findByMeter_IdAndYearAndMonth(anyString(), anyInt(), any()))
                .thenReturn(Optional.empty());

        List<MeterReading> saved = new ArrayList<>();
        when(readings.save(any(MeterReading.class))).thenAnswer(inv -> {
            MeterReading mr = inv.getArgument(0);
            saved.add(mr);
            return mr;
        });
        when(readings.findByMeter_IdAndYearOrderByMonthAsc("0001", 2024))
                .thenAnswer(inv -> saved.stream()
                        .sorted(Comparator.comparing(MeterReading::getMonth))
                        .toList());

        assertDoesNotThrow(() -> service.importYear("0001", 2024, list));
        assertEquals(12, saved.size());
    }

    @Test
    void importYear_rejects_month_outside_25pct_tolerance() {
        List<MeterReadingDTO> list = new ArrayList<>();
        BigDecimal cumulative = BigDecimal.ZERO;

        for (Month m : Month.values()) {
            BigDecimal increment = BigDecimal.TEN;
            if (m == Month.MARCH) increment = BigDecimal.ONE; // too small for tolerance
            cumulative = cumulative.add(increment);
            list.add(new MeterReadingDTO(m, cumulative));
        }

        when(readings.findByMeter_IdAndYearAndMonth(anyString(), anyInt(), any()))
                .thenReturn(Optional.empty());
        List<MeterReading> saved = new ArrayList<>();
        when(readings.save(any(MeterReading.class))).thenAnswer(inv -> {
            MeterReading mr = inv.getArgument(0);
            saved.add(mr);
            return mr;
        });
        when(readings.findByMeter_IdAndYearOrderByMonthAsc("0001", 2024))
                .thenAnswer(inv -> saved.stream()
                        .sorted(Comparator.comparing(MeterReading::getMonth))
                        .toList());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.importYear("0001", 2024, list));
        assertTrue(ex.getMessage().toLowerCase().contains("consumption for march"));
    }

    @Test
    void getYear_returns_sorted_dtos_and_404_when_empty() {
        List<MeterReading> persisted = new ArrayList<>();
        BigDecimal v = BigDecimal.ZERO;
        for (Month m : Month.values()) {
            v = v.add(BigDecimal.TEN);
            persisted.add(new MeterReading(meter, 2024, m, v));
        }
        when(readings.findByMeter_IdAndYearOrderByMonthAsc("0001", 2024))
                .thenReturn(persisted);

        var dtos = service.getYear("0001", 2024);
        assertEquals(12, dtos.size());
        assertEquals(Month.JANUARY, dtos.get(0).month());
        assertEquals(Month.DECEMBER, dtos.get(11).month());

        when(readings.findByMeter_IdAndYearOrderByMonthAsc("0002", 2024))
                .thenReturn(List.of());
        assertThrows(NoSuchElementException.class, () -> service.getYear("0002", 2024));
    }

    @Test
    void importYear_accepts_exact_tolerance_bounds() {
        // Expected monthly consumption = 10 (uniform fractions, annual 120)
        // Use increments: mostly 10, but MARCH = 7.5 (0.75×) and APRIL = 12.5 (1.25×)
        List<MeterReadingDTO> list = new ArrayList<>();
        BigDecimal cumulative = BigDecimal.ZERO;
        for (Month m : Month.values()) {
            BigDecimal inc = BigDecimal.TEN;
            if (m == Month.MARCH) inc = new BigDecimal("7.5");
            if (m == Month.APRIL) inc = new BigDecimal("12.5");
            cumulative = cumulative.add(inc);
            list.add(new MeterReadingDTO(m, cumulative));
        }

        when(readings.findByMeter_IdAndYearAndMonth(anyString(), anyInt(), any()))
                .thenReturn(Optional.empty());

        List<MeterReading> saved = new ArrayList<>();
        when(readings.save(any(MeterReading.class))).thenAnswer(inv -> {
            MeterReading mr = inv.getArgument(0);
            saved.add(mr);
            return mr;
        });
        when(readings.findByMeter_IdAndYearOrderByMonthAsc("0001", 2024))
                .thenAnswer(inv -> saved.stream()
                        .sorted(Comparator.comparing(MeterReading::getMonth))
                        .toList());

        assertDoesNotThrow(() -> service.importYear("0001", 2024, list));
        assertEquals(12, saved.size());
    }

    @Test
    void importYear_rejects_just_outside_tolerance_bounds() {
        // Base plan: 11 months at +10, 1 month (MARCH) at +6.
        // Annual = 11*10 + 6 = 116 → expected monthly ≈ 9.6667
        // Lower bound = 0.75 * 9.6667 ≈ 7.25  → 6.0 is below → should FAIL.
        List<MeterReadingDTO> list = new ArrayList<>();
        BigDecimal cumulative = BigDecimal.ZERO;
        for (Month m : Month.values()) {
            BigDecimal inc = BigDecimal.TEN;
            if (m == Month.MARCH) inc = new BigDecimal("6.0"); // safely below lower tolerance
            cumulative = cumulative.add(inc);
            list.add(new MeterReadingDTO(m, cumulative));
        }

        when(readings.findByMeter_IdAndYearAndMonth(anyString(), anyInt(), any()))
                .thenReturn(Optional.empty());

        // capture saved readings so service can read back and validate
        List<MeterReading> saved = new ArrayList<>();
        when(readings.save(any(MeterReading.class))).thenAnswer(inv -> {
            MeterReading mr = inv.getArgument(0);
            saved.add(mr);
            return mr;
        });
        when(readings.findByMeter_IdAndYearOrderByMonthAsc("0001", 2024))
                .thenAnswer(inv -> saved.stream()
                        .sorted(Comparator.comparing(MeterReading::getMonth))
                        .toList());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.importYear("0001", 2024, list));
        assertTrue(ex.getMessage().toLowerCase().contains("march"));
    }

    @Test
    void importYear_rejects_upper_bound_violation() {
        // 11 months at +10, APRIL at +14.5
        // Annual = 124.5 → expected monthly ≈ 10.375; upper bound 1.25× ≈ 12.97
        // 14.5 > 12.97 → should FAIL.
        List<MeterReadingDTO> list = new ArrayList<>();
        BigDecimal cumulative = BigDecimal.ZERO;
        for (Month m : Month.values()) {
            BigDecimal inc = BigDecimal.TEN;
            if (m == Month.APRIL) inc = new BigDecimal("14.5");
            cumulative = cumulative.add(inc);
            list.add(new MeterReadingDTO(m, cumulative));
        }

        when(readings.findByMeter_IdAndYearAndMonth(anyString(), anyInt(), any()))
                .thenReturn(Optional.empty());

        // Capture saves so service can read back and validate
        List<MeterReading> saved = new ArrayList<>();
        when(readings.save(any(MeterReading.class))).thenAnswer(inv -> {
            MeterReading mr = inv.getArgument(0);
            saved.add(mr);
            return mr;
        });
        when(readings.findByMeter_IdAndYearOrderByMonthAsc("0001", 2024))
                .thenAnswer(inv -> saved.stream()
                        .sorted(Comparator.comparing(MeterReading::getMonth))
                        .toList());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.importYear("0001", 2024, list));
        assertTrue(ex.getMessage().toLowerCase().contains("april"));
    }

    @Test
    void importYear_rejects_decreasing_inside_payload() {
        // Make JAN cumulative = 10, FEB cumulative = 9 (decrease), then continue increasing.
        List<MeterReadingDTO> list = new ArrayList<>();
        Map<Month, BigDecimal> cumulativeByMonth = new LinkedHashMap<>();
        cumulativeByMonth.put(Month.JANUARY,  new BigDecimal("10"));
        cumulativeByMonth.put(Month.FEBRUARY, new BigDecimal("9"));   // decrease vs JAN → should fail
        cumulativeByMonth.put(Month.MARCH,    new BigDecimal("19"));
        cumulativeByMonth.put(Month.APRIL,    new BigDecimal("29"));
        cumulativeByMonth.put(Month.MAY,      new BigDecimal("39"));
        cumulativeByMonth.put(Month.JUNE,     new BigDecimal("49"));
        cumulativeByMonth.put(Month.JULY,     new BigDecimal("59"));
        cumulativeByMonth.put(Month.AUGUST,   new BigDecimal("69"));
        cumulativeByMonth.put(Month.SEPTEMBER,new BigDecimal("79"));
        cumulativeByMonth.put(Month.OCTOBER,  new BigDecimal("89"));
        cumulativeByMonth.put(Month.NOVEMBER, new BigDecimal("99"));
        cumulativeByMonth.put(Month.DECEMBER, new BigDecimal("109"));

        cumulativeByMonth.forEach((m, v) -> list.add(new MeterReadingDTO(m, v)));

        // Let the service look up "current saved reading for a month" dynamically from what's been saved so far.
        List<MeterReading> saved = new ArrayList<>();

        when(readings.save(any(MeterReading.class))).thenAnswer(inv -> {
            MeterReading mr = inv.getArgument(0);
            saved.add(mr);
            return mr;
        });

        // When the service queries for an existing reading for a specific month, return from 'saved'
        when(readings.findByMeter_IdAndYearAndMonth(anyString(), anyInt(), any()))
                .thenAnswer(inv -> {
                    String meterId = inv.getArgument(0);
                    int year = inv.getArgument(1);
                    Month month = inv.getArgument(2);
                    return saved.stream()
                            .filter(r -> r.getMeter().getId().equals(meterId)
                                    && r.getYear() == year
                                    && r.getMonth() == month)
                            .findFirst();
                });

        // Also support the later "read back the whole year" call, if your service does it:
        when(readings.findByMeter_IdAndYearOrderByMonthAsc("0001", 2024))
                .thenAnswer(inv -> saved.stream()
                        .sorted(Comparator.comparing(MeterReading::getMonth))
                        .toList());

        // Upstream setUp() already stubs meters.getOrThrow("0001") to return 'meter'

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.importYear("0001", 2024, list));

        // Message can vary; checking for "lower" or "decreas" and FEB/March is OK. Keep it loose:
        assertTrue(ex.getMessage().toLowerCase().contains("lower")
                || ex.getMessage().toLowerCase().contains("decreas"));
    }
}
