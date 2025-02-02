package com.wefky.RESTfulWeb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.*;

/**
 * Represents an image entity stored in the database.
 */
@Entity
@Table(name = "images")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @NotBlank(message = "Owner is required.")
    @Column(name = "owner", length = 100, nullable = false)
    private String owner;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "data", nullable = false)
    private byte[] data;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @NotBlank(message = "Content Type is required.")
    @Column(name = "content_type", length = 50, nullable = false)
    private String contentType;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    /**
     * Transient field to hold the Base64-encoded image data for display purposes.
     */
    @Transient
    private String base64Data;
}
