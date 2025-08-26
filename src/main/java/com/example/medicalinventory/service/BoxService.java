package com.example.medicalinventory.service;

import com.example.medicalinventory.DTO.BoxRequest;
import com.example.medicalinventory.DTO.ReturnRequest;
import com.example.medicalinventory.model.*;
import com.example.medicalinventory.repository.BoxImageRepository;
import com.example.medicalinventory.repository.BoxRepository;
import com.example.medicalinventory.repository.InstrumentRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code39Writer;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoxService {

    private final BoxRepository boxRepository;
    private final BoxImageRepository boxImageRepository;
    private final FileUploadService fileUploadService;
    private final InstrumentRepository instrumentRepository;
    private final InstrumentBoxHistoryService instrumentBoxHistoryService;
    @Transactional
    public byte[] createBoxAndGeneratePdf(BoxRequest request) throws Exception {
        Box box = Box.builder()
                .barcode(generateBarcode())
                .doctorName(request.getDoctorName())
                .status(BoxStatus.CREATED)
                .instruments(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .returnBy(request.getReturnDate())
                .build();

        Box savedBox = boxRepository.save(box);

        if (request.getInstrumentBarcodes() != null && !request.getInstrumentBarcodes().isEmpty()) {
            for (String instrumentBarcode : request.getInstrumentBarcodes()) {
                Instrument instrument = instrumentRepository.findByBarcode(instrumentBarcode)
                        .orElseThrow(() -> new RuntimeException("Instrument not found: " + instrumentBarcode));

                instrument.setStatus(InstrumentStatus.IN_BOX);
                instrumentRepository.save(instrument);

                savedBox.getInstruments().add(instrument);

                instrumentBoxHistoryService.logOperation(savedBox, instrument, HistoryOperation.ISSUED);
            }
            boxRepository.save(savedBox);
        }

        return generatePdfWithBarcode(savedBox);
    }


    private String generateBarcode() {
        return "BOX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private byte[] generatePdfWithBarcode(Box box) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
        Document document = new Document(pdf);

        PdfFont font = PdfFontFactory.createFont("fonts/FreeSans.ttf", PdfEncodings.IDENTITY_H);
        document.setFont(font);

        document.add(new Paragraph("Бокс: " + box.getBarcode()));
        if (box.getDoctorName() != null) {
            document.add(new Paragraph("Доктор: " + box.getDoctorName()));
        }
        document.add(new Paragraph("Статус: " + box.getStatus()));

        // штрих-код
        Image barcodeImage = new Image(generateBarcodeImage(box.getBarcode()));
        document.add(barcodeImage);

        document.close();
        return baos.toByteArray();
    }

    private ImageData generateBarcodeImage(String code) throws WriterException {
        Code39Writer writer = new Code39Writer();
        BitMatrix bitMatrix = writer.encode(code, BarcodeFormat.CODE_39, 200, 50);
        try {
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            return ImageDataFactory.create(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при генерации изображения штрих-кода", e);
        }
    }

    @Transactional
    public Box addInstrumentToBox(String boxBarcode, String instrumentBarcode) {
        Box box = boxRepository.findByBarcode(boxBarcode).orElseThrow(() -> new RuntimeException("Box not found"));
        if (box == null) throw new RuntimeException("Box not found: " + boxBarcode);

        Instrument instrument = instrumentRepository.findByBarcode(instrumentBarcode).orElseThrow(() -> new RuntimeException("Instrument not found"));
        if (instrument == null) throw new RuntimeException("Instrument not found: " + instrumentBarcode);

        instrument.setStatus(InstrumentStatus.IN_BOX);
        instrumentRepository.save(instrument);

        box.getInstruments().add(instrument);
        Box savedBox = boxRepository.save(box);
        instrumentBoxHistoryService.logOperation(savedBox, instrument, HistoryOperation.ISSUED);


        return savedBox;
    }

    @Transactional
    public Box updateBox(String boxBarcode, BoxRequest request, BoxStatus boxStatus) throws Exception {
        Box box = boxRepository.findByBarcode(boxBarcode).orElseThrow(() -> new RuntimeException("Box not found"));
        if (box == null) throw new RuntimeException("Box not found: " + boxBarcode);

        if (boxStatus != null) box.setStatus(boxStatus);
        box.setUpdatedAt(LocalDateTime.now());


        return boxRepository.save(box);
    }



    @Transactional
    public void returnBox(ReturnRequest request) {
        Box box = boxRepository.findByBarcode(request.getBoxBarcode())
                .orElseThrow(() -> new RuntimeException("Box not found"));

        for (String instrumentBarcode : request.getInstrumentBarcodes()) {
            instrumentRepository.findByBarcode(instrumentBarcode)
                    .ifPresentOrElse(instrument -> {
                        instrument.setStatus(InstrumentStatus.ACTIVE);
                        instrumentRepository.save(instrument);
                        instrumentBoxHistoryService.logOperation(box, instrument, HistoryOperation.RETURNED);
                    }, () -> {
                        // если инструмент не найден в системе
                        instrumentBoxHistoryService.logOperation(box, null, HistoryOperation.LOST);
                    });
        }

        box.setStatus(BoxStatus.RETURNED);
        boxRepository.save(box);
        instrumentBoxHistoryService.logOperation(box, null, HistoryOperation.RETURNED);
    }
    @Transactional
    public Page<Box> getBoxesByStatus(BoxStatus status, Pageable pageable) {
        return boxRepository.findBoxByStatus(status, pageable);
    }


}
