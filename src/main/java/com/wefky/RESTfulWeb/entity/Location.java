package com.wefky.RESTfulWeb.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a Location.
 */
/**
 * Represents a geographical location with postal code, city name, latitude, and longitude.
 * This entity is mapped to the "locations" table in the database.
 * 
 * Annotations used:
 * - @Entity: Specifies that the class is an entity and is mapped to a database table.
 * - @Table: Specifies the name of the database table to be used for mapping.
 * - @Data: Lombok annotation to generate getters, setters, toString, equals, and hashCode methods.
 * - @AllArgsConstructor: Lombok annotation to generate a constructor with all fields.
 * - @NoArgsConstructor: Lombok annotation to generate a no-argument constructor.
 * 
 * Fields:
 * - locationId: Unique identifier for the location, auto-generated.
 * - postalCode: Postal code of the location, cannot be blank.
 * - cityName: Name of the city, cannot be blank.
 * - latitude: Latitude of the location, must be between -90 and 90, cannot be null.
 * - longitude: Longitude of the location, must be between -180 and 180, cannot be null.
 * - deleted: Flag indicating whether the location is deleted, defaults to false.
 */
@Entity
@Table(name = "locations")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {

    /**
     * Represents the unique identifier for the location entity.
     * This field is automatically generated using the IDENTITY strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long locationId;

    /**
     * Represents the postal code of a location.
     * This field is mandatory and cannot be blank.
     * It is mapped to the "postal_code" column in the database.
     * 
     * @NotBlank ensures that the postal code is not null or empty.
     * @Column specifies the column details in the database.
     */
    @NotBlank(message = "Postal Code is required.")
    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    /**
     * Represents the name of the city.
     * This field is mandatory and cannot be blank.
     * It is mapped to the "city_name" column in the database.
     * 
     * @NotBlank ensures that the city name is not null or empty.
     * @Column specifies the column details in the database.
     */
    @NotBlank(message = "City Name is required.")
    @Column(name = "city_name", nullable = false)
    private String cityName;

    /**
     * Represents the latitude of a location.
     * The latitude must be a non-null value between -90.0 and 90.0.
     * @NotNull Ensures that the latitude is not null.
     * @DecimalMin Ensures that the latitude is not less than -90.0.
     * @DecimalMax Ensures that the latitude is not greater than 90.0.
     * @Column Specifies the column mapping for the latitude field in the database.
     */
    @NotNull(message = "Latitude is required.")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90.")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90.")
    @Column(name = "latitude", nullable = false)
    private Float latitude;

   
    /**
     * Represents the longitudinal coordinate for a location.
     * This field must satisfy the following constraints:
     * It cannot be null.
     * The value must be no less than -180.0.
     * The value must be no greater than 180.0.
     */
    @NotNull(message = "Longitude is required.")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180.")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180.")
    @Column(name = "longitude", nullable = false)
    private Float longitude;

    /**
     * Represents the deleted status of the location.
     * This field is mapped to the "deleted" column in the database.
     * The default value is false.
     */
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
