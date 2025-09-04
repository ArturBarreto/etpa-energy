package com.etpa.energy.repo;

import com.etpa.energy.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.time.Month;
import java.util.*;


@Repository
public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {
    List<MeterReading> findByMeter_IdAndYearOrderByMonthAsc(String meterId, int year);
    Optional<MeterReading> findByMeter_IdAndYearAndMonth(String meterId, int year, Month month);
}
