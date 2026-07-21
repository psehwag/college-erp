package com.erp.face.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "face_enrollments")
public class FaceEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Lob
    @Column(name = "face_image_data", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] faceImageData;

    @Column(name = "image_index")
    private Integer imageIndex;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "enrolled_at", updatable = false)
    private LocalDateTime enrolledAt;

    public FaceEnrollment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public byte[] getFaceImageData() { return faceImageData; }
    public void setFaceImageData(byte[] faceImageData) { this.faceImageData = faceImageData; }
    public Integer getImageIndex() { return imageIndex; }
    public void setImageIndex(Integer imageIndex) { this.imageIndex = imageIndex; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getEnrolledAt() { return enrolledAt; }
}
