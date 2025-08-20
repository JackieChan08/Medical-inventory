package com.example.medicalinventory.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
public class BoxRequest {
    private List<MultipartFile> images;
    private String name;
    private LocalDate returnDate;
}

