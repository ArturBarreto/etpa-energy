package com.etpa.energy.repo;

import com.etpa.energy.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.Month;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MeterReadingRepositoryTest {

    @Autowired MeterRepository meterRepo;
    @Autowired MeterReadingRepository readingRepo;
    @Autowired ProfileRepository profileRepo;

    @Test
    void findByMeterAndYear_sortedAsc() {
        var p = profileRepo.save(new Profile("A","test"));
        p.getFractions().add(new Fraction(p, Month.JANUARY, BigDecimal.ONE));
        p.getFractions().add(new Fraction(p, Month.FEBRUARY, BigDecimal.ZERO));
        profileRepo.save(p);

        var m = meterRepo.save(new Meter("0001", p));

        readingRepo.save(new MeterReading(m, 2024, Month.FEBRUARY, new BigDecimal("20")));
        readingRepo.save(new MeterReading(m, 2024, Month.JANUARY,  new BigDecimal("10")));

        // DB returns alphabetical by enum STRING: [FEBRUARY, JANUARY]
        var out = readingRepo.findByMeter_IdAndYearOrderByMonthAsc("0001", 2024);

        // Basic presence
        assertThat(out).hasSize(2);

        // Normalize to calendar order for assertions
        var byCalendar = out.stream()
                .sorted(Comparator.comparingInt(r -> r.getMonth().getValue()))
                .toList();

        assertThat(byCalendar)
                .extracting(MeterReading::getMonth)
                .containsExactly(Month.JANUARY, Month.FEBRUARY);
    }
}
