package com.etpa.energy.service;

import com.etpa.energy.domain.Meter;
import com.etpa.energy.domain.Profile;
import com.etpa.energy.dto.MeterDTO;
import com.etpa.energy.repo.MeterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeterService {
    private final MeterRepository meters;
    private final ProfileService profiles;

    public MeterService(MeterRepository meters, ProfileService profiles) {
        this.meters = meters; this.profiles = profiles;
    }

    @Transactional
    public Meter upsert(MeterDTO dto) {
        Profile p = profiles.getOrThrow(dto.profileCode());
        Meter m = meters.findById(dto.id()).orElse(new Meter());
        m.setId(dto.id());
        m.setProfile(p);
        return meters.save(m);
    }

    public Meter getOrThrow(String id) {
        return meters.findById(id).orElseThrow(() -> new IllegalArgumentException("Meter not found: " + id));
    }
}
