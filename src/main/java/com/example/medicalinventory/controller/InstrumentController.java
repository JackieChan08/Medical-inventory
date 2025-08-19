package com.example.medicalinventory.controller;

import com.example.medicalinventory.DTO.InstrumentRequest;
import com.example.medicalinventory.service.InstrumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instruments")
@RequiredArgsConstructor
public class InstrumentController {

    private final InstrumentService instrumentService;


    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponseEntity<byte[]> createInstrument(@ModelAttribute InstrumentRequest request) throws Exception {
        byte[] pdfBytes = instrumentService.createInstrumentsAndGeneratePdf(request); // метод без фото
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=instruments.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }



}

