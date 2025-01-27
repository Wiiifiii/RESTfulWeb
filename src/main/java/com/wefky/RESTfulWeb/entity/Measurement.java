package com.wefky.RESTfulWeb.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "measurements")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "measurement_id")
    private Long measurementId;

    @NotBlank(message = "Measurement unit is required")
    @Column(name = "measurement_unit")
    private String measurementUnit;

    @Positive(message = "Amount must be positive")
    @Column(name = "amount")
    private double amount;

    @NotNull(message = "Timestamp is required")
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "location_id_fk", referencedColumnName = "location_id")
    private Location location;

    @Column(name = "deleted", nullable = false)
     private boolean deleted = false;
}
