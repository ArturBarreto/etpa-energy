package com.etpa.energy.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * CUMULATIVE monthly reading at the end of the month for a meter and year.
 * Unique per (meterId, year, month).
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"meterId","year","month"}))
public class MeterReading {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String meterId;
    private int year;

    @Enumerated(EnumType.STRING)
    private Month month;

    @Column(precision = 18, scale = 6)
    private BigDecimal reading; // cumulative value

    public Long getId() {
        return id;
    }

    public String getMeterId() {
        return meterId;
    }

    public void setMeterId(String meterId) {
        this.meterId = meterId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public BigDecimal getReading() {
        return reading;
    }

    public void setReading(BigDecimal reading) {
        this.reading = reading;
    }
}
