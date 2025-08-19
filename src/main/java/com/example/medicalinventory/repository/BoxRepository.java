package com.example.medicalinventory.repository;

import com.example.medicalinventory.model.Box;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BoxRepository extends JpaRepository<Box, UUID> {
    Box findByBarcode(String barcode);
}
