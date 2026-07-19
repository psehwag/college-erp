package com.erp.marks.service;

import com.erp.marks.dto.MarksDto;
import com.erp.marks.entity.Marks;
import com.erp.marks.exception.MarksException;
import com.erp.marks.repository.MarksRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Slf4j @Transactional
public class MarksService {

    private final MarksRepository marksRepository;

    public MarksDto.Response upsertMarks(MarksDto.UpsertRequest req) {
        if (req.getMarksObtained() > req.getMaxMarks())
            throw new MarksException("Marks obtained cannot exceed max marks");

        Optional<Marks> existing = marksRepository
                .findByStudentIdAndSubjectIdAndExamTypeAndAcademicYear(
                        req.getStudentId(), req.getSubjectId(), req.getExamType(), req.getAcademicYear());

        Marks marks;
        if (existing.isPresent()) {
            marks = existing.get();
            marks.setMarksObtained(req.getMarksObtained());
            marks.setMaxMarks(req.getMaxMarks());
            marks.setRemarks(req.getRemarks());
        } else {
            marks = Marks.builder()
                    .studentId(req.getStudentId()).subjectId(req.getSubjectId())
                    .facultyId(req.getFacultyId()).batchId(req.getBatchId())
                    .semester(req.getSemester()).examType(req.getExamType())
                    .marksObtained(req.getMarksObtained()).maxMarks(req.getMaxMarks())
                    .academicYear(req.getAcademicYear()).remarks(req.getRemarks()).build();
        }
        return toResponse(marksRepository.save(marks));
    }

    public List<MarksDto.Response> bulkUpsert(MarksDto.BulkUpsertRequest req) {
        List<Marks> toSave = new ArrayList<>();
        for (MarksDto.StudentMark sm : req.getStudentMarks()) {
            if (sm.getMarksObtained() > req.getMaxMarks())
                throw new MarksException("Marks for student " + sm.getStudentId() + " exceed max marks");

            Optional<Marks> existing = marksRepository
                    .findByStudentIdAndSubjectIdAndExamTypeAndAcademicYear(
                            sm.getStudentId(), req.getSubjectId(), req.getExamType(), req.getAcademicYear());

            Marks marks = existing.orElseGet(() -> Marks.builder()
                    .studentId(sm.getStudentId()).subjectId(req.getSubjectId())
                    .facultyId(req.getFacultyId()).batchId(req.getBatchId())
                    .semester(req.getSemester()).examType(req.getExamType())
                    .maxMarks(req.getMaxMarks()).academicYear(req.getAcademicYear()).build());

            marks.setMarksObtained(sm.getMarksObtained());
            marks.setRemarks(sm.getRemarks());
            toSave.add(marks);
        }
        return marksRepository.saveAll(toSave).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MarksDto.Response> getMarksByStudent(Long studentId) {
        return marksRepository.findByStudentId(studentId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MarksDto.StudentSummary getStudentSemesterSummary(Long studentId, Integer semester) {
        List<Marks> marksList = marksRepository.findByStudentIdAndSemester(studentId, semester);
        double totalObtained = marksList.stream().mapToDouble(Marks::getMarksObtained).sum();
        double totalMax = marksList.stream().mapToDouble(Marks::getMaxMarks).sum();
        double pct = totalMax > 0 ? (totalObtained / totalMax) * 100 : 0;

        return MarksDto.StudentSummary.builder()
                .studentId(studentId).semester(semester)
                .totalObtained(totalObtained).totalMax(totalMax)
                .overallPercentage(Math.round(pct * 100.0) / 100.0)
                .overallGrade(calculateGrade(pct))
                .subjectMarks(marksList.stream().map(this::toResponse).collect(Collectors.toList()))
                .build();
    }

    @Transactional(readOnly = true)
    public List<MarksDto.Response> getMarksByBatchSubjectExam(Long batchId, Long subjectId, Marks.ExamType examType) {
        return marksRepository.findByBatchIdAndSubjectIdAndExamType(batchId, subjectId, examType)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private Marks.Grade calculateGrade(double pct) {
        if (pct >= 90) return Marks.Grade.O;
        if (pct >= 85) return Marks.Grade.A_PLUS;
        if (pct >= 75) return Marks.Grade.A;
        if (pct >= 65) return Marks.Grade.B_PLUS;
        if (pct >= 55) return Marks.Grade.B;
        if (pct >= 45) return Marks.Grade.C;
        if (pct >= 35) return Marks.Grade.D;
        return Marks.Grade.F;
    }

    private MarksDto.Response toResponse(Marks m) {
        double pct = m.getMaxMarks() > 0 ? (m.getMarksObtained() / m.getMaxMarks()) * 100 : 0;
        return MarksDto.Response.builder()
                .id(m.getId()).studentId(m.getStudentId()).subjectId(m.getSubjectId())
                .facultyId(m.getFacultyId()).batchId(m.getBatchId()).semester(m.getSemester())
                .examType(m.getExamType()).marksObtained(m.getMarksObtained()).maxMarks(m.getMaxMarks())
                .percentage(Math.round(pct * 100.0) / 100.0).grade(m.getGrade())
                .academicYear(m.getAcademicYear()).remarks(m.getRemarks()).updatedAt(m.getUpdatedAt()).build();
    }
}
