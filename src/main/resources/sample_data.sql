-- ================================================
-- AI-Based Campus Placement Portal - Sample Data
-- Run AFTER schema.sql
-- ================================================

USE campus_placement;

-- Admin / Placement Officer
INSERT IGNORE INTO placement_officer (name, email, username, password) VALUES
('Admin User',    'admin@campus.edu',  'admin',  'admin123'),
('Dr. Priya Nair','priya@campus.edu',  'priya',  'priya123');

-- Companies
INSERT IGNORE INTO company (name, industry, location, contact_email, contact_phone) VALUES
('TCS',              'IT Services',       'Chennai',   'hr@tcs.com',       '044-12345678'),
('Infosys',          'IT Services',       'Bangalore', 'hr@infosys.com',   '080-87654321'),
('Wipro',            'IT Consulting',     'Pune',      'hr@wipro.com',     '020-11223344'),
('Cognizant',        'IT Services',       'Chennai',   'hr@cognizant.com', '044-55667788'),
('HCL Technologies', 'Software',          'Noida',     'hr@hcl.com',       '0120-9988776'),
('Zoho Corp',        'Software Products', 'Chennai',   'hr@zoho.com',      '044-22334455');

-- Jobs
INSERT IGNORE INTO job (company_id, title, description, salary, min_cgpa, deadline, is_active, required_skills, domain, job_role) VALUES
(1, 'Java Backend Developer',    'Build scalable backend APIs',         700000,  7.0, '2026-12-31', TRUE, 'Java,SQL,Spring Boot,REST API', 'Software Development', 'Backend Developer'),
(1, 'Data Analyst',              'Analyse business data and reports',   550000,  6.5, '2026-12-31', TRUE, 'SQL,Python,Excel,Tableau',      'Data Analytics',       'Data Analyst'),
(2, 'Software Engineer',         'Full lifecycle software development',  650000,  6.0, '2026-12-31', TRUE, 'Java,DSA,Git',                  'Software Development', 'Software Engineer'),
(2, 'Machine Learning Engineer', 'Build ML models for products',        900000,  8.0, '2026-12-31', TRUE, 'Python,ML,TensorFlow,NumPy',    'Artificial Intelligence', 'ML Engineer'),
(3, 'Full Stack Developer',      'React + Spring Boot web development',  750000,  7.0, '2026-12-31', TRUE, 'Java,React,HTML,CSS,REST API',  'Web Development',      'Full Stack Developer'),
(4, 'Python Developer',          'Backend development with Python',      620000,  6.5, '2026-12-31', TRUE, 'Python,SQL,REST API,Git',       'Software Development', 'Backend Developer'),
(5, 'DevOps Engineer',           'CI/CD pipeline and cloud infra',       800000,  7.5, '2026-12-31', TRUE, 'Git,Linux,Docker,Python',       'Cloud & DevOps',       'DevOps Engineer'),
(6, 'Frontend Developer',        'Build modern UI with React',           680000,  6.5, '2026-12-31', TRUE, 'HTML,CSS,JavaScript,React,Bootstrap', 'Web Development', 'Frontend Developer');

-- Students
INSERT IGNORE INTO student (name, email, phone, department, cgpa, pass_year, username, password, is_placed, skills, preferred_role) VALUES
('Oviya R',       'oviya@student.edu',   '9876543210', 'CSE', 8.7, 2025, 'oviya',   'oviya123',   FALSE, 'Java,SQL,Spring Boot,DSA,Git',    'Backend Developer'),
('Arun Kumar',    'arun@student.edu',    '9876543211', 'CSE', 7.5, 2025, 'arun',    'arun123',    FALSE, 'Python,ML,TensorFlow,NumPy,SQL',  'ML Engineer'),
('Priya Devi',    'priya@student.edu',   '9876543212', 'IT',  8.2, 2025, 'priyad',  'priya123',   FALSE, 'HTML,CSS,JavaScript,React',       'Frontend Developer'),
('Rahul Singh',   'rahul@student.edu',   '9876543213', 'CSE', 6.8, 2025, 'rahul',   'rahul123',   FALSE, 'Java,SQL,DSA',                    'Software Engineer'),
('Sneha Patel',   'sneha@student.edu',   '9876543214', 'ECE', 7.1, 2025, 'sneha',   'sneha123',   FALSE, 'C,Embedded Systems,VLSI',         'Embedded Engineer'),
('Karthik M',     'karthik@student.edu', '9876543215', 'IT',  9.0, 2025, 'karthik', 'karthik123', FALSE, 'Java,Spring Boot,React,SQL,Git,REST API', 'Full Stack Developer'),
('Deepa N',       'deepa@student.edu',   '9876543216', 'CSE', 7.8, 2025, 'deepa',   'deepa123',   FALSE, 'Python,SQL,Excel,Tableau',        'Data Analyst'),
('Vijay R',       'vijay@student.edu',   '9876543217', 'CSE', 6.2, 2025, 'vijay',   'vijay123',   FALSE, 'Java,DSA,Git',                    'Software Engineer');
