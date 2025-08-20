package com.example.medicalinventory.service;

import com.example.medicalinventory.DTO.BoxRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
                .status(BoxStatus.CERTIFIED)
                .boxImages(new ArrayList<>())
                .instruments(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .returnBy(request.getReturnDate())
                .build();

        Box savedBox = boxRepository.save(box);

        // сохраняем фото (если есть)
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (MultipartFile file : request.getImages()) {
                FileEntity fileEntity = fileUploadService.saveImage(file);
                BoxImage boxImage = BoxImage.builder()
                        .box(savedBox)
                        .image(fileEntity)
                        .build();
                boxImageRepository.save(boxImage);
                savedBox.getBoxImages().add(boxImage);
            }
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
        Box box = boxRepository.findByBarcode(boxBarcode);
        if (box == null) throw new RuntimeException("Box not found: " + boxBarcode);

        Instrument instrument = instrumentRepository.findByBarcode(instrumentBarcode);
        if (instrument == null) throw new RuntimeException("Instrument not found: " + instrumentBarcode);

        instrument.setStatus(InstrumentStatus.IN_BOX);
        instrumentRepository.save(instrument);

        box.getInstruments().add(instrument);
        Box savedBox = boxRepository.save(box);



        return savedBox;
    }

    @Transactional
    public Box updateBox(String boxBarcode, BoxRequest request, BoxStatus boxStatus) throws Exception {
        Box box = boxRepository.findByBarcode(boxBarcode);
        if (box == null) throw new RuntimeException("Box not found: " + boxBarcode);

        if (boxStatus != null) box.setStatus(boxStatus);
        box.setUpdatedAt(LocalDateTime.now());

        if (box.getBoxImages() == null) box.setBoxImages(new ArrayList<>());

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            boxImageRepository.deleteAll(box.getBoxImages());
            box.getBoxImages().clear();

            for (MultipartFile file : request.getImages()) {
                FileEntity fileEntity = fileUploadService.saveImage(file);
                BoxImage boxImage = BoxImage.builder()
                        .box(box)
                        .image(fileEntity)
                        .build();
                boxImageRepository.save(boxImage);
                box.getBoxImages().add(boxImage);
            }
        }

        return boxRepository.save(box);
    }

    @Transactional
    public Box assignDoctor(String boxBarcode, String doctorName) {
        Box box = boxRepository.findByBarcode(boxBarcode);
        if (box == null) throw new RuntimeException("Box not found: " + boxBarcode);

        box.setDoctorName(doctorName);
        box.setUpdatedAt(LocalDateTime.now());
        Box savedBox = boxRepository.save(box);

        // записываем историю для всех инструментов в боксе, что доктор теперь ответственен
        if (savedBox.getInstruments() != null) {
            for (Instrument instrument : savedBox.getInstruments()) {
                instrumentBoxHistoryService.logOperation(savedBox, instrument, HistoryOperation.ADDED, doctorName);
            }
        }

        return savedBox;
    }

}
