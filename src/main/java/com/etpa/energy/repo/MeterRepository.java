package com.etpa.energy.repo;

import com.etpa.energy.domain.Meter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeterRepository extends JpaRepository<Meter, String> {

}
