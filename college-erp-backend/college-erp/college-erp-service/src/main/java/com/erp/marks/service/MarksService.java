package com.erp.marks.service;

import com.erp.common.exception.AppException;
import com.erp.marks.entity.Marks;
import com.erp.marks.repository.MarksRepository;
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

    private final MarksRepository marksRepo;

    public MarksService(MarksRepository marksRepo) {
        this.marksRepo = marksRepo;
    }

    public Marks upsert(Map<String, Object> req) {
        Long   studentId    = Long.parseLong(req.get("studentId").toString());
        Long   subjectId    = Long.parseLong(req.get("subjectId").toString());
        Long   facultyId    = Long.parseLong(req.get("facultyId").toString());
        Double marksObtained = Double.parseDouble(req.get("marksObtained").toString());
        Double maxMarks      = Double.parseDouble(req.get("maxMarks").toString());
        Marks.ExamType examType = Marks.ExamType.valueOf(req.get("examType").toString());
        String academicYear = req.get("academicYear").toString();

        if (marksObtained > maxMarks)
            throw new AppException("Marks obtained cannot exceed max marks", HttpStatus.BAD_REQUEST);

        Optional<Marks> existing = marksRepo
                .findByStudentIdAndSubjectIdAndExamTypeAndAcademicYear(
                        studentId, subjectId, examType, academicYear);

        Marks m = existing.orElseGet(Marks::new);
        m.setStudentId(studentId);
        m.setSubjectId(subjectId);
        m.setFacultyId(facultyId);
        if (req.get("batchId") != null) m.setBatchId(Long.parseLong(req.get("batchId").toString()));
        m.setSemester(Integer.parseInt(req.get("semester").toString()));
        m.setExamType(examType);
        m.setMarksObtained(marksObtained);
        m.setMaxMarks(maxMarks);
        m.setAcademicYear(academicYear);
        if (req.get("remarks") != null) m.setRemarks(req.get("remarks").toString());

        return marksRepo.save(m);
    }

    public List<Marks> bulkUpsert(Map<String, Object> req) {
        Long   subjectId    = Long.parseLong(req.get("subjectId").toString());
        Long   facultyId    = Long.parseLong(req.get("facultyId").toString());
        Long   batchId      = Long.parseLong(req.get("batchId").toString());
        Double maxMarks     = Double.parseDouble(req.get("maxMarks").toString());
        Marks.ExamType examType = Marks.ExamType.valueOf(req.get("examType").toString());
        String academicYear = req.get("academicYear").toString();
        Integer semester    = Integer.parseInt(req.get("semester").toString());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> studentMarks =
                (List<Map<String, Object>>) req.get("studentMarks");

        List<Marks> toSave = new ArrayList<>();
        for (Map<String, Object> sm : studentMarks) {
            Long   sid     = Long.parseLong(sm.get("studentId").toString());
            Double obtained = Double.parseDouble(sm.get("marksObtained").toString());
            if (obtained > maxMarks)
                throw new AppException("Marks for student " + sid + " exceed max marks", HttpStatus.BAD_REQUEST);

            Optional<Marks> existing = marksRepo
                    .findByStudentIdAndSubjectIdAndExamTypeAndAcademicYear(
                            sid, subjectId, examType, academicYear);

            Marks m = existing.orElseGet(Marks::new);
            m.setStudentId(sid);
            m.setSubjectId(subjectId);
            m.setFacultyId(facultyId);
            m.setBatchId(batchId);
            m.setSemester(semester);
            m.setExamType(examType);
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

    @Transactional(readOnly = true)
    public List<Marks> getBatchMarks(Long batchId, Long subjectId, Marks.ExamType examType) {
        return marksRepo.findByBatchIdAndSubjectIdAndExamType(batchId, subjectId, examType);
    }
}
