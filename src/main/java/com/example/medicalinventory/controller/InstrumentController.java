package com.example.medicalinventory.controller;

import com.example.medicalinventory.DTO.InstrumentRequest;
import com.example.medicalinventory.DTO.InstrumentResponse;
import com.example.medicalinventory.model.Instrument;
import com.example.medicalinventory.service.InstrumentConverterService;
import com.example.medicalinventory.service.InstrumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/instruments")
@RequiredArgsConstructor
public class InstrumentController {

    private final InstrumentService instrumentService;
    private final InstrumentConverterService converterService;


    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponseEntity<byte[]> createInstrument(@ModelAttribute InstrumentRequest request) throws Exception {
        byte[] pdfBytes = instrumentService.createInstrumentsAndGeneratePdf(request); // метод без фото
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=instruments.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstrumentResponse> getInstrumentResponse(@PathVariable UUID id) throws Exception {
        Instrument instrument = instrumentService.getById(id);
        if (instrument == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        InstrumentResponse instrumentResponse = converterService.convertToInstrumentResponse(instrument);

        return ResponseEntity.ok(instrumentResponse);
    }



}

