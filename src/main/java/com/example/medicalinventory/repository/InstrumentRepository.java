package com.example.medicalinventory.repository;

import com.example.medicalinventory.model.Instrument;
import com.example.medicalinventory.model.InstrumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InstrumentRepository extends JpaRepository<Instrument, UUID> {
    List<Instrument> findByStatus(InstrumentStatus status);
}
