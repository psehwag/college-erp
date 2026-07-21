package com.erp.marks.service;

import com.erp.common.exception.AppException;
import com.erp.course.entity.Batch;
import com.erp.course.repository.BatchRepository;
import com.erp.marks.entity.Marks;
import com.erp.marks.repository.MarksRepository;
import com.erp.student.entity.Student;
import com.erp.student.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Transactional
public class MarksService {

    private static final Logger log = Logger.getLogger(MarksService.class.getName());

    // Marks are now recorded as a single overall mark per subject per
    // academic year — the UI no longer asks for exam type or academic
    // year; both are auto-derived here so the schema/unique-constraint
    // (studentId + subjectId + examType + academicYear) still holds.
    private static final Marks.ExamType DEFAULT_EXAM_TYPE = Marks.ExamType.FINAL;

    private final MarksRepository marksRepo;
    private final BatchRepository batchRepo;
    private final StudentRepository studentRepo;

    public MarksService(MarksRepository marksRepo, BatchRepository batchRepo, StudentRepository studentRepo) {
        this.marksRepo   = marksRepo;
        this.batchRepo   = batchRepo;
        this.studentRepo = studentRepo;
    }

    private String academicYearForBatch(Long batchId) {
        return batchRepo.findById(batchId)
                .map(Batch::getAcademicYear)
                .orElseThrow(() -> new AppException("Batch not found", HttpStatus.NOT_FOUND));
    }

    public Marks upsert(Map<String, Object> req) {
        Long   studentId     = Long.parseLong(req.get("studentId").toString());
        Long   subjectId     = Long.parseLong(req.get("subjectId").toString());
        Long   facultyId     = Long.parseLong(req.get("facultyId").toString());
        Long   batchId       = Long.parseLong(req.get("batchId").toString());
        Double marksObtained = Double.parseDouble(req.get("marksObtained").toString());
        Double maxMarks      = Double.parseDouble(req.get("maxMarks").toString());
        String academicYear  = academicYearForBatch(batchId);

        if (marksObtained > maxMarks)
            throw new AppException("Marks obtained cannot exceed max marks", HttpStatus.BAD_REQUEST);

        Optional<Marks> existing = marksRepo
                .findByStudentIdAndSubjectIdAndExamTypeAndAcademicYear(
                        studentId, subjectId, DEFAULT_EXAM_TYPE, academicYear);

        Marks m = existing.orElseGet(Marks::new);
        m.setStudentId(studentId);
        m.setSubjectId(subjectId);
        m.setFacultyId(facultyId);
        m.setBatchId(batchId);
        m.setSemester(Integer.parseInt(req.get("semester").toString()));
        m.setExamType(DEFAULT_EXAM_TYPE);
        m.setMarksObtained(marksObtained);
        m.setMaxMarks(maxMarks);
        m.setAcademicYear(academicYear);
        if (req.get("remarks") != null) m.setRemarks(req.get("remarks").toString());

        return marksRepo.save(m);
    }

    public List<Marks> bulkUpsert(Map<String, Object> req) {
        Long   subjectId = Long.parseLong(req.get("subjectId").toString());
        Long   facultyId = Long.parseLong(req.get("facultyId").toString());
        Long   batchId   = Long.parseLong(req.get("batchId").toString());
        Double maxMarks  = Double.parseDouble(req.get("maxMarks").toString());
        Integer semester = Integer.parseInt(req.get("semester").toString());
        String academicYear = academicYearForBatch(batchId);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> studentMarks =
                (List<Map<String, Object>>) req.get("studentMarks");

        if (studentMarks == null || studentMarks.isEmpty()) {
            throw new AppException("No student marks provided", HttpStatus.BAD_REQUEST);
        }

        List<Marks> toSave = new ArrayList<>();
        for (Map<String, Object> sm : studentMarks) {
            Long   sid      = Long.parseLong(sm.get("studentId").toString());
            Double obtained = Double.parseDouble(sm.get("marksObtained").toString());
            if (obtained > maxMarks)
                throw new AppException("Marks for student " + sid + " exceed max marks", HttpStatus.BAD_REQUEST);

            Optional<Marks> existing = marksRepo
                    .findByStudentIdAndSubjectIdAndExamTypeAndAcademicYear(
                            sid, subjectId, DEFAULT_EXAM_TYPE, academicYear);

            Marks m = existing.orElseGet(Marks::new);
            m.setStudentId(sid);
            m.setSubjectId(subjectId);
            m.setFacultyId(facultyId);
            m.setBatchId(batchId);
            m.setSemester(semester);
            m.setExamType(DEFAULT_EXAM_TYPE);
            m.setMarksObtained(obtained);
            m.setMaxMarks(maxMarks);
            m.setAcademicYear(academicYear);
            if (sm.get("remarks") != null) m.setRemarks(sm.get("remarks").toString());
            toSave.add(m);
        }
        return marksRepo.saveAll(toSave);
    }

    @Transactional(readOnly = true)
    public List<Marks> getByStudent(Long studentId) {
        return marksRepo.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSemesterSummary(Long studentId, Integer semester) {
        List<Marks> list = marksRepo.findByStudentIdAndSemester(studentId, semester);
        double totalObtained = list.stream().mapToDouble(Marks::getMarksObtained).sum();
        double totalMax      = list.stream().mapToDouble(Marks::getMaxMarks).sum();
        double pct           = totalMax > 0 ? Math.round((totalObtained / totalMax * 100) * 100.0) / 100.0 : 0;

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("studentId", studentId);
        summary.put("semester", semester);
        summary.put("totalObtained", totalObtained);
        summary.put("totalMax", totalMax);
        summary.put("overallPercentage", pct);
        summary.put("subjectMarks", list);
        return summary;
    }

    /**
     * "View existing" for a batch+subject, enriched with the student's
     * name and enrollment number so the UI never has to show a raw studentId.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getBatchMarksEnriched(Long batchId, Long subjectId) {
        List<Marks> marks = marksRepo.findByBatchIdAndSubjectId(batchId, subjectId);
        Map<Long, Student> studentsById = studentRepo.findByBatchId(batchId).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Marks m : marks) {
            Student s = studentsById.get(m.getStudentId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", m.getId());
            row.put("studentId", m.getStudentId());
            row.put("studentName", s != null ? s.getFullName() : ("Student #" + m.getStudentId()));
            row.put("enrollmentNumber", s != null ? s.getEnrollmentNumber() : "—");
            row.put("subjectId", m.getSubjectId());
            row.put("marksObtained", m.getMarksObtained());
            row.put("maxMarks", m.getMaxMarks());
            row.put("percentage", m.getPercentage());
            row.put("grade", m.getGrade());
            result.add(row);
        }
        return result;
    }
}
