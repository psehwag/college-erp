package com.erp.common.util;

import com.erp.common.exception.AppException;
import org.springframework.http.HttpStatus;

/**
 * Utility for enforcing role-based access control.
 * The API Gateway injects X-User-Role header after JWT validation.
 * Controllers pass this value here to enforce permissions.
 */
public class RoleGuard {

    private RoleGuard() {}

    public static void requireAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new AppException(
                "Access denied. Only ADMIN can perform this action.",
                HttpStatus.FORBIDDEN
            );
        }
    }

    public static void requireAdminOrFaculty(String role) {
        if (!"ADMIN".equals(role) && !"FACULTY".equals(role)) {
            throw new AppException(
                "Access denied. Only ADMIN or FACULTY can perform this action.",
                HttpStatus.FORBIDDEN
            );
        }
    }

    public static void requireAnyRole(String role, String... allowedRoles) {
        for (String allowed : allowedRoles) {
            if (allowed.equals(role)) return;
        }
        throw new AppException(
            "Access denied. Insufficient permissions.",
            HttpStatus.FORBIDDEN
        );
    }

    public static void requireOwnerOrAdmin(String role, Long requestedId, Long actualId) {
        if ("ADMIN".equals(role)) return;
        if (requestedId != null && requestedId.equals(actualId)) return;
        throw new AppException(
            "Access denied. You can only access your own data.",
            HttpStatus.FORBIDDEN
        );
    }
}
