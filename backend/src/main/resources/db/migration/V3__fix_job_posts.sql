-- ============================================================
-- V3__fix_job_posts.sql  –  Insert missing job posts
-- ============================================================

-- Delete the test entry
DELETE FROM job_posts WHERE company_name = 'test';

-- Get the admin ID and insert all companies
INSERT INTO job_posts (company_name, role_description, package_lpa, deadline, status,
                       min_cgpa, min_10th, min_12th, max_backlogs, admin_id)
VALUES 
  ('Google',     'Software Engineer – Backend focused on distributed systems and APIs.',           22.00, '2026-04-15'::date, 'RESULTS_OUT', 8.50, 80.00, 80.00, 0, (SELECT id FROM admins LIMIT 1)),
  ('Microsoft',  'Software Development Engineer – Cloud & Azure platform team.',                  18.50, '2026-04-20'::date, 'ONGOING',     8.00, 75.00, 75.00, 0, (SELECT id FROM admins LIMIT 1)),
  ('Amazon',     'SDE-1 – Fullstack development on AWS-backed consumer products.',                16.00, '2026-04-30'::date, 'OPEN',        7.50, 70.00, 70.00, 1, (SELECT id FROM admins LIMIT 1)),
  ('Infosys',    'Systems Engineer – Enterprise Java/SpringBoot application development.',         7.00, '2026-05-10'::date, 'OPEN',        6.00, 60.00, 60.00, 2, (SELECT id FROM admins LIMIT 1)),
  ('Wipro',      'Project Engineer – Frontend and backend web development.',                       6.50, '2026-05-05'::date, 'OPEN',        6.00, 60.00, 60.00, 2, (SELECT id FROM admins LIMIT 1)),
  ('Flipkart',   'Software Engineer – Data engineering and ML platform pipelines.',               14.00, '2026-04-25'::date, 'CLOSED',      8.00, 75.00, 75.00, 0, (SELECT id FROM admins LIMIT 1));
