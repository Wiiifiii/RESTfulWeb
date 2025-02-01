package com.wefky.RESTfulWeb.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Represents an Image or file in the database.
 */
/**
 * Represents an image entity stored in the database.
 * This entity contains metadata about the image as well as the image data itself.
 * 
 * Annotations:
 * - @Entity: Specifies that this class is an entity and is mapped to a database table.
 * - @Table: Specifies the name of the database table to be used for mapping.
 * - @Data: Lombok annotation to generate getters, setters, toString, equals, and hashCode methods.
 * - @AllArgsConstructor: Lombok annotation to generate a constructor with all fields.
 * - @NoArgsConstructor: Lombok annotation to generate a no-argument constructor.
 * - @Builder: Lombok annotation to implement the builder pattern.
 */
@Entity
@Table(name = "images")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Image {

    /**
     * Unique identifier for the image.
     * 
     * Annotations:
     * - @Id: Specifies the primary key of an entity.
     * - @GeneratedValue: Provides the specification of generation strategies for the values of primary keys.
     * - @Column: Specifies the mapped column for a persistent property or field.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    /**
     * Owner of the image.
     * 
     * Annotations:
     * - @NotBlank: Ensures that the field is not null and the trimmed length is greater than zero.
     * - @Column: Specifies the mapped column for a persistent property or field.
     */
    @NotBlank(message = "Owner is required.")
    @Column(name = "owner", length = 100)
    private String owner;

    /**
     * Binary data of the image.
     * 
     * Annotations:
     * - @Lob: Specifies that the field should be persisted as a large object.
     * - @Basic: Specifies the fetch strategy for the field.
     * - @Column: Specifies the mapped column for a persistent property or field.
     */
    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "data", nullable = false)
    private byte[] data;

    /**
     * Flag indicating whether the image is deleted.
     * 
     * Annotations:
     * - @Column: Specifies the mapped column for a persistent property or field.
     */
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    /**
     * Content type of the image (e.g., image/jpeg).
     * 
     * Annotations:
     * - @NotBlank: Ensures that the field is not null and the trimmed length is greater than zero.
     * - @Column: Specifies the mapped column for a persistent property or field.
     */
    @NotBlank(message = "Content Type is required.")
    @Column(name = "content_type", length = 50)
    private String contentType;

    /**
     * Title of the image.
     * 
     * Annotations:
     * - @Column: Specifies the mapped column for a persistent property or field.
     */
    @Column(name = "title", length = 255)
    private String title;

    /**
     * Description of the image.
     * 
     * Annotations:
     * - @Column: Specifies the mapped column for a persistent property or field.
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Date and time when the image was uploaded.
     * 
     * Annotations:
     * - @Column: Specifies the mapped column for a persistent property or field.
     */
    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    /**
     * Base64-encoded representation of the image data.
     * This field is transient and not stored in the database.
     * It is used by Thymeleaf for displaying the image.
     * 
     * Annotations:
     * - @Transient: Specifies that the field is not persistent.
     */
    @Transient
    private String base64Data;
}