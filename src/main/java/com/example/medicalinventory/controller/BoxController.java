package com.example.medicalinventory.controller;

import com.example.medicalinventory.DTO.BoxRequest;
import com.example.medicalinventory.DTO.BoxResponse;
import com.example.medicalinventory.DTO.ReturnRequest;
import com.example.medicalinventory.model.Box;
import com.example.medicalinventory.model.BoxStatus;
import com.example.medicalinventory.service.BoxConverterService;
import com.example.medicalinventory.service.BoxService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boxes")
@RequiredArgsConstructor
public class BoxController {

    private final BoxService boxService;
    private final BoxConverterService boxConverterService;


    @PostMapping(value = "/create/pdf")
    public ResponseEntity<ByteArrayResource> createBoxAndGetPdf(@ModelAttribute BoxRequest request) throws Exception {
        byte[] pdfBytes = boxService.createBoxAndGeneratePdf(request);

        ByteArrayResource resource = new ByteArrayResource(pdfBytes);
        return ResponseEntity.ok()
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


    @PostMapping("/return")
    public ResponseEntity<String> returnBox(@RequestBody ReturnRequest request) {
        boxService.returnBox(request);
        return ResponseEntity.ok("Box and instruments processed as returned/lost");
    }

    @GetMapping("/get-by-status")
    public ResponseEntity<Page<BoxResponse>> getBoxByStatus(@RequestParam BoxStatus status,
                                                            @RequestParam int page,
                                                            @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Box> boxes = boxService.getBoxesByStatus(status, pageable);
        return ResponseEntity.ok(boxes.map(boxConverterService::convertToBoxResponse));
    }


}
