package com.example.medicalinventory.controller;

import com.example.medicalinventory.DTO.BoxRequest;
import com.example.medicalinventory.DTO.BoxResponse;
import com.example.medicalinventory.model.Box;
import com.example.medicalinventory.service.BoxConverterService;
import com.example.medicalinventory.service.BoxService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boxes")
@RequiredArgsConstructor
public class BoxController {

    private final BoxService boxService;
    private final BoxConverterService boxConverterService;


    @PostMapping(value = "/create/pdf", consumes = "multipart/form-data")
    public ResponseEntity<ByteArrayResource> createBoxAndGetPdf(@ModelAttribute BoxRequest request) throws Exception {
        byte[] pdfBytes = boxService.createBoxAndGeneratePdf(request);

        ByteArrayResource resource = new ByteArrayResource(pdfBytes);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=box_" + System.currentTimeMillis() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(resource);
    }

    @PostMapping("/{boxBarcode}/add-instrument")
    public ResponseEntity<Box> addInstrumentToBox(
            @PathVariable String boxBarcode,
            @RequestParam String instrumentBarcode
    ) {
        Box updatedBox = boxService.addInstrumentToBox(boxBarcode, instrumentBarcode);
        return ResponseEntity.ok(updatedBox);
    }

    @PostMapping("/{boxBarcode}/assign-doctor")
    public ResponseEntity<Box> assignDoctorToBox(
            @PathVariable String boxBarcode,
            @RequestParam String doctorName
    ) {
        Box box = boxService.assignDoctor(boxBarcode, doctorName);
        return ResponseEntity.ok(box);
    }


}
