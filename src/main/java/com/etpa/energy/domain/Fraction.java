package com.etpa.energy.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Month;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


@Entity
@Table(name = "fractions", uniqueConstraints = @UniqueConstraint(columnNames = {"profile_code", "month_name"}))
public class Fraction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(optional = false)
    @JoinColumn(name = "profile_code")
    private Profile profile;


    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(name = "month_name", nullable = false, length = 9)
    private Month month;


    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Digits(integer = 1, fraction = 6)
    @Column(name = "fraction_value", nullable = false, precision = 8, scale = 6)
    private BigDecimal value; // ratio in [0,1]


    public Fraction() {}
    public Fraction(Profile p, Month m, BigDecimal v){ this.profile=p; this.month=m; this.value=v; }


    public Long getId() { return id; }
    public Profile getProfile() { return profile; }
    public void setProfile(Profile profile) { this.profile = profile; }
    public Month getMonth() { return month; }
    public void setMonth(Month month) { this.month = month; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
}
