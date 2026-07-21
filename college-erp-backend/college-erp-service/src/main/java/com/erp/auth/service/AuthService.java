package com.erp.auth.service;

import com.erp.auth.dto.AuthDto;
import com.erp.auth.entity.User;
import com.erp.auth.repository.UserRepository;
import com.erp.auth.security.JwtService;
import com.erp.common.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@Service
@Transactional
public class AuthService {

    private static final Logger log = Logger.getLogger(AuthService.class.getName());
    public static final String DEFAULT_PASSWORD = "Password@123";

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager) {
        this.userRepository      = userRepository;
        this.jwtService          = jwtService;
        this.passwordEncoder     = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    // ── Login ─────────────────────────────────────────────────────────────

    public AuthDto.LoginResponse login(AuthDto.LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            req.getUsernameOrEmail(), req.getPassword()));
        } catch (BadCredentialsException e) {
            throw new AppException("Invalid username/email or password", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository
                .findByUsernameOrEmail(req.getUsernameOrEmail(), req.getUsernameOrEmail())
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AppException("Account is deactivated. Contact administrator.", HttpStatus.FORBIDDEN);
        }

        String accessToken  = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        userRepository.updateRefreshToken(user.getId(), refreshToken);
        userRepository.updateLastLogin(user.getId(), LocalDateTime.now());

        log.info("User logged in: " + user.getUsername() + " role=" + user.getRole());
        return buildLoginResponse(user, accessToken, refreshToken);
    }

    // ── Refresh token ─────────────────────────────────────────────────────

    public AuthDto.LoginResponse refreshToken(AuthDto.RefreshTokenRequest req) {
        User user = userRepository.findByRefreshToken(req.getRefreshToken())
                .orElseThrow(() -> new AppException("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED));

        String newAccess  = jwtService.generateAccessToken(user);
        String newRefresh = jwtService.generateRefreshToken(user);
        userRepository.updateRefreshToken(user.getId(), newRefresh);
        return buildLoginResponse(user, newAccess, newRefresh);
    }

    // ── Logout ────────────────────────────────────────────────────────────

    public void logout(Long userId) {
        userRepository.updateRefreshToken(userId, null);
        log.info("User " + userId + " logged out");
    }

    // ── Change own password ───────────────────────────────────────────────

    public void changePassword(Long userId, AuthDto.ChangePasswordRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new AppException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }

        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new AppException(
                    "New password must be different from your current password",
                    HttpStatus.BAD_REQUEST);
        }

        userRepository.updatePassword(userId, passwordEncoder.encode(req.getNewPassword()));
        log.info("Password changed by user " + userId);
    }

    // ── Admin resets any user's password ─────────────────────────────────

    public void adminResetPassword(AuthDto.AdminResetPasswordRequest req) {
        if (!userRepository.existsById(req.getUserId())) {
            throw new AppException("User not found", HttpStatus.NOT_FOUND);
        }
        userRepository.updatePassword(req.getUserId(), passwordEncoder.encode(req.getNewPassword()));
        log.info("Admin reset password for user " + req.getUserId());
    }

    // ── Create admin user (ADMIN only) ────────────────────────────────────

    public User createAdminUser(AuthDto.CreateAdminRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new AppException("Username already taken", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new AppException("Email already registered", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setName((req.getName() == null || req.getName().isBlank()) ? req.getUsername() : req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(User.Role.ADMIN);
        user.setIsActive(true);
        user.setIsEmailVerified(false);
        user.setMustChangePassword(false);

        user = userRepository.save(user);
        log.info("Admin user created: " + req.getUsername());
        return user;
    }

    // ── Create linked user (called when student/faculty/parent is created) ─

    public User createLinkedUser(String baseUsername, String email, String fullName,
                                  User.Role role, Long referenceId) {
        String username = makeUniqueUsername(baseUsername);

        if (userRepository.existsByEmail(email)) {
            throw new AppException(
                    "Email " + email + " is already registered to another user",
                    HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setUsername(username);
        user.setName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setRole(role);
        user.setReferenceId(referenceId);
        user.setIsActive(true);
        user.setIsEmailVerified(false);
        user.setMustChangePassword(true); // force change on first login

        user = userRepository.save(user);
        log.info("Linked user created: " + username + " role=" + role + " referenceId=" + referenceId);
        return user;
    }

    /** Keep old signature working if anything still calls it without a name. */
    public User createLinkedUser(String baseUsername, String email, User.Role role, Long referenceId) {
        return createLinkedUser(baseUsername, email, baseUsername, role, referenceId);
    }

    // ── Update a linked user's name/email when the owning record changes ────

    public void updateLinkedUserProfile(Long userId, String fullName, String email) {
        if (userId == null) return;
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;
        if (fullName != null) user.setName(fullName);
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new AppException("Email " + email + " is already in use", HttpStatus.CONFLICT);
            }
            user.setEmail(email);
        }
        userRepository.save(user);
    }

    // ── Admin self-management ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public java.util.List<AuthDto.AdminSummary> listAdmins() {
        return userRepository.findByRole(User.Role.ADMIN).stream()
                .map(u -> new AuthDto.AdminSummary(
                        u.getId(), u.getUsername(), u.getName(), u.getEmail(),
                        u.getIsActive(), u.getCreatedAt()))
                .collect(java.util.stream.Collectors.toList());
    }

    public AuthDto.AdminSummary updateAdmin(Long id, AuthDto.UpdateAdminRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("Admin not found", HttpStatus.NOT_FOUND));
        if (user.getRole() != User.Role.ADMIN) {
            throw new AppException("User is not an admin", HttpStatus.BAD_REQUEST);
        }
        if (req.getName() != null && !req.getName().isBlank()) user.setName(req.getName());
        if (req.getEmail() != null && !req.getEmail().isBlank() && !req.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(req.getEmail())) {
                throw new AppException("Email already in use", HttpStatus.CONFLICT);
            }
            user.setEmail(req.getEmail());
        }
        user = userRepository.save(user);
        return new AuthDto.AdminSummary(user.getId(), user.getUsername(), user.getName(),
                user.getEmail(), user.getIsActive(), user.getCreatedAt());
    }

    public void deleteAdmin(Long id, Long requesterId) {
        if (id.equals(requesterId)) {
            throw new AppException("You cannot delete your own admin account", HttpStatus.BAD_REQUEST);
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("Admin not found", HttpStatus.NOT_FOUND));
        if (user.getRole() != User.Role.ADMIN) {
            throw new AppException("User is not an admin", HttpStatus.BAD_REQUEST);
        }
        long adminCount = userRepository.countByRole(User.Role.ADMIN);
        if (adminCount <= 1) {
            throw new AppException("Cannot delete the last remaining admin account", HttpStatus.BAD_REQUEST);
        }
        userRepository.delete(user);
        log.info("Admin deleted: " + id);
    }

    // ── Activate / Deactivate ─────────────────────────────────────────────

    public void setActive(Long userId, boolean active) {
        if (!userRepository.existsById(userId)) {
            throw new AppException("User not found", HttpStatus.NOT_FOUND);
        }
        userRepository.updateActive(userId, active);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String makeUniqueUsername(String base) {
        // Sanitize: keep only alphanumeric, dots, underscores
        String clean = base.toLowerCase().replaceAll("[^a-z0-9._]", "");
        if (clean.isEmpty()) clean = "user";

        if (!userRepository.existsByUsername(clean)) return clean;

        int i = 1;
        while (userRepository.existsByUsername(clean + i)) i++;
        return clean + i;
    }

    private AuthDto.LoginResponse buildLoginResponse(User user, String access, String refresh) {
        AuthDto.UserInfo info = new AuthDto.UserInfo(
                user.getId(), user.getUsername(), user.getName(), user.getEmail(),
                user.getRole(), user.getReferenceId(), user.getMustChangePassword());

        return new AuthDto.LoginResponse(
                access, refresh, "Bearer", jwtService.getExpiration(), info);
    }
}
