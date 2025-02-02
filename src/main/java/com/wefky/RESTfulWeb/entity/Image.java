package com.wefky.RESTfulWeb.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Represents an image entity stored in the database.
 * Contains metadata and binary data (as a byte array) for an image.
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
    @Column(name = "owner", length = 100)
    private String owner;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "data", nullable = false)
    private byte[] data;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @NotBlank(message = "Content Type is required.")
    @Column(name = "content_type", length = 50)
    private String contentType;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    @Transient
    private String base64Data;
}
