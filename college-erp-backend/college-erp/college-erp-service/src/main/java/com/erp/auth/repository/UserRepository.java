package com.erp.auth.repository;

import com.erp.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    Optional<User> findByRefreshToken(String token);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :t WHERE u.id = :id")
    void updateLastLogin(@Param("id") Long id, @Param("t") LocalDateTime t);

    @Modifying
    @Query("UPDATE User u SET u.refreshToken = :token WHERE u.id = :id")
    void updateRefreshToken(@Param("id") Long id, @Param("token") String token);

    @Modifying
    @Query("UPDATE User u SET u.password = :pwd, u.mustChangePassword = false WHERE u.id = :id")
    void updatePassword(@Param("id") Long id, @Param("pwd") String pwd);

    @Modifying
    @Query("UPDATE User u SET u.isActive = :active WHERE u.id = :id")
    void updateActive(@Param("id") Long id, @Param("active") Boolean active);
}
