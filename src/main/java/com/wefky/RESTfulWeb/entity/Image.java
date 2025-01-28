package com.wefky.RESTfulWeb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing an Image record.
 * Currently has:
 * - imageId (PK)
 * - owner (User)
 * - data (LOB)
 * - deleted flag
 * - contentType (String)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = true)
    private User owner; // If you have a User entity.

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "data", nullable = false)
    private byte[] data;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "content_type", length = 50)
    private String contentType;

//     //If you want to store file name or title, add:
//     @Column(name = "title", length = 255)
//     private String title;
    
//    // Possibly:
//     @Column(name = "description", length = 500)
//     private String description;
}
