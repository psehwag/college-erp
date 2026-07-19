package com.erp.auth.service;

import com.erp.auth.dto.AuthDto;
import com.erp.auth.entity.User;
import com.erp.auth.exception.AuthException;
import com.erp.auth.repository.UserRepository;
import com.erp.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthDto.LoginResponse login(AuthDto.LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new AuthException("Invalid username/email or password");
        }

        User user = userRepository.findByUsernameOrEmail(
                request.getUsernameOrEmail(),
                request.getUsernameOrEmail()
        ).orElseThrow(() -> new AuthException("User not found"));

        if (!user.getIsActive()) {
            throw new AuthException("Account is deactivated. Contact administrator.");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        userRepository.updateRefreshToken(user.getId(), refreshToken);
        userRepository.updateLastLogin(user.getId(), LocalDateTime.now());

        log.info("User logged in: {}", user.getUsername());

        return AuthDto.LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration())
                .user(mapToUserInfo(user))
                .build();
    }

    public AuthDto.LoginResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : User.Role.STUDENT)
                .referenceId(request.getReferenceId())
                .isActive(true)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        userRepository.updateRefreshToken(user.getId(), refreshToken);

        return AuthDto.LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration())
                .user(mapToUserInfo(user))
                .build();
    }

    public AuthDto.LoginResponse refreshToken(AuthDto.RefreshTokenRequest request) {
        User user = userRepository.findByRefreshToken(request.getRefreshToken())
                .orElseThrow(() -> new AuthException("Invalid refresh token"));

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        userRepository.updateRefreshToken(user.getId(), newRefreshToken);

        return AuthDto.LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration())
                .user(mapToUserInfo(user))
                .build();
    }

    public void logout(Long userId) {
        userRepository.updateRefreshToken(userId, null);
        log.info("User logged out, id: {}", userId);
    }

    public void changePassword(Long userId, AuthDto.ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AuthException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user id: {}", userId);
    }

    private AuthDto.UserInfo mapToUserInfo(User user) {
        return AuthDto.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .referenceId(user.getReferenceId())
                .build();
    }
}
