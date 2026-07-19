-- =====================================================
--  Smart College ERP Lite — Database Initialization
--  Creates all service schemas. Tables are managed
--  by Hibernate (ddl-auto=update). This script only
--  creates databases and inserts seed data.
-- =====================================================

CREATE DATABASE IF NOT EXISTS erp_auth       CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS erp_students   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS erp_faculty    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS erp_attendance CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS erp_marks      CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS erp_courses    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS erp_notifications CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS erp_parents    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS erp_face       CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ── Seed: Auth Users (passwords are BCrypt of "Password@123") ──────────────
USE erp_auth;

-- NOTE: Tables are created by Hibernate on first boot.
-- This procedure waits and retries until the table exists, then inserts seed data.
DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS seed_users()
BEGIN
  DECLARE done INT DEFAULT 0;
  REPEAT
    IF EXISTS (SELECT 1 FROM information_schema.tables
               WHERE table_schema = 'erp_auth' AND table_name = 'users') THEN
      SET done = 1;
    ELSE
      DO SLEEP(2);
    END IF;
  UNTIL done END REPEAT;

  -- Admin user
  INSERT IGNORE INTO users (username, email, password, role, is_active, is_email_verified)
  VALUES
    ('admin',   'admin@college.edu',   '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCAgIMTQm0KkHXsYe9B0i2e', 'ADMIN',   true, true),
    ('faculty1','faculty1@college.edu','$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCAgIMTQm0KkHXsYe9B0i2e', 'FACULTY', true, true),
    ('student1','student1@college.edu','$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCAgIMTQm0KkHXsYe9B0i2e', 'STUDENT', true, true),
    ('parent1', 'parent1@gmail.com',   '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCAgIMTQm0KkHXsYe9B0i2e', 'PARENT',  true, true);
END$$

DELIMITER ;

-- ── Seed: Departments & Courses ────────────────────────────────────────────
USE erp_courses;

-- Departments seeded via Hibernate after boot using data.sql approach,
-- or insert directly here after tables are created.
-- For demo purposes, the application's CommandLineRunner (see DataSeeder.java)
-- handles this more cleanly.
