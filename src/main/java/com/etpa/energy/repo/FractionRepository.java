package com.etpa.energy.repo;

import com.etpa.energy.domain.Fraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FractionRepository extends JpaRepository<Fraction, Long> {
    List<Fraction> findByProfile_CodeOrderByMonthAsc(String code);
}
