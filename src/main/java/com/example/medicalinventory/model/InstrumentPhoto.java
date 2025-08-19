package com.example.medicalinventory.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "instrument_photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrumentPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String url; // путь к файлу

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id")
    private Instrument instrument;
}
