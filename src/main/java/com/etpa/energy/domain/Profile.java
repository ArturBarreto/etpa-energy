package com.etpa.energy.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

/**
 * Profile (e.g., A, B) has 12 monthly fractions that MUST sum to 1.0.
 * They model how a year's total consumption is distributed over months.
 */
@Entity
public class Profile {
    @Id
    private String profileCode; // "A", "B", ...

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_fraction", joinColumns = @JoinColumn(name = "profile_code"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "month")
    @Column(name = "fraction", nullable = false, precision = 10, scale = 6)
    private Map<Month, BigDecimal> fractions = new EnumMap<>(Month.class);

    public String getProfileCode() {
        return profileCode;
    }

    public void setProfileCode(String profileCode) {
        this.profileCode = profileCode;
    }

    public Map<Month, BigDecimal> getFractions() {
        return fractions;
    }

    public void setFractions(Map<Month, BigDecimal> fractions) {
        this.fractions = fractions;
    }
}
