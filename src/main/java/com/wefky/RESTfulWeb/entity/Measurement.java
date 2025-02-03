package com.wefky.RESTfulWeb.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    /**
     * Represents the unique identifier for the measurement entity.
     * This field is automatically generated using the IDENTITY strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "measurement_id")
    private Long measurementId;
    /**
     * Represents the unit of measurement for the measurement.
     * This field is mandatory and cannot be blank.
     * It is mapped to the "measurement_unit" column in the database.
     * 
     * @NotBlank ensures that the measurement unit is not null or empty.
     * @Column specifies the column details in the database.
     */
    @NotBlank(message = "Measurement Unit is required.")
    @Column(name = "measurement_unit", nullable = false, columnDefinition = "text")
    private String measurementUnit;
    /**
     * Represents the amount of the measurement.
     * This field is mandatory and must be a positive value.
     * It is mapped to the "amount" column in the database.
     * 
     * @NotNull ensures that the amount is not null.
     * @DecimalMin specifies that the amount must be greater than 0.
     * @Column specifies the column details in the database.
     */
    @NotNull(message = "Amount is required.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive.")
    @Column(name = "amount", nullable = false)
    private Double amount;
    /**
     * Represents the timestamp when the measurement was taken.
     * This field is mandatory and cannot be null.
     * It is mapped to the "timestamp" column in the database.
     * 
     * @NotNull ensures that the timestamp is not null.
     * @Column specifies the column details in the database.
     */
    @NotNull(message = "Timestamp is required.")
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    /**
     * Represents the location associated with the measurement.
     * This field is fetched eagerly.
     * It is mapped to the "location_id" column in the database.
     * 
     * @ManyToOne specifies the many-to-one relationship with the Location entity.
     * @JoinColumn specifies the mapping between the field and the database column.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", referencedColumnName = "location_id")
    private Location location;
    /**
     * Represents a flag indicating if the measurement has been deleted.
     * This field is mapped to the "deleted" column in the database and is not nullable.
     * Default value is false.
     * 
     * @Column specifies the column details in the database.
     */
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
