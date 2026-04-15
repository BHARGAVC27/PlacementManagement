# Campus Placement Tracker (PlacementManagement)

A Campus Placement Tracker that centralizes job postings, student applications, round scheduling, and placement reporting.
The project is split into a Spring Boot REST backend and a JavaFX desktop frontend that consumes those APIs.

## Tech stack

- Backend: Java + Spring Boot, Spring Security (JWT), Spring Data JPA, Flyway, PostgreSQL
- Frontend: JavaFX (FXML) desktop app, Java `HttpClient`
- API Docs: Swagger/OpenAPI (`/swagger-ui.html`)

## High-level architecture

- The JavaFX frontend calls the backend via HTTP (`http://localhost:8080/api/...`).
- Authentication is JWT-based. After login, the frontend stores the token and sends it as `Authorization: Bearer <token>`.
- Database schema and seed data are managed with Flyway migrations.

## Features (implemented)

### Authentication & roles

- Student registration and login
- Role-based access control (Student vs Admin/Placement Officer)
- JWT token generation and validation

### Job postings

- Admin/Placement Officer can create, update, delete job posts
- Job status workflow (OPEN/ONGOING/RESULTS_OUT/CLOSED)
- Students can browse open jobs
- Admin can view eligible students for a job

### Applications & tracking

- Students can apply to jobs
- Students can view “My Applications”
- Admin/Placement Officer can view all applications for a job
- Admin/Placement Officer can update application status (e.g., shortlisted/interview/offered/rejected)

### Rounds & interview schedule

- Admin/Placement Officer can create and list rounds per job
- Students can view their interview rounds (only for relevant shortlisted jobs)

### Reports

- Admin analytics summary (placement %, company-wise offers, branch-wise offers, package stats)

## Project structure (where to look)

### Backend (Spring Boot)

- Main app entry: backend/src/main/java/com/placement/tracker/PlacementtrackerApplication.java
- Controllers (REST endpoints): backend/src/main/java/com/placement/tracker/controller/
  - AuthController: `/api/auth/*`
  - JobPostController: `/api/jobs/*`
  - ApplicationController: `/api/applications/*`
  - JobRoundController: `/api/rounds/*`
  - StudentController: `/api/students/*`
  - ReportsController: `/api/reports/*`
- Services (business logic): backend/src/main/java/com/placement/tracker/service/
- Entities (JPA models): backend/src/main/java/com/placement/tracker/model/
- Repositories (Spring Data): backend/src/main/java/com/placement/tracker/repository/
- Security (JWT + Spring Security config): backend/src/main/java/com/placement/tracker/security/
- Configuration: backend/src/main/resources/application.properties
- DB migrations (schema + seed): backend/src/main/resources/db/migration/

### Frontend (JavaFX)

- JavaFX app entry: frontend/src/main/java/com/placement/MainApp.java
- Controllers: frontend/src/main/java/com/placement/controller/
- Screens (FXML): frontend/src/main/resources/com/placement/fxml/
- Styling: frontend/src/main/resources/com/placement/styles/
- Session state (Singleton): frontend/src/main/java/com/placement/SessionManager.java

## Getting started (Windows / PowerShell)

### 1) Prerequisites

- Java 21+ installed
- Git (if you are cloning)
- Maven available for the frontend module (backend uses Maven Wrapper)
- A PostgreSQL database (project is commonly run using Neon PostgreSQL)

### 2) Configure backend environment

The backend reads environment variables from backend/.env.

1. Copy the template file:

	- backend/.env.example -> backend/.env

	PowerShell command:

```powershell
Set-Location backend
Copy-Item .env.example .env
```

2. Set these keys in backend/.env:

	- DB_URL
	- DB_USERNAME
	- DB_PASSWORD
	- JWT_SECRET
	- JWT_EXPIRATION

Example:

```dotenv
DB_URL=jdbc:postgresql://<host>/<database>?sslmode=require&channelBinding=require
DB_USERNAME=<username>
DB_PASSWORD=<password>
JWT_SECRET=<long-random-secret>
JWT_EXPIRATION=86400000
```

### 3) Run backend

From the repository root:

```powershell
Set-Location backend
./mvnw.cmd spring-boot:run
```

Expected:

- Backend starts on http://localhost:8080
- Flyway migrations run automatically
- Swagger UI is available at http://localhost:8080/swagger-ui.html

### 4) Run frontend (JavaFX)

In a second terminal:

```powershell
Set-Location frontend
mvn javafx:run
```

The JavaFX app will open and connect to the backend at `http://localhost:8080`.

## Demo credentials

If the seed data is applied successfully, you can use:

- Admin: admin@placement.com / admin123
- Student: student@placement.com / student123

## Quick evaluator walkthrough

1. Start backend.
2. Start frontend.
3. Login as Admin to:
	- Post/manage jobs
	- View eligible students / applications
	- Create rounds and update application statuses
	- View reports dashboard
4. Login as Student to:
	- View open job drives
	- Apply to jobs
	- Track application status
	- View interview schedule (when applicable)

## Notes / troubleshooting

- If login fails with connection errors, confirm the backend is running on port 8080.
- Do not commit backend/.env (it contains secrets and is gitignored).
- If Flyway migration fails, verify DB_URL/credentials and that the database is reachable.