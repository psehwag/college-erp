# Smart College ERP Lite
### AI-Based Face Recognition Attendance System — Backend

A production-ready microservices backend built with **Java 17 + Spring Boot 3.2 + Spring Cloud**.

---

## Architecture Overview

```
React Frontend
      │
      ▼
┌─────────────────────────────────────────┐
│           API Gateway  :8080            │  ◄─ JWT validation, routing, CORS
└─────────────────────────────────────────┘
      │
      ├── /api/auth/**        → Auth Service        :8081
      ├── /api/students/**    → Student Service     :8082
      ├── /api/faculty/**     → Faculty Service     :8083
      ├── /api/attendance/**  → Attendance Service  :8084
      ├── /api/marks/**       → Marks Service       :8085
      ├── /api/courses/**     → Course Service      :8086
      ├── /api/notifications/** → Notification Svc  :8087
      ├── /api/parents/**     → Parent Service      :8088
      ├── /api/admin/**       → Admin Service       :8089
      └── /api/face/**        → Face Recognition    :8090

All services register with:
  Eureka Discovery Server  :8761

Async events via:
  Apache Kafka  :9092
  Topics: attendance-events | marks-events | general-events

Databases:
  MySQL :3306  (one schema per service)
```

---

## Service Port Map

| Service                | Port | Database         |
|------------------------|------|------------------|
| Eureka Server          | 8761 | —                |
| API Gateway            | 8080 | —                |
| Auth Service           | 8081 | erp_auth         |
| Student Service        | 8082 | erp_students     |
| Faculty Service        | 8083 | erp_faculty      |
| Attendance Service     | 8084 | erp_attendance   |
| Marks Service          | 8085 | erp_marks        |
| Course Service         | 8086 | erp_courses      |
| Notification Service   | 8087 | erp_notifications|
| Parent Service         | 8088 | erp_parents      |
| Admin Service          | 8089 | —                |
| Face Recognition       | 8090 | erp_face         |
| Kafka UI               | 8888 | —                |

---

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- MySQL 8.0 (if running locally without Docker)

---

## Quick Start

### Option A — Docker Compose (recommended)

```bash
# 1. Clone and navigate
git clone <repo-url> && cd college-erp

# 2. (Optional) Set email credentials for notifications
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password

# 3. Start everything
docker-compose up -d

# 4. Watch startup
docker-compose logs -f
```

Wait ~90 seconds for all services to register with Eureka.

### Option B — Local Development

```bash
# Start infrastructure only
docker-compose up -d mysql zookeeper kafka kafka-ui

# Build all modules
mvn clean package -DskipTests

# Start services in order (each in a separate terminal)
cd eureka-server   && mvn spring-boot:run
cd api-gateway     && mvn spring-boot:run
cd auth-service    && mvn spring-boot:run
cd course-service  && mvn spring-boot:run
cd student-service && mvn spring-boot:run
cd faculty-service && mvn spring-boot:run
# ... etc
```

---

## Default Credentials

All default users share the password: **`Password@123`**

| Role    | Username  | Email                    |
|---------|-----------|--------------------------|
| Admin   | admin     | admin@college.edu        |
| Faculty | faculty1  | faculty1@college.edu     |
| Student | student1  | student1@college.edu     |
| Parent  | parent1   | parent1@gmail.com        |

---

## API Quick Reference

All requests go through the **API Gateway at `http://localhost:8080`**.

### Authentication

```bash
# Login
POST /api/auth/login
{
  "usernameOrEmail": "admin",
  "password": "Password@123"
}
# Returns: { "accessToken": "eyJ...", "refreshToken": "..." }

# Use the token in all subsequent requests:
Authorization: Bearer <accessToken>

# Refresh token
POST /api/auth/refresh-token
{ "refreshToken": "..." }

# Change password
POST /api/auth/change-password
{ "currentPassword": "...", "newPassword": "..." }
```

### Course Setup (Admin — do this first)

```bash
# Create department
POST /api/departments
{ "name": "Computer Science", "code": "CS" }

# Create course
POST /api/courses
{ "name": "B.Tech CSE", "code": "BTECH-CS", "departmentId": 1, "totalSemesters": 8, "durationYears": 4 }

# Create subject
POST /api/subjects
{ "name": "Data Structures", "code": "CS201", "courseId": 1, "departmentId": 1, "semester": 2, "credits": 4, "totalLectures": 60 }

# Create batch
POST /api/courses/batches
{ "name": "CS-A-2024", "courseId": 1, "departmentId": 1, "academicYear": "2024-25", "currentSemester": 1, "maxStrength": 60 }
```

### Student Management

```bash
# Add student
POST /api/students
{ "firstName": "John", "lastName": "Doe", "email": "john@college.edu",
  "departmentId": 1, "courseId": 1, "batchId": 1, "currentSemester": 1, "admissionYear": 2024 }

# Search students
GET /api/students/search?q=john&page=0&size=10

# Get students in a batch
GET /api/students/batch/1
```

### Faculty & Subject Assignment

```bash
# Add faculty
POST /api/faculty
{ "firstName": "Dr. Priya", "lastName": "Sharma", "email": "priya@college.edu", "departmentId": 1 }

# Assign faculty to subject + batch
POST /api/faculty/assignments
{ "facultyId": 1, "subjectId": 2, "batchId": 1, "academicYear": "2024-25", "semester": 1 }

# Get faculty assignments
GET /api/faculty/1/assignments
```

### Attendance (Manual)

```bash
# Mark attendance for a batch
POST /api/attendance/bulk
{
  "facultyId": 1, "subjectId": 2, "batchId": 1,
  "attendanceDate": "2024-11-15",
  "studentAttendances": [
    { "studentId": 1, "status": "PRESENT" },
    { "studentId": 2, "status": "ABSENT", "remarks": "Medical leave" }
  ]
}

# Get attendance percentage
GET /api/attendance/student/1/subject/2/percentage

# Get defaulters (below 75%)
GET /api/attendance/subject/2/defaulters?threshold=75.0
```

### Face Recognition Attendance (AI)

```bash
# Step 1 — Enroll student face (Admin)
POST /api/face/enroll/1          # multipart/form-data
  photos: [file1.jpg, file2.jpg, ..., file10.jpg]  # 5-10 photos recommended

# Step 2 — Train model for the batch (Faculty/Admin)
POST /api/face/train
{ "batchId": 1, "studentIds": [1, 2, 3, 4, 5] }

# Step 3 — Start session (Faculty)
POST /api/attendance/session/start
{ "facultyId": 1, "subjectId": 2, "batchId": 1 }
# Returns sessionToken

# Step 4 — Recognize face (per webcam frame, from frontend)
POST /api/face/recognize
{
  "batchId": 1, "subjectId": 2, "facultyId": 1,
  "sessionToken": "abc123...",
  "frameBase64": "data:image/jpeg;base64,/9j/4AAQ..."
}
# Returns: { "recognized": true, "studentId": 3, "confidenceScore": 91.5 }

# Step 5 — Mark attendance (called by frontend after recognition)
POST /api/attendance/face-recognition
{ "studentId": 3, "subjectId": 2, "facultyId": 1, "batchId": 1,
  "confidenceScore": 91.5, "sessionToken": "abc123..." }

# Step 6 — End session
PATCH /api/attendance/session/1/end
```

### Marks

```bash
# Upload marks for entire batch at once
POST /api/marks/bulk
{
  "subjectId": 2, "facultyId": 1, "batchId": 1, "semester": 1,
  "examType": "INTERNAL_1", "maxMarks": 30, "academicYear": "2024-25",
  "studentMarks": [
    { "studentId": 1, "marksObtained": 27 },
    { "studentId": 2, "marksObtained": 22, "remarks": "Absent for unit 3" }
  ]
}

# Student semester summary
GET /api/marks/student/1/semester/1/summary
```

### Dashboards

```bash
GET /api/admin/dashboard                    # Admin stats
GET /api/admin/dashboard/faculty/1          # Faculty dashboard
GET /api/admin/dashboard/student/1          # Student dashboard
GET /api/admin/reports/defaulters/subject/2 # Defaulter list
POST /api/admin/broadcast?title=Holiday&message=College closed tomorrow&targetRole=STUDENT
```

### Notifications

```bash
GET /api/notifications?page=0&size=20       # My notifications
GET /api/notifications/unread-count
PATCH /api/notifications/5/read
PATCH /api/notifications/read-all
```

---

## Swagger UI (per service)

Each service exposes its own Swagger UI when running locally:

| Service           | URL                                     |
|-------------------|-----------------------------------------|
| Auth              | http://localhost:8081/swagger-ui.html   |
| Student           | http://localhost:8082/swagger-ui.html   |
| Faculty           | http://localhost:8083/swagger-ui.html   |
| Attendance        | http://localhost:8084/swagger-ui.html   |
| Marks             | http://localhost:8085/swagger-ui.html   |
| Course            | http://localhost:8086/swagger-ui.html   |
| Notification      | http://localhost:8087/swagger-ui.html   |
| Parent            | http://localhost:8088/swagger-ui.html   |
| Admin             | http://localhost:8089/swagger-ui.html   |
| Face Recognition  | http://localhost:8090/swagger-ui.html   |

---

## Face Recognition Setup

The LBPH model requires OpenCV native libraries. The `javacv-platform` Maven dependency bundles them.

**Download the Haar cascade file:**
```bash
curl -L https://raw.githubusercontent.com/opencv/opencv/master/data/haarcascades/haarcascade_frontalface_default.xml \
  -o face-recognition-service/src/main/resources/haarcascade_frontalface_default.xml
```

**Tips for good recognition:**
- Enroll 8–10 photos per student with varied lighting/angles
- Use 200×200 grayscale internally (handled automatically)
- Confidence threshold of 80 works well; lower it (e.g. 70) for stricter matching
- Retrain the model (`POST /api/face/train`) after any new enrollments

---

## Kafka Topics

| Topic               | Producer          | Consumer             | Purpose                          |
|---------------------|-------------------|----------------------|----------------------------------|
| `attendance-events` | Attendance Svc    | Notification Svc     | Shortfall alerts to students     |
| `marks-events`      | Marks Svc         | Notification Svc     | Marks upload notifications       |
| `general-events`    | Admin Svc         | Notification Svc     | Broadcasts and announcements     |

Monitor topics at: **http://localhost:8888** (Kafka UI)

---

## Technology Stack

| Layer            | Technology                                      |
|------------------|-------------------------------------------------|
| Language         | Java 17                                         |
| Framework        | Spring Boot 3.2, Spring Cloud 2023.0            |
| Service Discovery| Netflix Eureka                                  |
| API Gateway      | Spring Cloud Gateway                            |
| Security         | Spring Security + JWT (jjwt 0.11.5)             |
| ORM              | Spring Data JPA / Hibernate                     |
| Database         | MySQL 8.0 (schema-per-service)                  |
| Messaging        | Apache Kafka                                    |
| AI / Vision      | JavaCV 1.5.9 + OpenCV (LBPH algorithm)          |
| Inter-service    | OpenFeign (sync) + Kafka (async)                |
| Documentation    | SpringDoc OpenAPI / Swagger UI                  |
| Build            | Maven (multi-module)                            |
| Containerisation | Docker + Docker Compose                         |

---

## Project Structure

```
college-erp/
├── pom.xml                        ← Parent POM (all modules declared here)
├── docker-compose.yml
├── init-db.sql
├── eureka-server/                 ← Service registry :8761
├── api-gateway/                   ← Entry point :8080, JWT filter
├── auth-service/                  ← Login/register/tokens :8081
├── student-service/               ← Student CRUD :8082
├── faculty-service/               ← Faculty + assignments :8083
├── attendance-service/            ← Attendance + sessions :8084
├── marks-service/                 ← Marks + grades :8085
├── course-service/                ← Dept/Course/Subject/Batch :8086
├── notification-service/          ← Kafka consumer + email :8087
├── parent-service/                ← Parent portal :8088
├── admin-service/                 ← Dashboard aggregation :8089
└── face-recognition-service/      ← LBPH AI model :8090
```

---

## Role-Based Access Summary

| Endpoint Area              | ADMIN | FACULTY | STUDENT | PARENT |
|----------------------------|-------|---------|---------|--------|
| User management            | ✅    | ❌      | ❌      | ❌     |
| Department/Course/Subject  | ✅    | ❌      | 👁      | ❌     |
| Student profiles           | ✅    | 👁      | Own     | Own child|
| Faculty profiles           | ✅    | Own     | ❌      | ❌     |
| Mark attendance            | ✅    | ✅      | ❌      | ❌     |
| Upload marks               | ✅    | ✅      | ❌      | ❌     |
| View own marks/attendance  | ✅    | ✅      | ✅      | ✅     |
| Face enroll/train          | ✅    | ❌      | ❌      | ❌     |
| Face recognition session   | ✅    | ✅      | ❌      | ❌     |
| Defaulter report           | ✅    | 👁      | ❌      | ❌     |
| Broadcast announcements    | ✅    | ❌      | ❌      | ❌     |

*(Role enforcement is done via the `X-User-Role` header injected by the API Gateway after JWT validation)*
