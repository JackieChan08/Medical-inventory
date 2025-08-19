package com.example.medicalinventory.repository;

import com.example.medicalinventory.model.InstrumentPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InstrumentPhotoRepository extends JpaRepository<InstrumentPhoto, UUID> {
    List<InstrumentPhoto> findByInstrumentId(UUID instrumentId);
}
