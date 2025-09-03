package com.etpa.energy.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Month;


@Entity
@Table(name = "meter_readings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"meter_id", "reading_year", "month_name"}))
public class MeterReading {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(optional = false)
    @JoinColumn(name = "meter_id")
    private Meter meter;


    @Column(name = "reading_year", nullable = false)
    private int year;


    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(name = "month_name", nullable = false, length = 9)
    private Month month;


    @DecimalMin("0.0")
    @Digits(integer=18, fraction=3)
    @Column(nullable = false, precision = 21, scale = 3)
    private BigDecimal reading; // counter value (non-decreasing within a year)


    public MeterReading() {}
    public MeterReading(Meter meter, int year, Month month, BigDecimal reading) {
        this.meter = meter; this.year = year; this.month = month; this.reading = reading;
    }


    public Long getId() { return id; }
    public Meter getMeter() { return meter; }
    public void setMeter(Meter meter) { this.meter = meter; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public Month getMonth() { return month; }
    public void setMonth(Month month) { this.month = month; }
    public BigDecimal getReading() { return reading; }
    public void setReading(BigDecimal reading) { this.reading = reading; }
}
