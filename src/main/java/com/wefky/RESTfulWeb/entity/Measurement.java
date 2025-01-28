package com.wefky.RESTfulWeb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a Measurement.
 */
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

    @NotBlank(message = "Measurement Unit is required.")
    @Column(name = "measurement_unit", nullable = false)
    private String measurementUnit;

    @NotNull(message = "Amount is required.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive.")
    @Column(name = "amount", nullable = false)
    private Double amount;

    @NotNull(message = "Timestamp is required.")
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id_fk")
    private Location location;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
