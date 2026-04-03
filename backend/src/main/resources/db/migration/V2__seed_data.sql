-- ============================================================
-- V2__seed_data.sql  –  Dummy data for Campus Placement System
-- ============================================================
-- Password for ALL users (students + admin) is:  Test@1234
-- The hash below is BCrypt of "Test@1234"
-- ============================================================

-- ── 1. USERS (base table) ────────────────────────────────────────────────────
INSERT INTO users (email, password_hash, role, user_type) VALUES
  ('admin@placement.com',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVyIjIUK76', 'ADMIN',   'ADMIN'),
  ('arjun.sharma@rvce.edu',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVyIjIUK76', 'STUDENT', 'STUDENT'),
  ('priya.nair@rvce.edu',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVyIjIUK76', 'STUDENT', 'STUDENT'),
  ('rahul.verma@rvce.edu',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVyIjIUK76', 'STUDENT', 'STUDENT'),
  ('sneha.patil@rvce.edu',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVyIjIUK76', 'STUDENT', 'STUDENT'),
  ('karan.mehta@rvce.edu',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVyIjIUK76', 'STUDENT', 'STUDENT'),
  ('divya.krishna@rvce.edu',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVyIjIUK76', 'STUDENT', 'STUDENT'),
  ('aditya.sinha@rvce.edu',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVyIjIUK76', 'STUDENT', 'STUDENT'),
  ('meera.iyer@rvce.edu',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVyIjIUK76', 'STUDENT', 'STUDENT'),
  ('rohan.das@rvce.edu',        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVyIjIUK76', 'STUDENT', 'STUDENT'),
  ('ananya.gupta@rvce.edu',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVyIjIUK76', 'STUDENT', 'STUDENT'),
  ('vikram.reddy@rvce.edu',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVyIjIUK76', 'STUDENT', 'STUDENT'),
  ('pooja.joshi@rvce.edu',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVyIjIUK76', 'STUDENT', 'STUDENT'),
  ('nikhil.bhat@rvce.edu',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVyIjIUK76', 'STUDENT', 'STUDENT'),
  ('kavya.menon@rvce.edu',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVyIjIUK76', 'STUDENT', 'STUDENT'),
  ('saurabh.tiwari@rvce.edu',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVyIjIUK76', 'STUDENT', 'STUDENT')
ON CONFLICT (email) DO NOTHING;

-- ── 2. ADMINS ────────────────────────────────────────────────────────────────
INSERT INTO admins (id, employee_id, department)
  SELECT id, 'EMP001', 'Placement Cell'
  FROM users WHERE email = 'admin@placement.com'
ON CONFLICT DO NOTHING;

-- ── 3. STUDENTS ─────────────────────────────────────────────────────────────
INSERT INTO students (id, usn, first_name, last_name, phone)
  SELECT u.id, s.usn, s.first_name, s.last_name, s.phone
  FROM users u
  JOIN (VALUES
    ('arjun.sharma@rvce.edu',   'CS21B001', 'Arjun',   'Sharma',  '9876543210'),
    ('priya.nair@rvce.edu',     'CS21B002', 'Priya',   'Nair',    '9876543211'),
    ('rahul.verma@rvce.edu',    'IS21B003', 'Rahul',   'Verma',   '9876543212'),
    ('sneha.patil@rvce.edu',    'IS21B004', 'Sneha',   'Patil',   '9876543213'),
    ('karan.mehta@rvce.edu',    'CS21B005', 'Karan',   'Mehta',   '9876543214'),
    ('divya.krishna@rvce.edu',  'EC21B006', 'Divya',   'Krishna', '9876543215'),
    ('aditya.sinha@rvce.edu',   'CS21B007', 'Aditya',  'Sinha',   '9876543216'),
    ('meera.iyer@rvce.edu',     'CS21B008', 'Meera',   'Iyer',    '9876543217'),
    ('rohan.das@rvce.edu',      'ME21B009', 'Rohan',   'Das',     '9876543218'),
    ('ananya.gupta@rvce.edu',   'CS21B010', 'Ananya',  'Gupta',   '9876543219'),
    ('vikram.reddy@rvce.edu',   'IS21B011', 'Vikram',  'Reddy',   '9876543220'),
    ('pooja.joshi@rvce.edu',    'CS21B012', 'Pooja',   'Joshi',   '9876543221'),
    ('nikhil.bhat@rvce.edu',    'EC21B013', 'Nikhil',  'Bhat',    '9876543222'),
    ('kavya.menon@rvce.edu',    'CS21B014', 'Kavya',   'Menon',   '9876543223'),
    ('saurabh.tiwari@rvce.edu', 'IS21B015', 'Saurabh', 'Tiwari',  '9876543224')
  ) AS s(email, usn, first_name, last_name, phone)
  ON u.email = s.email
ON CONFLICT DO NOTHING;

-- ── 4. STUDENT PROFILES ──────────────────────────────────────────────────────
INSERT INTO student_profiles (current_cgpa, tenth_percent, twelfth_percent, active_backlogs, branch, resume_url, student_id)
  SELECT p.cgpa, p.tenth, p.twelfth, p.backlogs, p.branch, '', u.id
  FROM users u
  JOIN (VALUES
    ('arjun.sharma@rvce.edu',   9.10, 95.00, 92.00, 0, 'CSE'),
    ('priya.nair@rvce.edu',     8.75, 91.00, 89.00, 0, 'CSE'),
    ('rahul.verma@rvce.edu',    7.80, 85.00, 82.00, 1, 'ISE'),
    ('sneha.patil@rvce.edu',    8.40, 88.00, 86.00, 0, 'ISE'),
    ('karan.mehta@rvce.edu',    9.30, 97.00, 95.00, 0, 'CSE'),
    ('divya.krishna@rvce.edu',  7.50, 80.00, 78.00, 2, 'ECE'),
    ('aditya.sinha@rvce.edu',   8.90, 93.00, 91.00, 0, 'CSE'),
    ('meera.iyer@rvce.edu',     8.20, 87.00, 85.00, 0, 'CSE'),
    ('rohan.das@rvce.edu',      6.90, 75.00, 72.00, 3, 'ME'),
    ('ananya.gupta@rvce.edu',   9.50, 98.00, 96.00, 0, 'CSE'),
    ('vikram.reddy@rvce.edu',   7.60, 82.00, 80.00, 1, 'ISE'),
    ('pooja.joshi@rvce.edu',    8.60, 90.00, 88.00, 0, 'CSE'),
    ('nikhil.bhat@rvce.edu',    7.90, 84.00, 81.00, 0, 'ECE'),
    ('kavya.menon@rvce.edu',    9.00, 94.00, 93.00, 0, 'CSE'),
    ('saurabh.tiwari@rvce.edu', 7.20, 79.00, 76.00, 2, 'ISE')
  ) AS p(email, cgpa, tenth, twelfth, backlogs, branch)
  ON u.email = p.email
ON CONFLICT (student_id) DO NOTHING;

-- ── 5. JOB POSTS ─────────────────────────────────────────────────────────────
-- All posted by the admin (id resolved via subquery)
INSERT INTO job_posts (company_name, role_description, package_lpa, deadline, status,
                       min_cgpa, min_10th, min_12th, max_backlogs, admin_id)
SELECT *
FROM (VALUES
  ('Google',     'Software Engineer – Backend focused on distributed systems and APIs.',           22.00, '2026-04-15', 'RESULTS_OUT', 8.50, 80.00, 80.00, 0),
  ('Microsoft',  'Software Development Engineer – Cloud & Azure platform team.',                  18.50, '2026-04-20', 'ONGOING',     8.00, 75.00, 75.00, 0),
  ('Amazon',     'SDE-1 – Fullstack development on AWS-backed consumer products.',                16.00, '2026-04-30', 'OPEN',        7.50, 70.00, 70.00, 1),
  ('Infosys',    'Systems Engineer – Enterprise Java/SpringBoot application development.',         7.00, '2026-05-10', 'OPEN',        6.00, 60.00, 60.00, 2),
  ('Wipro',      'Project Engineer – Frontend and backend web development.',                       6.50, '2026-05-05', 'OPEN',        6.00, 60.00, 60.00, 2),
  ('Flipkart',   'Software Engineer – Data engineering and ML platform pipelines.',               14.00, '2026-04-25', 'CLOSED',      8.00, 75.00, 75.00, 0)
) AS v(company_name, role_description, package_lpa, deadline, status, min_cgpa, min_10th, min_12th, max_backlogs)
CROSS JOIN (SELECT id FROM admins LIMIT 1) AS a(admin_id)
ON CONFLICT DO NOTHING;

-- ── 6. ALLOWED BRANCHES ───────────────────────────────────────────────────────
-- Google: CSE, ISE only
INSERT INTO job_post_allowed_branches (job_post_id, branch)
  SELECT jp.id, b.branch FROM job_posts jp
  CROSS JOIN (VALUES ('CSE'), ('ISE')) AS b(branch)
  WHERE jp.company_name = 'Google'
ON CONFLICT DO NOTHING;

-- Microsoft: CSE, ISE, ECE
INSERT INTO job_post_allowed_branches (job_post_id, branch)
  SELECT jp.id, b.branch FROM job_posts jp
  CROSS JOIN (VALUES ('CSE'), ('ISE'), ('ECE')) AS b(branch)
  WHERE jp.company_name = 'Microsoft'
ON CONFLICT DO NOTHING;

-- Amazon: all branches
INSERT INTO job_post_allowed_branches (job_post_id, branch)
  SELECT jp.id, b.branch FROM job_posts jp
  CROSS JOIN (VALUES ('CSE'), ('ISE'), ('ECE'), ('ME'), ('AIML'), ('AIDS'), ('CSD')) AS b(branch)
  WHERE jp.company_name = 'Amazon'
ON CONFLICT DO NOTHING;

-- Infosys: all branches
INSERT INTO job_post_allowed_branches (job_post_id, branch)
  SELECT jp.id, b.branch FROM job_posts jp
  CROSS JOIN (VALUES ('CSE'), ('ISE'), ('ECE'), ('ME'), ('AIML'), ('AIDS'), ('CSD')) AS b(branch)
  WHERE jp.company_name = 'Infosys'
ON CONFLICT DO NOTHING;

-- Wipro: all branches
INSERT INTO job_post_allowed_branches (job_post_id, branch)
  SELECT jp.id, b.branch FROM job_posts jp
  CROSS JOIN (VALUES ('CSE'), ('ISE'), ('ECE'), ('ME'), ('AIML'), ('AIDS'), ('CSD')) AS b(branch)
  WHERE jp.company_name = 'Wipro'
ON CONFLICT DO NOTHING;

-- Flipkart: CSE, ISE, AIML only
INSERT INTO job_post_allowed_branches (job_post_id, branch)
  SELECT jp.id, b.branch FROM job_posts jp
  CROSS JOIN (VALUES ('CSE'), ('ISE'), ('AIML')) AS b(branch)
  WHERE jp.company_name = 'Flipkart'
ON CONFLICT DO NOTHING;


-- ── 7. APPLICATIONS ──────────────────────────────────────────────────────────
-- Google (RESULTS_OUT) — top CSE/ISE students, various final statuses
INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-01 10:00:00', 'OFFERED',   'Selected after 3 rounds', u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'arjun.sharma@rvce.edu'   AND jp.company_name = 'Google'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-01 10:05:00', 'OFFERED',   'Excellent performance in system design', u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'karan.mehta@rvce.edu'    AND jp.company_name = 'Google'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-01 10:10:00', 'OFFERED',   'Strong DSA and communication', u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'ananya.gupta@rvce.edu'   AND jp.company_name = 'Google'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-01 10:15:00', 'REJECTED',  'Did not clear final round', u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'priya.nair@rvce.edu'     AND jp.company_name = 'Google'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-01 10:20:00', 'REJECTED',  'Did not clear OA', u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'aditya.sinha@rvce.edu'   AND jp.company_name = 'Google'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-01 10:25:00', 'REJECTED',  'OA score below cutoff', u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'meera.iyer@rvce.edu'     AND jp.company_name = 'Google'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-01 10:30:00', 'OFFERED',   'Top performer in coding round', u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'kavya.menon@rvce.edu'    AND jp.company_name = 'Google'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-01 10:35:00', 'REJECTED',  'Cleared OA but rejected in HR', u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'pooja.joshi@rvce.edu'    AND jp.company_name = 'Google'
ON CONFLICT DO NOTHING;

-- Microsoft (ONGOING) — students in various pipeline stages
INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-10 09:00:00', 'INTERVIEW_SCHEDULED', 'OA cleared, interview on Apr 5', u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'arjun.sharma@rvce.edu'   AND jp.company_name = 'Microsoft'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-10 09:05:00', 'INTERVIEW_SCHEDULED', 'Shortlisted for technical interview', u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'karan.mehta@rvce.edu'    AND jp.company_name = 'Microsoft'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-10 09:10:00', 'OA_CLEARED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'priya.nair@rvce.edu'     AND jp.company_name = 'Microsoft'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-10 09:15:00', 'OA_CLEARED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'ananya.gupta@rvce.edu'   AND jp.company_name = 'Microsoft'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-10 09:20:00', 'SHORTLISTED_OA', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'aditya.sinha@rvce.edu'   AND jp.company_name = 'Microsoft'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-10 09:25:00', 'SHORTLISTED_OA', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'meera.iyer@rvce.edu'     AND jp.company_name = 'Microsoft'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-10 09:30:00', 'APPLIED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'sneha.patil@rvce.edu'    AND jp.company_name = 'Microsoft'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-10 09:35:00', 'APPLIED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'vikram.reddy@rvce.edu'   AND jp.company_name = 'Microsoft'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-10 09:40:00', 'REJECTED', 'Did not meet OA cutoff', u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'nikhil.bhat@rvce.edu'    AND jp.company_name = 'Microsoft'
ON CONFLICT DO NOTHING;

-- Amazon (OPEN) — a few applications already in
INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-18 11:00:00', 'APPLIED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'arjun.sharma@rvce.edu'   AND jp.company_name = 'Amazon'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-18 11:05:00', 'APPLIED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'priya.nair@rvce.edu'     AND jp.company_name = 'Amazon'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-18 11:10:00', 'APPLIED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'rahul.verma@rvce.edu'    AND jp.company_name = 'Amazon'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-18 11:15:00', 'APPLIED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'karan.mehta@rvce.edu'    AND jp.company_name = 'Amazon'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-18 11:20:00', 'APPLIED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'ananya.gupta@rvce.edu'   AND jp.company_name = 'Amazon'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-18 11:25:00', 'APPLIED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'kavya.menon@rvce.edu'    AND jp.company_name = 'Amazon'
ON CONFLICT DO NOTHING;

-- Infosys (OPEN) — broad intake, many students applied
INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-15 14:00:00', 'APPLIED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'rahul.verma@rvce.edu'    AND jp.company_name = 'Infosys'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-15 14:05:00', 'APPLIED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'sneha.patil@rvce.edu'    AND jp.company_name = 'Infosys'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-15 14:10:00', 'APPLIED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'divya.krishna@rvce.edu'  AND jp.company_name = 'Infosys'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-15 14:15:00', 'APPLIED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'rohan.das@rvce.edu'      AND jp.company_name = 'Infosys'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-15 14:20:00', 'APPLIED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'vikram.reddy@rvce.edu'   AND jp.company_name = 'Infosys'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-15 14:25:00', 'APPLIED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'saurabh.tiwari@rvce.edu' AND jp.company_name = 'Infosys'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-15 14:30:00', 'APPLIED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'nikhil.bhat@rvce.edu'    AND jp.company_name = 'Infosys'
ON CONFLICT DO NOTHING;

-- Wipro (OPEN)
INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-16 10:00:00', 'APPLIED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'divya.krishna@rvce.edu'  AND jp.company_name = 'Wipro'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-16 10:05:00', 'APPLIED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'rohan.das@rvce.edu'      AND jp.company_name = 'Wipro'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-16 10:10:00', 'APPLIED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'saurabh.tiwari@rvce.edu' AND jp.company_name = 'Wipro'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-16 10:15:00', 'APPLIED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'vikram.reddy@rvce.edu'   AND jp.company_name = 'Wipro'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-03-16 10:20:00', 'APPLIED', NULL, u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'rahul.verma@rvce.edu'    AND jp.company_name = 'Wipro'
ON CONFLICT DO NOTHING;

-- Flipkart (CLOSED)
INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-02-20 09:00:00', 'OFFERED',  'Excellent data pipeline knowledge', u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'ananya.gupta@rvce.edu'   AND jp.company_name = 'Flipkart'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-02-20 09:05:00', 'OFFERED',  'Strong ML background', u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'kavya.menon@rvce.edu'    AND jp.company_name = 'Flipkart'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-02-20 09:10:00', 'REJECTED', 'Did not clear technical round', u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'priya.nair@rvce.edu'     AND jp.company_name = 'Flipkart'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-02-20 09:15:00', 'REJECTED', 'OA score below threshold', u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'aditya.sinha@rvce.edu'   AND jp.company_name = 'Flipkart'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-02-20 09:20:00', 'OFFERED',  'Selected after 2 rounds', u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'karan.mehta@rvce.edu'    AND jp.company_name = 'Flipkart'
ON CONFLICT DO NOTHING;

INSERT INTO applications (applied_date, current_status, remarks, student_id, job_post_id)
SELECT '2026-02-20 09:25:00', 'REJECTED', 'Rejected in HR round', u.id, jp.id
  FROM users u, job_posts jp
  WHERE u.email = 'sneha.patil@rvce.edu'    AND jp.company_name = 'Flipkart'
ON CONFLICT DO NOTHING;


-- ── 8. JOB ROUNDS ─────────────────────────────────────────────────────────────

-- Google rounds
INSERT INTO job_rounds (round_name, scheduled_time, venue_or_link, instructions, job_post_id)
SELECT 'Online Assessment', '2026-03-05 10:00:00', 'https://hackerrank.com/google-oa-2026',
       'Complete 3 coding problems in 90 minutes. No external help allowed.',
       jp.id FROM job_posts jp WHERE jp.company_name = 'Google'
ON CONFLICT DO NOTHING;

INSERT INTO job_rounds (round_name, scheduled_time, venue_or_link, instructions, job_post_id)
SELECT 'Technical Interview 1', '2026-03-12 10:00:00', 'Google Meet — link sent via email',
       'Focus on DSA: arrays, trees, graphs. Expect 2 coding problems.',
       jp.id FROM job_posts jp WHERE jp.company_name = 'Google'
ON CONFLICT DO NOTHING;

INSERT INTO job_rounds (round_name, scheduled_time, venue_or_link, instructions, job_post_id)
SELECT 'Technical Interview 2', '2026-03-14 10:00:00', 'Google Meet — link sent via email',
       'System design round. Prepare for scalable architecture discussions.',
       jp.id FROM job_posts jp WHERE jp.company_name = 'Google'
ON CONFLICT DO NOTHING;

INSERT INTO job_rounds (round_name, scheduled_time, venue_or_link, instructions, job_post_id)
SELECT 'HR Round', '2026-03-16 14:00:00', 'Google Meet — link sent via email',
       'Cultural fit and behavioural questions. Review your resume thoroughly.',
       jp.id FROM job_posts jp WHERE jp.company_name = 'Google'
ON CONFLICT DO NOTHING;

-- Microsoft rounds
INSERT INTO job_rounds (round_name, scheduled_time, venue_or_link, instructions, job_post_id)
SELECT 'Online Assessment', '2026-03-15 10:00:00', 'https://hackerrank.com/microsoft-oa-2026',
       '2 coding problems plus 1 MCQ section. Duration 75 minutes.',
       jp.id FROM job_posts jp WHERE jp.company_name = 'Microsoft'
ON CONFLICT DO NOTHING;

INSERT INTO job_rounds (round_name, scheduled_time, venue_or_link, instructions, job_post_id)
SELECT 'Technical Interview', '2026-04-05 10:00:00', 'Microsoft Teams — link will be shared',
       'Data structures, algorithms and problem solving. Expect live coding on screen.',
       jp.id FROM job_posts jp WHERE jp.company_name = 'Microsoft'
ON CONFLICT DO NOTHING;

INSERT INTO job_rounds (round_name, scheduled_time, venue_or_link, instructions, job_post_id)
SELECT 'HR Round', '2026-04-08 14:00:00', 'Microsoft Teams — link will be shared',
       'Discuss past experience, goals and culture fit with the hiring manager.',
       jp.id FROM job_posts jp WHERE jp.company_name = 'Microsoft'
ON CONFLICT DO NOTHING;

-- Amazon rounds
INSERT INTO job_rounds (round_name, scheduled_time, venue_or_link, instructions, job_post_id)
SELECT 'Online Assessment', '2026-05-02 10:00:00', 'https://hackerrank.com/amazon-oa-2026',
       '2 coding problems plus work simulation section. Total 90 minutes.',
       jp.id FROM job_posts jp WHERE jp.company_name = 'Amazon'
ON CONFLICT DO NOTHING;

INSERT INTO job_rounds (round_name, scheduled_time, venue_or_link, instructions, job_post_id)
SELECT 'Technical Interview', '2026-05-10 10:00:00', 'Amazon Chime — link sent via email',
       'Focus on leadership principles alongside technical problem solving.',
       jp.id FROM job_posts jp WHERE jp.company_name = 'Amazon'
ON CONFLICT DO NOTHING;

-- Infosys rounds
INSERT INTO job_rounds (round_name, scheduled_time, venue_or_link, instructions, job_post_id)
SELECT 'Online Assessment', '2026-05-12 10:00:00', 'https://infytq.onwingspan.com',
       'Aptitude, logical reasoning and basic coding. Duration 120 minutes.',
       jp.id FROM job_posts jp WHERE jp.company_name = 'Infosys'
ON CONFLICT DO NOTHING;

INSERT INTO job_rounds (round_name, scheduled_time, venue_or_link, instructions, job_post_id)
SELECT 'HR Interview', '2026-05-18 10:00:00', 'Seminar Hall B — RV College of Engineering',
       'General HR questions. Carry a printed copy of your resume and all marksheets.',
       jp.id FROM job_posts jp WHERE jp.company_name = 'Infosys'
ON CONFLICT DO NOTHING;

-- Wipro rounds
INSERT INTO job_rounds (round_name, scheduled_time, venue_or_link, instructions, job_post_id)
SELECT 'Online Assessment', '2026-05-08 10:00:00', 'https://wilptest.wipro.com',
       'Verbal ability, analytical and coding sections. Duration 90 minutes.',
       jp.id FROM job_posts jp WHERE jp.company_name = 'Wipro'
ON CONFLICT DO NOTHING;

INSERT INTO job_rounds (round_name, scheduled_time, venue_or_link, instructions, job_post_id)
SELECT 'Technical Interview', '2026-05-14 10:00:00', 'Seminar Hall A — RV College of Engineering',
       'Core CS fundamentals. Be ready to explain your final year project in detail.',
       jp.id FROM job_posts jp WHERE jp.company_name = 'Wipro'
ON CONFLICT DO NOTHING;

INSERT INTO job_rounds (round_name, scheduled_time, venue_or_link, instructions, job_post_id)
SELECT 'HR Round', '2026-05-14 15:00:00', 'Seminar Hall A — RV College of Engineering',
       'Salary negotiation and joining formalities. Carry all original documents.',
       jp.id FROM job_posts jp WHERE jp.company_name = 'Wipro'
ON CONFLICT DO NOTHING;

-- Flipkart rounds
INSERT INTO job_rounds (round_name, scheduled_time, venue_or_link, instructions, job_post_id)
SELECT 'Online Assessment', '2026-02-25 10:00:00', 'https://unstop.com/flipkart-oa-2026',
       '3 coding problems. Focus on graphs and dynamic programming.',
       jp.id FROM job_posts jp WHERE jp.company_name = 'Flipkart'
ON CONFLICT DO NOTHING;

INSERT INTO job_rounds (round_name, scheduled_time, venue_or_link, instructions, job_post_id)
SELECT 'Technical Interview', '2026-03-02 10:00:00', 'Zoom — link shared via email',
       'Core CS plus data engineering concepts. Bring your laptop.',
       jp.id FROM job_posts jp WHERE jp.company_name = 'Flipkart'
ON CONFLICT DO NOTHING;

INSERT INTO job_rounds (round_name, scheduled_time, venue_or_link, instructions, job_post_id)
SELECT 'HR Round', '2026-03-04 14:00:00', 'Zoom — link shared via email',
       'Behavioural and situational questions. Be ready to discuss past projects.',
       jp.id FROM job_posts jp WHERE jp.company_name = 'Flipkart'
ON CONFLICT DO NOTHING;



