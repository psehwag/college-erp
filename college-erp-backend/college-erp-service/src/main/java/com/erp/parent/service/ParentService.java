package com.erp.parent.service;

import com.erp.auth.entity.User;
import com.erp.auth.repository.UserRepository;
import com.erp.auth.service.AuthService;
import com.erp.common.exception.AppException;
import com.erp.parent.entity.Parent;
import com.erp.parent.repository.ParentRepository;
import com.erp.student.repository.StudentRepository;
import com.erp.student.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
@Transactional
public class ParentService {

    private static final Logger log = Logger.getLogger(ParentService.class.getName());

    private final ParentRepository parentRepo;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public ParentService(ParentRepository parentRepo,
                         AuthService authService,
                         UserRepository userRepository,
                         StudentRepository studentRepository) {
        this.parentRepo     = parentRepo;
        this.authService    = authService;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    public Parent create(Map<String, Object> req) {
        String email = req.get("email").toString();
        if (parentRepo.existsByEmail(email)) {
            throw new AppException("Email already registered for another parent", HttpStatus.CONFLICT);
        }

        Parent p = new Parent();
        p.setFirstName(req.get("firstName").toString());
        p.setLastName(req.get("lastName").toString());
        p.setEmail(email);
        if (req.get("phone") != null)             p.setPhone(req.get("phone").toString());
        if (req.get("alternatePhone") != null)    p.setAlternatePhone(req.get("alternatePhone").toString());
        if (req.get("address") != null)           p.setAddress(req.get("address").toString());
        if (req.get("occupation") != null)        p.setOccupation(req.get("occupation").toString());
        if (req.get("relationToStudent") != null) p.setRelationToStudent(req.get("relationToStudent").toString());
        p.setReceiveSms(req.get("receiveSms") == null || Boolean.parseBoolean(req.get("receiveSms").toString()));
        p.setReceiveEmail(req.get("receiveEmail") == null || Boolean.parseBoolean(req.get("receiveEmail").toString()));
        p.setIsActive(true);
        p = parentRepo.save(p);

        String baseUsername = email.split("@")[0];
        User user = authService.createLinkedUser(baseUsername, email,
                p.getFirstName() + " " + p.getLastName(), User.Role.PARENT, p.getId());
        p.setUserId(user.getId());
        p = parentRepo.save(p);

        log.info("Parent created: " + email + " login=" + user.getUsername());
        return p;
    }

    @Transactional(readOnly = true)
    public Parent getById(Long id) {
        return parentRepo.findById(id)
                .orElseThrow(() -> new AppException("Parent not found: " + id, HttpStatus.NOT_FOUND));
    }

    /** Full update — every editable field including the active/inactive checkbox. */
    public Parent update(Long id, Map<String, Object> req) {
        Parent p = getById(id);
        if (req.get("firstName") != null)      p.setFirstName(req.get("firstName").toString());
        if (req.get("lastName") != null)       p.setLastName(req.get("lastName").toString());
        if (req.get("email") != null) {
            String newEmail = req.get("email").toString();
            if (!newEmail.equals(p.getEmail())) {
                if (parentRepo.existsByEmail(newEmail)) {
                    throw new AppException("Email already registered for another parent", HttpStatus.CONFLICT);
                }
                p.setEmail(newEmail);
            }
        }
        if (req.get("phone") != null)          p.setPhone(req.get("phone").toString());
        if (req.get("alternatePhone") != null) p.setAlternatePhone(req.get("alternatePhone").toString());
        if (req.get("address") != null)        p.setAddress(req.get("address").toString());
        if (req.get("occupation") != null)     p.setOccupation(req.get("occupation").toString());
        if (req.get("relationToStudent") != null) p.setRelationToStudent(req.get("relationToStudent").toString());
        if (req.get("receiveSms") != null)     p.setReceiveSms(Boolean.parseBoolean(req.get("receiveSms").toString()));
        if (req.get("receiveEmail") != null)   p.setReceiveEmail(Boolean.parseBoolean(req.get("receiveEmail").toString()));
        if (req.get("isActive") != null) {
            boolean active = Boolean.parseBoolean(req.get("isActive").toString());
            p.setIsActive(active);
            if (p.getUserId() != null) authService.setActive(p.getUserId(), active);
        }
        p = parentRepo.save(p);

        if (p.getUserId() != null) {
            authService.updateLinkedUserProfile(p.getUserId(), p.getFirstName() + " " + p.getLastName(), p.getEmail());
        }
        return p;
    }

    @Transactional(readOnly = true)
    public Page<Parent> getAll(int page, int size) {
        return parentRepo.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    /** Soft-deactivate only (kept for compatibility). */
    public void deactivate(Long id) {
        Parent p = getById(id);
        p.setIsActive(false);
        if (p.getUserId() != null) authService.setActive(p.getUserId(), false);
        parentRepo.save(p);
    }

    /**
     * Permanent hard delete — removes the parent record and their login
     * account, and unlinks (does NOT delete) any children so student
     * records are never destroyed just because a parent account was removed.
     */
    public void hardDelete(Long id) {
        Parent p = getById(id);

        List<Student> children = studentRepository.findByParentId(id);
        for (Student child : children) {
            child.setParentId(null);
            studentRepository.save(child);
        }

        if (p.getUserId() != null) {
            userRepository.findById(p.getUserId()).ifPresent(userRepository::delete);
        }

        parentRepo.delete(p);
        log.info("Parent permanently deleted (children unlinked, not deleted): " + id);
    }

    public String getLoginUsername(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId).map(User::getUsername).orElse(null);
    }
}
