package com.example.medicalinventory.controller;

import com.example.medicalinventory.DTO.InstrumentRequest;
import com.example.medicalinventory.DTO.InstrumentResponse;
import com.example.medicalinventory.model.Instrument;
import com.example.medicalinventory.service.InstrumentConverterService;
import com.example.medicalinventory.service.InstrumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
@RequiredArgsConstructor
public class InstrumentController {

    private final InstrumentService instrumentService;
    private final InstrumentConverterService converterService;

    @PostMapping(value = "/batch", produces = "application/zip")
    public ResponseEntity<byte[]> createAndDownloadBarcodesZip(@ModelAttribute InstrumentRequest request) throws Exception {
        byte[] zip = instrumentService.createInstrumentsAndGenerateZipSvgs(request);

        String filename = "barcodes_" + java.time.LocalDate.now() + ".zip";

        return ResponseEntity.ok()
                .header("Content-Type", "application/zip")
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(zip);
    }

    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponseEntity<byte[]> createInstrument(@ModelAttribute InstrumentRequest request) throws Exception {
        byte[] zipBytes = instrumentService.createInstrumentsAndGenerateZipSvgs(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=instruments.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zipBytes);
    }


    @GetMapping("/{barcode}")
    public ResponseEntity<InstrumentResponse> getInstrumentResponseByBarcode(@PathVariable String barcode) {
        Instrument instrument = instrumentService.getByBarcode(barcode);
        if (instrument == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(converterService.convertToInstrumentResponse(instrument));
    }


    @PostMapping("/return")
    public ResponseEntity<String> returnInstruments(@RequestParam List<String> instrumentBarcodes) {
        instrumentService.returnInstruments(instrumentBarcodes);
        return ResponseEntity.ok("Instruments processed as returned/lost");
    }


    @GetMapping
    public ResponseEntity<Page<InstrumentResponse>> getAllInstruments(@RequestParam int page,
                                                                      @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Instrument> instruments = instrumentService.findAll(pageable);
        return ResponseEntity.ok(instruments.map(converterService::convertToInstrumentResponse));
    }
}
