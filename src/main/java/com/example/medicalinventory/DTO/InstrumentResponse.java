package com.example.medicalinventory.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InstrumentResponse {
    private String name;
    private String barcode;
}
