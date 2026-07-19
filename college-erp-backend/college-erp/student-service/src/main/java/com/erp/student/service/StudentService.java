package com.erp.student.service;

import com.erp.student.dto.StudentDto;
import com.erp.student.entity.Student;
import com.erp.student.exception.StudentException;
import com.erp.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentDto.Response createStudent(StudentDto.CreateRequest request) {
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new StudentException("Email already registered");
        }

        String enrollmentNumber = generateEnrollmentNumber(request.getDepartmentId(), request.getAdmissionYear());

        Student student = Student.builder()
                .enrollmentNumber(enrollmentNumber)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .address(request.getAddress())
                .departmentId(request.getDepartmentId())
                .courseId(request.getCourseId())
                .batchId(request.getBatchId())
                .currentSemester(request.getCurrentSemester())
                .admissionYear(request.getAdmissionYear())
                .parentId(request.getParentId())
                .status(Student.StudentStatus.ACTIVE)
                .build();

        student = studentRepository.save(student);
        log.info("Student created: {}", student.getEnrollmentNumber());
        return mapToResponse(student);
    }

    @Transactional(readOnly = true)
    public StudentDto.Response getStudentById(Long id) {
        return studentRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new StudentException("Student not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public StudentDto.Response getStudentByEnrollment(String enrollmentNumber) {
        return studentRepository.findByEnrollmentNumber(enrollmentNumber)
                .map(this::mapToResponse)
                .orElseThrow(() -> new StudentException("Student not found: " + enrollmentNumber));
    }

    public StudentDto.Response updateStudent(Long id, StudentDto.UpdateRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentException("Student not found with id: " + id));

        if (request.getFirstName() != null) student.setFirstName(request.getFirstName());
        if (request.getLastName() != null) student.setLastName(request.getLastName());
        if (request.getPhone() != null) student.setPhone(request.getPhone());
        if (request.getDateOfBirth() != null) student.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) student.setGender(request.getGender());
        if (request.getAddress() != null) student.setAddress(request.getAddress());
        if (request.getBatchId() != null) student.setBatchId(request.getBatchId());
        if (request.getCurrentSemester() != null) student.setCurrentSemester(request.getCurrentSemester());
        if (request.getParentId() != null) student.setParentId(request.getParentId());
        if (request.getStatus() != null) student.setStatus(request.getStatus());

        return mapToResponse(studentRepository.save(student));
    }

    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentException("Student not found with id: " + id));
        student.setStatus(Student.StudentStatus.INACTIVE);
        studentRepository.save(student);
        log.info("Student deactivated: {}", id);
    }

    @Transactional(readOnly = true)
    public Page<StudentDto.Response> getAllStudents(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        return studentRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<StudentDto.Response> searchStudents(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return studentRepository.search(query, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<StudentDto.Summary> getStudentsByBatch(Long batchId) {
        return studentRepository.findByBatchId(batchId).stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentDto.Summary> getStudentsByDepartment(Long departmentId) {
        return studentRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    public void markFaceEnrolled(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentException("Student not found"));
        student.setFaceEnrolled(true);
        studentRepository.save(student);
    }

    private String generateEnrollmentNumber(Long departmentId, Integer admissionYear) {
        long count = studentRepository.count() + 1;
        return String.format("ENR%d%02d%04d", admissionYear % 100, departmentId, count);
    }

    private StudentDto.Response mapToResponse(Student s) {
        return StudentDto.Response.builder()
                .id(s.getId()).enrollmentNumber(s.getEnrollmentNumber())
                .firstName(s.getFirstName()).lastName(s.getLastName())
                .fullName(s.getFullName()).email(s.getEmail())
                .phone(s.getPhone()).dateOfBirth(s.getDateOfBirth())
                .gender(s.getGender()).address(s.getAddress())
                .photoUrl(s.getPhotoUrl()).departmentId(s.getDepartmentId())
                .courseId(s.getCourseId()).batchId(s.getBatchId())
                .currentSemester(s.getCurrentSemester()).admissionYear(s.getAdmissionYear())
                .parentId(s.getParentId()).status(s.getStatus())
                .faceEnrolled(s.getFaceEnrolled()).createdAt(s.getCreatedAt())
                .build();
    }

    private StudentDto.Summary mapToSummary(Student s) {
        return StudentDto.Summary.builder()
                .id(s.getId()).enrollmentNumber(s.getEnrollmentNumber())
                .fullName(s.getFullName()).email(s.getEmail())
                .departmentId(s.getDepartmentId()).currentSemester(s.getCurrentSemester())
                .status(s.getStatus()).build();
    }
}
