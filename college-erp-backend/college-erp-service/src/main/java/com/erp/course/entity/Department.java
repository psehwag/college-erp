package com.erp.course.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "departments")
public class Department {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 100) private String name;
    @Column(unique = true, nullable = false, length = 10)  private String code;
    @Column(length = 500) private String description;
    @Column(name = "head_faculty_id") private Long headFacultyId;
    @Column(name = "is_active") private Boolean isActive = true;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp  @Column(name = "updated_at") private LocalDateTime updatedAt;

    public Department() {}
    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getCode() { return code; }
    public void setCode(String v) { this.code = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public Long getHeadFacultyId() { return headFacultyId; }
    public void setHeadFacultyId(Long v) { this.headFacultyId = v; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean v) { this.isActive = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
