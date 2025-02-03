package com.wefky.RESTfulWeb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.*;

/**
 * Represents a file entity that can be an image, PDF, or Word document.
 */
@Entity
@Table(name = "images")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Image {

    /**
     * Represents the unique identifier for the image entity.
     * This field is automatically generated using the identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    /**
     * Represents the owner of the image.
     * This field is mandatory and cannot be blank.
     * The owner's name is stored in the database column "owner" with a maximum length of 100 characters.
     * 
     * @NotBlank ensures that the owner field is not null or empty.
     * @Column specifies the mapping between the field and the database column.
     * 
     * @NotBlank(message = "Owner is required.")
     * @Column(name = "owner", length = 100, nullable = false)
     */
    @NotBlank(message = "Owner is required.")
    @Column(name = "owner", length = 100, nullable = false)
    private String owner;

    /**
     * Represents the image data stored as a byte array.
     * 
     * <p>This field is annotated with {@code @Lob} to indicate that it should be 
     * treated as a large object. The {@code @Basic(fetch = FetchType.EAGER)} 
     * annotation specifies that the data should be fetched eagerly, meaning it 
     * will be loaded immediately along with its owning entity. The {@code @Column} 
     * annotation is used to map this field to the "data" column in the database, 
     * and it is marked as non-nullable.
     */
    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "data", nullable = false)
    private byte[] data;

    /**
     * Indicates whether the image is deleted.
     * This field is mapped to the "deleted" column in the database and is not nullable.
     * Default value is false.
     */
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    /**
     * The content type of the image.
     * This field is required and cannot be blank.
     * It is stored in the database column "content_type" with a maximum length of 100 characters.
     * 
     * @NotBlank ensures that the content type is not null or empty.
     * @Column specifies the mapping between the field and the database column.
     */
    @NotBlank(message = "Content Type is required.")
    @Column(name = "content_type", length = 100, nullable = false)
    private String contentType;

    /**
     * The title of the image.
     * This field is mapped to the "title" column in the database with a maximum length of 255 characters.
     */
    @Column(name = "title", length = 255)
    private String title;

    /**
     * The description of the image.
     * This field is mapped to the "description" column in the database with a maximum length of 500 characters.
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * The date and time when the image was uploaded.
     * This field is mapped to the "upload_date" column in the database and cannot be null.
     */
    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    /**
     * This field stores the base64 encoded data of the image.
     * It is marked as @Transient, meaning it will not be persisted in the database.
     * Transient field to hold the Base64–encoded file data for display purposes.
     */
    @Transient
    private String base64Data;
}
