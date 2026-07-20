package com.erp.parent.service;

import com.erp.auth.entity.User;
import com.erp.auth.repository.UserRepository;
import com.erp.auth.service.AuthService;
import com.erp.common.exception.AppException;
import com.erp.parent.entity.Parent;
import com.erp.parent.repository.ParentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.logging.Logger;

@Service
@Transactional
public class ParentService {

    private static final Logger log = Logger.getLogger(ParentService.class.getName());

    private final ParentRepository parentRepo;
    private final AuthService authService;
    private final UserRepository userRepository;

    public ParentService(ParentRepository parentRepo,
                         AuthService authService,
                         UserRepository userRepository) {
        this.parentRepo     = parentRepo;
        this.authService    = authService;
        this.userRepository = userRepository;
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

        // Auto-create login: username = first part of email before @
        String baseUsername = email.split("@")[0];
        User user = authService.createLinkedUser(baseUsername, email, User.Role.PARENT, p.getId());
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

    public Parent update(Long id, Map<String, Object> req) {
        Parent p = getById(id);
        if (req.get("phone") != null)          p.setPhone(req.get("phone").toString());
        if (req.get("alternatePhone") != null) p.setAlternatePhone(req.get("alternatePhone").toString());
        if (req.get("address") != null)        p.setAddress(req.get("address").toString());
        if (req.get("occupation") != null)     p.setOccupation(req.get("occupation").toString());
        if (req.get("receiveSms") != null)     p.setReceiveSms(Boolean.parseBoolean(req.get("receiveSms").toString()));
        if (req.get("receiveEmail") != null)   p.setReceiveEmail(Boolean.parseBoolean(req.get("receiveEmail").toString()));
        return parentRepo.save(p);
    }

    public void delete(Long id) {
        Parent p = getById(id);
        p.setIsActive(false);
        if (p.getUserId() != null) authService.setActive(p.getUserId(), false);
        parentRepo.save(p);
    }

    public String getLoginUsername(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId).map(User::getUsername).orElse(null);
    }
}
