package com.etpa.energy.service;

import com.etpa.energy.domain.*;
import com.etpa.energy.dto.*;
import com.etpa.energy.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


@Service
public class ProfileService {
    private final ProfileRepository profiles;

    public ProfileService(ProfileRepository profiles) { this.profiles = profiles; }

    @Transactional
    public Profile upsert(ProfileDTO dto) {
        Profile p = profiles.findById(dto.code()).orElse(new Profile(dto.code(), dto.description()));
        p.setDescription(dto.description());
        // replace fractions
        p.getFractions().clear();
        for (FractionDTO f : dto.fractions()) {
            Fraction fr = new Fraction(p, f.month(), f.value());
            p.getFractions().add(fr);
        }
        validateFractionsSumToOne(p.getFractions());
        // ensure all 12 months present
        if (p.getFractions().size() != 12) throw new IllegalArgumentException("Profile must contain 12 monthly fractions");
        // ensure uniqueness per month inside list
        if (new HashSet<>(p.getFractions().stream().map(Fraction::getMonth).toList()).size() != 12) {
            throw new IllegalArgumentException("Duplicate months in fractions");
        }
        return profiles.save(p);
    }

    public Profile getOrThrow(String code){
        return profiles.findById(code).orElseThrow(() -> new NoSuchElementException("Profile not found: " + code));
    }

    private void validateFractionsSumToOne(List<Fraction> fractions) {
        BigDecimal sum = fractions.stream().map(Fraction::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.setScale(6, RoundingMode.HALF_UP).compareTo(BigDecimal.ONE.setScale(6, RoundingMode.HALF_UP)) != 0) {
            throw new IllegalArgumentException("Fractions must sum to 1.0 (was " + sum + ")");
        }
        // bounds are already enforced by @DecimalMin/Max
    }
}
