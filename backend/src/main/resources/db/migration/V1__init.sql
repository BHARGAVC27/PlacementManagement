-- Creates base users table for shared authentication fields.
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    user_type VARCHAR(50) NOT NULL,
    CONSTRAINT chk_users_role CHECK (role IN ('STUDENT', 'ADMIN', 'PLACEMENT_OFFICER'))
);

-- Creates students table for Student-specific fields (JOINED inheritance child of users).
CREATE TABLE students (
    id BIGINT PRIMARY KEY,
    usn VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    CONSTRAINT fk_students_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

-- Creates admins table for Admin-specific fields (JOINED inheritance child of users).
CREATE TABLE admins (
    id BIGINT PRIMARY KEY,
    employee_id VARCHAR(50) NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    CONSTRAINT fk_admins_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

-- Creates student_profiles table for academic/profile data linked one-to-one with a student.
CREATE TABLE student_profiles (
    id BIGSERIAL PRIMARY KEY,
    current_cgpa NUMERIC(4,2) NOT NULL,
    tenth_percent NUMERIC(5,2) NOT NULL,
    twelfth_percent NUMERIC(5,2) NOT NULL,
    active_backlogs INT NOT NULL,
    branch VARCHAR(100) NOT NULL,
    resume_url VARCHAR(500),
    student_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_student_profiles_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

-- Creates job_posts table for company openings and eligibility criteria.
CREATE TABLE job_posts (
    id BIGSERIAL PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    role_description TEXT NOT NULL,
    package_lpa NUMERIC(8,2) NOT NULL,
    deadline DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    min_cgpa NUMERIC(4,2) NOT NULL,
    min_10th NUMERIC(5,2) NOT NULL,
    min_12th NUMERIC(5,2) NOT NULL,
    max_backlogs INT NOT NULL,
    admin_id BIGINT NOT NULL,
    CONSTRAINT fk_job_posts_admin FOREIGN KEY (admin_id) REFERENCES admins(id) ON DELETE RESTRICT,
    CONSTRAINT chk_job_posts_status CHECK (status IN ('OPEN', 'CLOSED', 'ONGOING', 'RESULTS_OUT'))
);

-- Creates join table for allowed branches per job post.
CREATE TABLE job_post_allowed_branches (
    job_post_id BIGINT NOT NULL,
    branch VARCHAR(100) NOT NULL,
    PRIMARY KEY (job_post_id, branch),
    CONSTRAINT fk_allowed_branches_job_post FOREIGN KEY (job_post_id) REFERENCES job_posts(id) ON DELETE CASCADE
);

-- Creates applications table for student applications to jobs.
CREATE TABLE applications (
    id BIGSERIAL PRIMARY KEY,
    applied_date TIMESTAMP NOT NULL,
    current_status VARCHAR(50) NOT NULL,
    remarks VARCHAR(1000),
    student_id BIGINT NOT NULL,
    job_post_id BIGINT NOT NULL,
    CONSTRAINT fk_applications_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_applications_job_post FOREIGN KEY (job_post_id) REFERENCES job_posts(id) ON DELETE CASCADE,
    CONSTRAINT chk_applications_status CHECK (current_status IN ('APPLIED', 'ELIGIBLE', 'SHORTLISTED_OA', 'OA_CLEARED', 'INTERVIEW_SCHEDULED', 'OFFERED', 'REJECTED'))
);

-- Creates job_rounds table for round-by-round process details of each job.
CREATE TABLE job_rounds (
    id BIGSERIAL PRIMARY KEY,
    round_name VARCHAR(255) NOT NULL,
    scheduled_time TIMESTAMP NOT NULL,
    venue_or_link VARCHAR(500) NOT NULL,
    instructions TEXT,
    job_post_id BIGINT NOT NULL,
    CONSTRAINT fk_job_rounds_job_post FOREIGN KEY (job_post_id) REFERENCES job_posts(id) ON DELETE CASCADE
);

-- Creates index to speed up user lookup by email.
CREATE INDEX idx_users_email ON users(email);

-- Creates index to speed up student lookup by USN.
CREATE INDEX idx_students_usn ON students(usn);

-- Creates index to speed up filtering applications by job post.
CREATE INDEX idx_applications_job_post_id ON applications(job_post_id);

-- Creates index to speed up filtering applications by student.
CREATE INDEX idx_applications_student_id ON applications(student_id);

-- Creates index to speed up filtering rounds by job post.
CREATE INDEX idx_job_rounds_job_post_id ON job_rounds(job_post_id);
