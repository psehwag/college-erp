package com.erp.face.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "face_enrollments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FaceEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Lob
    @Column(name = "face_image_data", nullable = false)
    private byte[] faceImageData; // Grayscale 200x200 JPEG bytes

    @Column(name = "image_index")
    private Integer imageIndex; // Position in the enrollment set

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "enrolled_at", updatable = false)
    private LocalDateTime enrolledAt;
}
