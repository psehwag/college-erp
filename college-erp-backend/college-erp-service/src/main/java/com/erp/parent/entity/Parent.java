package com.erp.parent.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "parents")
public class Parent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50) private String firstName;
    @Column(name = "last_name",  nullable = false, length = 50) private String lastName;
    @Column(unique = true, nullable = false, length = 100)      private String email;
    @Column(length = 15) private String phone;
    @Column(name = "alternate_phone", length = 15) private String alternatePhone;
    @Column(length = 500) private String address;
    @Column(length = 100) private String occupation;
    @Column(name = "relation_to_student", length = 20) private String relationToStudent;
    @Column(name = "is_active") private Boolean isActive = true;
    @Column(name = "receive_sms")   private Boolean receiveSms   = true;
    @Column(name = "receive_email") private Boolean receiveEmail = true;
    @Column(name = "user_id") private Long userId;

    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp  @Column(name = "updated_at") private LocalDateTime updatedAt;

    public Parent() {}

    public String getFullName() { return firstName + " " + lastName; }

    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }
    public String getAlternatePhone() { return alternatePhone; }
    public void setAlternatePhone(String v) { this.alternatePhone = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { this.address = v; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String v) { this.occupation = v; }
    public String getRelationToStudent() { return relationToStudent; }
    public void setRelationToStudent(String v) { this.relationToStudent = v; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean v) { this.isActive = v; }
    public Boolean getReceiveSms() { return receiveSms; }
    public void setReceiveSms(Boolean v) { this.receiveSms = v; }
    public Boolean getReceiveEmail() { return receiveEmail; }
    public void setReceiveEmail(Boolean v) { this.receiveEmail = v; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
