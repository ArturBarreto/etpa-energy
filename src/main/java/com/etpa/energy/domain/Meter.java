package com.etpa.energy.domain;


import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


@Entity
@Table(name = "meters")
public class Meter {
    @Id
    @Column(length = 64)
    private String id; // e.g., "0001"


    @ManyToOne(optional = false)
    @JoinColumn(name = "profile_code")
    private Profile profile; // profile applied to this meter


    public Meter() {}
    public Meter(String id, Profile profile) {
        this.id = id; this.profile = profile;
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Profile getProfile() { return profile; }
    public void setProfile(Profile profile) { this.profile = profile; }
}
