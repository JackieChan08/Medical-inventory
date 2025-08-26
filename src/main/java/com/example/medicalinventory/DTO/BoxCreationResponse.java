package com.example.medicalinventory.DTO;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BoxCreationResponse {
    private boolean success;
    private byte[] pdf; // если success == true
    private List<String> barcodes; // список добавленных баркодов
    private List<String> notFoundBarcodes; // если success == false
}
