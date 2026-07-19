package com.erp.parent.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name = "parents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Parent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(length = 15)
    private String phone;

    @Column(name = "alternate_phone", length = 15)
    private String alternatePhone;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String occupation;

    @Column(name = "relation_to_student", length = 20)
    private String relationToStudent; // FATHER, MOTHER, GUARDIAN

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "receive_sms")
    private Boolean receiveSms = true;

    @Builder.Default
    @Column(name = "receive_email")
    private Boolean receiveEmail = true;

    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public String getFullName() { return firstName + " " + lastName; }
}
