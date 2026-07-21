package com.erp.student.service;

import com.erp.attendance.repository.AttendanceRepository;
import com.erp.auth.entity.User;
import com.erp.auth.repository.UserRepository;
import com.erp.auth.service.AuthService;
import com.erp.common.exception.AppException;
import com.erp.face.repository.FaceEnrollmentRepository;
import com.erp.marks.repository.MarksRepository;
import com.erp.student.dto.StudentDto;
import com.erp.student.entity.Student;
import com.erp.student.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentService {

    private static final Logger log = Logger.getLogger(StudentService.class.getName());

    private final StudentRepository studentRepository;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarksRepository marksRepository;
    private final FaceEnrollmentRepository faceEnrollmentRepository;

    public StudentService(StudentRepository studentRepository,
                          AuthService authService,
                          UserRepository userRepository,
                          AttendanceRepository attendanceRepository,
                          MarksRepository marksRepository,
                          FaceEnrollmentRepository faceEnrollmentRepository) {
        this.studentRepository = studentRepository;
        this.authService       = authService;
        this.userRepository    = userRepository;
        this.attendanceRepository = attendanceRepository;
        this.marksRepository = marksRepository;
        this.faceEnrollmentRepository = faceEnrollmentRepository;
    }

    public StudentDto.Response createStudent(StudentDto.CreateRequest req) {
        if (studentRepository.existsByEmail(req.getEmail())) {
            throw new AppException("Email already registered for another student", HttpStatus.CONFLICT);
        }

        String enrollmentNumber = generateEnrollmentNumber(req.getDepartmentId(), req.getAdmissionYear());

        Student student = new Student();
        student.setEnrollmentNumber(enrollmentNumber);
        student.setFirstName(req.getFirstName());
        student.setLastName(req.getLastName());
        student.setEmail(req.getEmail());
        student.setPhone(req.getPhone());
        student.setDateOfBirth(req.getDateOfBirth());
        student.setGender(req.getGender());
        student.setAddress(req.getAddress());
        student.setDepartmentId(req.getDepartmentId());
        student.setCourseId(req.getCourseId());
        student.setBatchId(req.getBatchId());
        student.setCurrentSemester(req.getCurrentSemester());
        student.setAdmissionYear(req.getAdmissionYear());
        student.setParentId(req.getParentId());
        student.setStatus(Student.StudentStatus.ACTIVE);
        student.setFaceEnrolled(false);
        student = studentRepository.save(student);

        // Auto-create login credentials for the new student
        User user = authService.createLinkedUser(
                enrollmentNumber,
                req.getEmail(),
                req.getFirstName() + " " + req.getLastName(),
                User.Role.STUDENT,
                student.getId()
        );
        student.setUserId(user.getId());
        student = studentRepository.save(student);

        log.info("Student created: " + enrollmentNumber + " login=" + user.getUsername());
        return toResponse(student, user.getUsername());
    }

    @Transactional(readOnly = true)
    public StudentDto.Response getById(Long id) {
        Student s = findById(id);
        return toResponse(s, resolveUsername(s.getUserId()));
    }

    @Transactional(readOnly = true)
    public StudentDto.Response getByEnrollment(String en) {
        Student s = studentRepository.findByEnrollmentNumber(en)
                .orElseThrow(() -> new AppException("Student not found: " + en, HttpStatus.NOT_FOUND));
        return toResponse(s, resolveUsername(s.getUserId()));
    }

    /**
     * Full update — every editable field on the student, including
     * department/course/batch/semester/admission year and the
     * active/inactive checkbox (mapped to Student.status + linked User.isActive).
     */
    public StudentDto.Response update(Long id, StudentDto.UpdateRequest req) {
        Student s = findById(id);

        if (req.getFirstName() != null)       s.setFirstName(req.getFirstName());
        if (req.getLastName() != null)         s.setLastName(req.getLastName());
        if (req.getEmail() != null && !req.getEmail().isBlank() && !req.getEmail().equals(s.getEmail())) {
            if (studentRepository.existsByEmail(req.getEmail())) {
                throw new AppException("Email already registered for another student", HttpStatus.CONFLICT);
            }
            s.setEmail(req.getEmail());
        }
        if (req.getPhone() != null)            s.setPhone(req.getPhone());
        if (req.getDateOfBirth() != null)      s.setDateOfBirth(req.getDateOfBirth());
        if (req.getGender() != null)           s.setGender(req.getGender());
        if (req.getAddress() != null)          s.setAddress(req.getAddress());
        if (req.getDepartmentId() != null)     s.setDepartmentId(req.getDepartmentId());
        if (req.getCourseId() != null)         s.setCourseId(req.getCourseId());
        if (req.getBatchId() != null)          s.setBatchId(req.getBatchId());
        if (req.getCurrentSemester() != null)  s.setCurrentSemester(req.getCurrentSemester());
        if (req.getAdmissionYear() != null)    s.setAdmissionYear(req.getAdmissionYear());
        // parentId can legitimately be set to null to unlink -> use the "parentIdProvided" flag
        if (req.isParentIdProvided())          s.setParentId(req.getParentId());

        if (req.getStatus() != null) {
            s.setStatus(req.getStatus());
            if (s.getUserId() != null) {
                authService.setActive(s.getUserId(), req.getStatus() == Student.StudentStatus.ACTIVE);
            }
        }

        s = studentRepository.save(s);

        // Keep the linked login account's name/email in sync
        if (s.getUserId() != null) {
            authService.updateLinkedUserProfile(s.getUserId(), s.getFullName(), s.getEmail());
        }

        return toResponse(s, resolveUsername(s.getUserId()));
    }

    /**
     * Soft-deactivate only (kept for backward compatibility / bulk ops).
     */
    public void deactivate(Long id) {
        Student s = findById(id);
        s.setStatus(Student.StudentStatus.INACTIVE);
        if (s.getUserId() != null) authService.setActive(s.getUserId(), false);
        studentRepository.save(s);
        log.info("Student deactivated: " + id);
    }

    /**
     * Permanent hard delete — removes the student record AND all data that
     * only makes sense in the context of this student: attendance history,
     * marks, face enrollment images, and the login account.
     */
    public void hardDelete(Long id) {
        Student s = findById(id);

        attendanceRepository.deleteByStudentId(id);
        marksRepository.deleteByStudentId(id);
        faceEnrollmentRepository.deleteByStudentId(id);

        if (s.getUserId() != null) {
            userRepository.findById(s.getUserId()).ifPresent(userRepository::delete);
        }

        studentRepository.delete(s);
        log.info("Student permanently deleted (with dependent data): " + id);
    }

    @Transactional(readOnly = true)
    public Page<StudentDto.Response> getAll(int page, int size, String sort) {
        return studentRepository.findAll(PageRequest.of(page, size, Sort.by(sort)))
                .map(s -> toResponse(s, resolveUsername(s.getUserId())));
    }

    @Transactional(readOnly = true)
    public Page<StudentDto.Response> search(String q, int page, int size) {
        return studentRepository.search(q, PageRequest.of(page, size))
                .map(s -> toResponse(s, resolveUsername(s.getUserId())));
    }

    @Transactional(readOnly = true)
    public List<StudentDto.Summary> getByBatch(Long batchId) {
        return studentRepository.findByBatchId(batchId)
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentDto.Summary> getByDepartment(Long deptId) {
        return studentRepository.findByDepartmentIdAndStatus(deptId, Student.StudentStatus.ACTIVE)
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentDto.Summary> getByParent(Long parentId) {
        return studentRepository.findByParentId(parentId)
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    public void markFaceEnrolled(Long studentId) {
        Student s = findById(studentId);
        s.setFaceEnrolled(true);
        studentRepository.save(s);
    }

    /** Used by CourseService when cascading a department/course/batch deletion. */
    @Transactional(readOnly = true)
    public List<Student> findRawByDepartment(Long deptId) {
        return studentRepository.findByDepartmentIdAndStatus(deptId, Student.StudentStatus.ACTIVE);
    }

    public void hardDeleteAllIn(List<Long> studentIds) {
        for (Long id : studentIds) {
            try { hardDelete(id); } catch (Exception e) { log.warning("Could not delete student " + id + ": " + e.getMessage()); }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private String generateEnrollmentNumber(Long deptId, Integer year) {
        long count = studentRepository.count() + 1;
        return String.format("ENR%d%02d%04d", year % 100, deptId, count);
    }

    private Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new AppException("Student not found: " + id, HttpStatus.NOT_FOUND));
    }

    private String resolveUsername(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId).map(User::getUsername).orElse(null);
    }

    private StudentDto.Response toResponse(Student s, String username) {
        StudentDto.Response r = new StudentDto.Response();
        r.setId(s.getId());
        r.setEnrollmentNumber(s.getEnrollmentNumber());
        r.setFirstName(s.getFirstName());
        r.setLastName(s.getLastName());
        r.setFullName(s.getFullName());
        r.setEmail(s.getEmail());
        r.setPhone(s.getPhone());
        r.setDateOfBirth(s.getDateOfBirth());
        r.setGender(s.getGender());
        r.setAddress(s.getAddress());
        r.setPhotoUrl(s.getPhotoUrl());
        r.setDepartmentId(s.getDepartmentId());
        r.setCourseId(s.getCourseId());
        r.setBatchId(s.getBatchId());
        r.setCurrentSemester(s.getCurrentSemester());
        r.setAdmissionYear(s.getAdmissionYear());
        r.setParentId(s.getParentId());
        r.setStatus(s.getStatus());
        r.setFaceEnrolled(s.getFaceEnrolled());
        r.setUserId(s.getUserId());
        r.setLoginUsername(username);
        r.setCreatedAt(s.getCreatedAt());
        return r;
    }

    private StudentDto.Summary toSummary(Student s) {
        StudentDto.Summary sm = new StudentDto.Summary();
        sm.setId(s.getId());
        sm.setEnrollmentNumber(s.getEnrollmentNumber());
        sm.setFullName(s.getFullName());
        sm.setEmail(s.getEmail());
        sm.setDepartmentId(s.getDepartmentId());
        sm.setCurrentSemester(s.getCurrentSemester());
        sm.setStatus(s.getStatus());
        return sm;
    }
}
