# Smart College ERP v2
### 4-Service Architecture · No Lombok · Full RBAC · KRaft Kafka

---

## Architecture

```
React Frontend :3000
      │
      ▼
API Gateway :8080  (JWT validation, routing, CORS)
      │
      ├──► college-erp-service :8081  (all business logic, MySQL erp_main)
      └──► notification-service :8082 (Kafka consumer, email, MySQL erp_notifications)

Service discovery: Eureka :8761
Kafka (KRaft, no Zookeeper): :9092
Kafka UI: :8888
```

---

## Services

| Service | Port | Database |
|---|---|---|
| Eureka Server | 8761 | — |
| API Gateway | 8080 | — |
| college-erp-service | 8081 | erp_main |
| notification-service | 8082 | erp_notifications |
| Kafka UI | 8888 | — |

---

## Quick Start

```bash
# From college-erp-v2 folder:
docker-compose up -d

# Watch logs
docker-compose logs -f college-erp-service
```

Wait ~3 minutes on first start. Then:

```bash
# Test login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"Password@123"}'
```

---

## Default Credentials

All default password: **Password@123**

| Role | Username | Note |
|---|---|---|
| ADMIN | admin | Can create all users |
| FACULTY | faculty1, faculty2 | Must change password on login |
| STUDENT | student1 | Must change password on login |
| PARENT | parent1 | Must change password on login |

---

## Role-Based Access Control

| Action | ADMIN | FACULTY | STUDENT | PARENT |
|---|---|---|---|---|
| Create student | ✅ | ❌ | ❌ | ❌ |
| Create faculty | ✅ | ❌ | ❌ | ❌ |
| Create parent | ✅ | ❌ | ❌ | ❌ |
| Create admin user | ✅ | ❌ | ❌ | ❌ |
| Reset any user's password | ✅ | ❌ | ❌ | ❌ |
| Change own password | ✅ | ✅ | ✅ | ✅ |
| View all students | ✅ | ✅ | Own only | Own child |
| Mark attendance | ✅ | ✅ | ❌ | ❌ |
| Upload marks | ✅ | ✅ | ❌ | ❌ |
| View marks/attendance | ✅ | ✅ | Own only | Own child |
| Admin dashboard | ✅ | ❌ | ❌ | ❌ |
| Broadcast notifications | ✅ | ❌ | ❌ | ❌ |
| Face enroll/train | ✅ | ❌ | ❌ | ❌ |

---

## Key API Endpoints

### Auth (public)
```
POST /api/auth/login
POST /api/auth/refresh-token
```

### Auth (authenticated)
```
POST /api/auth/logout
POST /api/auth/change-password          — any user, own password
POST /api/auth/admin/reset-password     — ADMIN only
POST /api/auth/admin/create-admin       — ADMIN only
PATCH /api/auth/admin/users/{id}/active — ADMIN only
```

### Students (ADMIN creates, others view)
```
POST   /api/students          — ADMIN only (auto-creates login)
GET    /api/students          — ADMIN, FACULTY
GET    /api/students/{id}     — ADMIN, FACULTY, own STUDENT
PUT    /api/students/{id}     — ADMIN, own STUDENT
DELETE /api/students/{id}     — ADMIN only
GET    /api/students/search?q=
GET    /api/students/batch/{batchId}
GET    /api/students/parent/{parentId}
```

### Faculty (ADMIN creates)
```
POST   /api/faculty           — ADMIN only (auto-creates login)
GET    /api/faculty           — ADMIN, FACULTY
PUT    /api/faculty/{id}      — ADMIN, own FACULTY
DELETE /api/faculty/{id}      — ADMIN only
POST   /api/faculty/assignments        — ADMIN only
GET    /api/faculty/{id}/assignments   — ADMIN, FACULTY
```

### Parents (ADMIN creates)
```
POST   /api/parents           — ADMIN only (auto-creates login)
GET    /api/parents/{id}      — ADMIN, own PARENT
GET    /api/parents/me        — PARENT (own profile)
PUT    /api/parents/{id}      — ADMIN, own PARENT
DELETE /api/parents/{id}      — ADMIN only
```

### Courses (ADMIN manages, others read)
```
POST /api/departments         — ADMIN only
GET  /api/departments         — all
POST /api/courses             — ADMIN only
POST /api/subjects            — ADMIN only
POST /api/courses/batches     — ADMIN only
GET  /api/courses/{id}/subjects?semester=1
GET  /api/departments/{id}/batches
```

### Attendance
```
POST  /api/attendance/bulk               — ADMIN, FACULTY
POST  /api/attendance/face               — ADMIN, FACULTY
POST  /api/attendance/session/start      — ADMIN, FACULTY
PATCH /api/attendance/session/{id}/end  — ADMIN, FACULTY
GET   /api/attendance/student/{id}      — own STUDENT, ADMIN, FACULTY
GET   /api/attendance/student/{id}/subject/{id}/percentage
GET   /api/attendance/subject/{id}/defaulters  — ADMIN, FACULTY
```

### Marks
```
POST /api/marks               — ADMIN, FACULTY
POST /api/marks/bulk          — ADMIN, FACULTY
GET  /api/marks/student/{id}  — own STUDENT, PARENT, ADMIN, FACULTY
GET  /api/marks/student/{id}/semester/{sem}/summary
GET  /api/marks/batch/{id}/subject/{id}?examType=INTERNAL_1
```

### Admin
```
GET  /api/admin/dashboard                          — ADMIN only
GET  /api/admin/reports/defaulters/subject/{id}    — ADMIN only
POST /api/admin/broadcast?title=&message=&targetRole=  — ADMIN only
```

### Notifications
```
GET   /api/notifications?page=0&size=20
GET   /api/notifications/unread-count
PATCH /api/notifications/{id}/read
PATCH /api/notifications/read-all
```

---

## Auto-Generated Credentials

When admin creates a student, faculty, or parent, a login account is automatically created:

| Person created | Login username | Default password | Must change? |
|---|---|---|---|
| Student | Enrollment number (e.g. ENR2601000001) | Password@123 | Yes |
| Faculty | Employee ID (e.g. FAC010001) | Password@123 | Yes |
| Parent | Email prefix (e.g. john.doe from john.doe@gmail.com) | Password@123 | Yes |

The response from the create endpoint includes the generated username:
```json
{
  "message": "Student created. Login: ENR2601000001 | Default password: Password@123",
  "data": { ... }
}
```

---

## Seed Data (2026)

On first startup, the following data is seeded automatically:

**Departments:** CS, IT, EC, MECH

**Courses:** B.Tech CS, B.Tech IT, M.Tech CS, B.Tech Mech

**Subjects:** 24 subjects across Semesters 1–6 for B.Tech CS

**Batches (as of 2026):**
- CS-A-2026, CS-B-2026 → Semester 1 (just started)
- CS-A-2025, CS-B-2025 → Semester 3 (second year)
- CS-A-2024 → Semester 5 (third year)
- CS-A-2023 → Semester 7 (final year)

---

## Docker Commands

```bash
docker-compose up -d          # Start all
docker-compose down           # Stop all (keeps data)
docker-compose down -v        # Stop and delete all data
docker-compose logs -f erp-main       # Watch main service
docker-compose logs -f erp-notification  # Watch notifications
docker-compose ps             # Check status
```

---

## Swagger UI

```
http://localhost:8081/swagger-ui.html   — Main ERP service
http://localhost:8082/swagger-ui.html   — Notification service
http://localhost:8761                    — Eureka dashboard (eureka/eureka-secret)
http://localhost:8888                    — Kafka UI
```
