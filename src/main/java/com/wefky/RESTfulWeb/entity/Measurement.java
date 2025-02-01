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
/**
 * Represents a Measurement entity that maps to the "measurements" table in the database.
 * This entity contains information about a specific measurement including its unit, amount, timestamp, 
 * associated location, and a flag indicating if it has been deleted.
 * 
 * Annotations:
 * - @Entity: Specifies that the class is an entity and is mapped to a database table.
 * - @Table: Specifies the name of the database table to be used for mapping.
 * - @Data: Lombok annotation to generate getters, setters, toString, equals, and hashCode methods.
 * - @AllArgsConstructor: Lombok annotation to generate a constructor with all fields.
 * - @NoArgsConstructor: Lombok annotation to generate a no-argument constructor.
 * 
 * Fields:
 * - measurementId: Unique identifier for the measurement, auto-generated.
 * - measurementUnit: Unit of the measurement, cannot be blank.
 * - amount: The amount of the measurement, must be a positive value.
 * - timestamp: The date and time when the measurement was taken, cannot be null.
 * - location: The location associated with the measurement, fetched eagerly.
 * - deleted: A flag indicating if the measurement has been marked as deleted, defaults to false.
 * 
 * Constraints:
 * - measurementUnit: Must not be blank.
 * - amount: Must be a positive value.
 * - timestamp: Must not be null.
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
    @Column(name = "measurement_unit", nullable = false, columnDefinition = "text")
    private String measurementUnit;

    @NotNull(message = "Amount is required.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive.")
    @Column(name = "amount", nullable = false)
    private Double amount;

    @NotNull(message = "Timestamp is required.")
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", referencedColumnName = "location_id")
    private Location location;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
