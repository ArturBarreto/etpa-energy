package com.etpa.energy.service;

import com.etpa.energy.domain.Meter;
import com.etpa.energy.domain.Profile;
import com.etpa.energy.dto.MeterDTO;
import com.etpa.energy.repo.MeterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeterServiceTest {

    @Mock MeterRepository repo;
    @Mock ProfileService profiles;

    @Test
    void getOrThrow_404() {
        when(repo.findById("X")).thenReturn(Optional.empty());
        var svc = new MeterService(repo, profiles);
        assertThrows(NoSuchElementException.class, () -> svc.getOrThrow("X"));
    }

    @Test
    void upsert_sets_profile_and_id() {
        var p = new Profile("A", "desc");
        when(profiles.getOrThrow("A")).thenReturn(p);
        when(repo.findById("0001")).thenReturn(Optional.empty());
        when(repo.save(org.mockito.ArgumentMatchers.any(Meter.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var dto = new MeterDTO("0001", "A");
        var svc = new MeterService(repo, profiles);
        var saved = svc.upsert(dto);

        assertEquals("0001", saved.getId());
        assertEquals("A", saved.getProfile().getCode());
    }
}
