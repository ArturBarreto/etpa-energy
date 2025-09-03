package com.etpa.energy.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Meter identified by meterId and linked to a Profile by profileCode.
 */
@Entity
public class Meter {
    @Id
    private String meterId;      // unique
    private String profileCode;  // references Profile.profileCode

    public Meter() {

    }
    public Meter(String meterId, String profileCode) {
        this.meterId = meterId;
        this.profileCode = profileCode;
    }

    public String getMeterId() {
        return meterId;
    }

    public void setMeterId(String meterId) {
        this.meterId = meterId;
    }

    public String getProfileCode() {
        return profileCode;
    }

    public void setProfileCode(String profileCode) {
        this.profileCode = profileCode;
    }
}
