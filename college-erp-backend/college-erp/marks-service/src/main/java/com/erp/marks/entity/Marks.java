package com.erp.marks.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "marks",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id","subject_id","exam_type","academic_year"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Marks {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "faculty_id", nullable = false)
    private Long facultyId;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false)
    private ExamType examType;

    @Column(name = "marks_obtained", nullable = false)
    private Double marksObtained;

    @Column(name = "max_marks", nullable = false)
    private Double maxMarks;

    @Column(name = "academic_year", nullable = false, length = 10)
    private String academicYear;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade")
    private Grade grade;

    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at") private LocalDateTime updatedAt;

    public enum ExamType { INTERNAL_1, INTERNAL_2, MIDTERM, FINAL, PRACTICAL, ASSIGNMENT, VIVA }

    public enum Grade { O, A_PLUS, A, B_PLUS, B, C, D, F }

    @PrePersist @PreUpdate
    public void computeGrade() {
        if (maxMarks == null || maxMarks == 0) return;
        double pct = (marksObtained / maxMarks) * 100;
        if (pct >= 90) grade = Grade.O;
        else if (pct >= 85) grade = Grade.A_PLUS;
        else if (pct >= 75) grade = Grade.A;
        else if (pct >= 65) grade = Grade.B_PLUS;
        else if (pct >= 55) grade = Grade.B;
        else if (pct >= 45) grade = Grade.C;
        else if (pct >= 35) grade = Grade.D;
        else grade = Grade.F;
    }
}
