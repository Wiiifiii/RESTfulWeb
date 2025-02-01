package com.wefky.RESTfulWeb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long locationId;

    @NotBlank(message = "Postal Code is required.")
    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @NotBlank(message = "City Name is required.")
    @Column(name = "city_name", nullable = false)
    private String cityName;

    @NotNull(message = "Latitude is required.")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90.")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90.")
    @Column(name = "latitude", nullable = false)
    private Float latitude;

    @NotNull(message = "Longitude is required.")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180.")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180.")
    @Column(name = "longitude", nullable = false)
    private Float longitude;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
