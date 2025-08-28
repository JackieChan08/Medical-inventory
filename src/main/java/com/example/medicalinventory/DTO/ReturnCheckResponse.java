package com.example.medicalinventory.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReturnCheckResponse {
    private List<String> foundBarcodes;
    private List<String> notFoundBarcodes;
}
