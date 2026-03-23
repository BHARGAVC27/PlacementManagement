# PlacementManagement Fresh Start Guide

This guide helps a new teammate run the project from scratch on Windows using PowerShell.

## 1) Prerequisites

- Git installed
- Java 21+ installed
- Maven available (or use included Maven wrapper)
- Internet access to reach Neon PostgreSQL

## 2) Clone and checkout the backend branch

Open **Terminal 1 (PowerShell)**:

```powershell
git clone https://github.com/BHARGAVC27/PlacementManagement.git
cd PlacementManagement
git fetch --all --prune
git switch --track origin/feature/campus-placement-backend
```

## 3) Configure backend environment

Still in **Terminal 1**, create `backend/.env`:

```powershell
Set-Location backend
Copy-Item .env.example .env
notepad .env
```

Set these keys in `.env`:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION`

Example shape:

```dotenv
DB_URL=jdbc:postgresql://<host>/<database>?sslmode=require&channelBinding=require
DB_USERNAME=<username>
DB_PASSWORD=<password>
JWT_SECRET=<long-random-secret>
JWT_EXPIRATION=86400000
```

## 4) Start backend

In **Terminal 1**:

```powershell
Set-Location ..\backend
.\mvnw.cmd spring-boot:run
```

Expected:

- App starts on `http://localhost:8080`
- Flyway migrations complete successfully

## 5) Start frontend (JavaFX)

Open **Terminal 2 (PowerShell)**:

```powershell
cd <path-to-your-clone>\PlacementManagement\frontend
mvn javafx:run
```

If `mvn` is unavailable, use Maven wrapper if added in future. Current frontend module expects Maven in PATH.

## 6) Quick API verification (optional)

Open **Terminal 3 (PowerShell)**:

```powershell
Invoke-WebRequest -UseBasicParsing -Uri http://localhost:8080/v3/api-docs -Method Get | Select-Object StatusCode
```

Expected status: `200`

## 7) Demo login credentials

Use these app logins:

- Admin: `admin@placement.com` / `admin123`
- Student: `student@placement.com` / `student123`

## 8) Useful test login commands

```powershell
# Admin login
$admin = @{ email='admin@placement.com'; password='admin123' } | ConvertTo-Json
Invoke-RestMethod -Uri http://localhost:8080/api/auth/login -Method Post -ContentType 'application/json' -Body $admin

# Student login
$student = @{ email='student@placement.com'; password='student123' } | ConvertTo-Json
Invoke-RestMethod -Uri http://localhost:8080/api/auth/login -Method Post -ContentType 'application/json' -Body $student
```

## 9) Stop services

- Backend: `Ctrl + C` in Terminal 1
- Frontend: `Ctrl + C` in Terminal 2

## Notes

- Do not commit `.env` (it is gitignored).
- Keep database and JWT secrets out of source control.
