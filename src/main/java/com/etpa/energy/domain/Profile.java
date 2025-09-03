package com.etpa.energy.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.*;


@Entity
@Table(name = "profiles")
public class Profile {
    @Id
    @Column(length = 32)
    private String code; // e.g., "A", "B"


    @NotBlank
    private String description;


    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("month ASC")
    private List<Fraction> fractions = new ArrayList<>();


    public Profile() {}
    public Profile(String code, String description) {
        this.code = code; this.description = description;
    }


    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Fraction> getFractions() { return fractions; }
}
