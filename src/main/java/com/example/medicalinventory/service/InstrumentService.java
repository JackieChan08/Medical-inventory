package com.example.medicalinventory.service;

import com.example.medicalinventory.DTO.InstrumentRequest;
import com.example.medicalinventory.model.*;
import com.example.medicalinventory.repository.InstrumentBoxHistoryRepository;
import com.example.medicalinventory.repository.InstrumentImageRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;
@Service
@RequiredArgsConstructor
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;
    private final InstrumentImageRepository instrumentImageRepository; // добавь
    private final FileUploadService fileUploadService; // добавь
    private final InstrumentBoxHistoryRepository historyRepository;

    @Transactional
    public byte[] createInstrumentsAndGeneratePdf(InstrumentRequest request) throws Exception {
        List<Instrument> allInstruments = new ArrayList<>();

        for (int i = 0; i < request.getQuantity(); i++) {
            Instrument instrument = Instrument.builder()
                    .name(request.getName())
                    .barcode(generateBarcode())
                    .serialNumber(generateSerialNumber())
                    .productionDate(request.getProductionDate())
                    .acceptanceDate(request.getAcceptanceDate())
                    .productionCompany(request.getProductionCompany())
                    .country(request.getCountry())
                    .composition(request.getComposition())
                    .reusable(request.getReusable())
                    .usageCount(request.getUsageCount() != null ? request.getUsageCount() : 0)
                    .status(InstrumentStatus.ACTIVE)
                    .build();

            Instrument savedInstrument = instrumentRepository.save(instrument);

            // сохраняем фото (если есть)
            if (request.getImages() != null && !request.getImages().isEmpty()) {
                for (MultipartFile image : request.getImages()) {
                    FileEntity fileEntity = fileUploadService.saveImage(image);
                    InstrumentImage instrumentImage = InstrumentImage.builder()
                            .instrument(savedInstrument)
                            .image(fileEntity)
                            .build();
                    instrumentImageRepository.save(instrumentImage);
                }
            }

            allInstruments.add(savedInstrument);
        }

        return generatePdfWithBarcodes(allInstruments);
    }

    private String generateBarcode() {
        return "INSTR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateSerialNumber() {
        return "SN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private byte[] generatePdfWithBarcodes(List<Instrument> instruments) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
        Document document = new Document(pdf);

        for (Instrument instrument : instruments) {
            PdfFont font = PdfFontFactory.createFont("fonts/FreeSans.ttf", PdfEncodings.IDENTITY_H);
            document.setFont(font);
            document.add(new Paragraph("Инструмент: " + instrument.getName()));
            document.add(new Paragraph("Serial: " + instrument.getSerialNumber()));
            document.add(new Paragraph("Barcode: " + instrument.getBarcode()));

            // штрих-код
            Image barcodeImage = new Image(generateBarcodeImage(instrument.getBarcode()));
            document.add(barcodeImage);

            document.add(new Paragraph("\n"));
        }

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

    public Instrument getById(UUID id) {
        return instrumentRepository.findById(id).orElse(null);
    }
    public Page<Instrument> search(String value, Pageable pageable) {
        return instrumentRepository.search(value, pageable);
    }


    @Transactional
    public Instrument assignInstrumentToDoctor(UUID instrumentId, String doctorName) {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new RuntimeException("Instrument not found: " + instrumentId));

        instrument.setStatus(InstrumentStatus.BROKEN);

        instrumentRepository.save(instrument);

        InstrumentBoxHistory history = InstrumentBoxHistory.builder()
                .instrument(instrument)
                .box(null) // нет бокса
                .doctorName(doctorName)
                .operation(HistoryOperation.ADDED)
                .timestamp(java.time.LocalDateTime.now())
                .build();

        historyRepository.save(history);

        return instrument;
    }

}
