-- ================================================
-- AI-Based Campus Placement Portal - Schema
-- Run this in MySQL Workbench ONCE to set up the DB
-- ================================================

CREATE DATABASE IF NOT EXISTS campus_placement;
USE campus_placement;

-- 1. Placement Officer (Admin)
CREATE TABLE IF NOT EXISTS placement_officer (
    officer_id INT PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(100) NOT NULL UNIQUE,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL
);

-- 2. Student
CREATE TABLE IF NOT EXISTS student (
    student_id     INT PRIMARY KEY AUTO_INCREMENT,
    name           VARCHAR(100) NOT NULL,
    email          VARCHAR(100) NOT NULL UNIQUE,
    phone          VARCHAR(15),
    department     VARCHAR(50)  NOT NULL,
    cgpa           DOUBLE       NOT NULL,
    pass_year      INT          NOT NULL,
    username       VARCHAR(50)  NOT NULL UNIQUE,
    password       VARCHAR(100) NOT NULL,
    is_placed      BOOLEAN      DEFAULT FALSE,
    skills         VARCHAR(500) DEFAULT '',
    preferred_role VARCHAR(100) DEFAULT '',
    profile_photo  VARCHAR(255) DEFAULT NULL,
    resume_path    VARCHAR(255) DEFAULT NULL
);

-- 3. Company
CREATE TABLE IF NOT EXISTS company (
    company_id    INT PRIMARY KEY AUTO_INCREMENT,
    name          VARCHAR(100) NOT NULL,
    industry      VARCHAR(100),
    location      VARCHAR(100),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(15)
);

-- 4. Job
CREATE TABLE IF NOT EXISTS job (
    job_id          INT PRIMARY KEY AUTO_INCREMENT,
    company_id      INT          NOT NULL,
    title           VARCHAR(100) NOT NULL,
    description     TEXT,
    salary          DOUBLE,
    min_cgpa        DOUBLE       DEFAULT 0.0,
    deadline        DATE,
    is_active       BOOLEAN      DEFAULT TRUE,
    required_skills VARCHAR(500) DEFAULT '',
    domain          VARCHAR(100) DEFAULT '',
    job_role        VARCHAR(100) DEFAULT '',
    FOREIGN KEY (company_id) REFERENCES company(company_id) ON DELETE CASCADE
);

-- 5. Application
CREATE TABLE IF NOT EXISTS application (
    application_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id     INT NOT NULL,
    job_id         INT NOT NULL,
    apply_date     DATE DEFAULT (CURRENT_DATE),
    status         ENUM('APPLIED','SHORTLISTED','IN_PROGRESS','SELECTED','REJECTED') DEFAULT 'APPLIED',
    FOREIGN KEY (student_id) REFERENCES student(student_id)  ON DELETE CASCADE,
    FOREIGN KEY (job_id)     REFERENCES job(job_id)          ON DELETE CASCADE,
    UNIQUE KEY unique_application (student_id, job_id)
);

-- 6. Company Account (for company-side login)
CREATE TABLE IF NOT EXISTS company_account (
    account_id    INT PRIMARY KEY AUTO_INCREMENT,
    company_id    INT          NOT NULL,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password      VARCHAR(100) NOT NULL,
    is_approved   BOOLEAN      DEFAULT FALSE,
    registered_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company(company_id) ON DELETE CASCADE
);

-- 7. Interview Round (per job — defined by company)
CREATE TABLE IF NOT EXISTS interview_round (
    round_id      INT PRIMARY KEY AUTO_INCREMENT,
    job_id        INT          NOT NULL,
    round_number  INT          NOT NULL,
    round_name    VARCHAR(100) NOT NULL,
    round_type    ENUM('RESUME_SHORTLIST','APTITUDE','ONLINE_TEST','CODING','TECHNICAL','GROUP_DISCUSSION','HR') NOT NULL,
    FOREIGN KEY (job_id) REFERENCES job(job_id) ON DELETE CASCADE,
    UNIQUE KEY unique_round (job_id, round_number)
);

-- 8. Round Result (tracks each student's progress per round)
CREATE TABLE IF NOT EXISTS round_result (
    result_id      INT PRIMARY KEY AUTO_INCREMENT,
    application_id INT          NOT NULL,
    round_id       INT          NOT NULL,
    status         ENUM('PENDING','PASSED','FAILED','NOT_STARTED') DEFAULT 'NOT_STARTED',
    remarks        VARCHAR(500) DEFAULT '',
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES application(application_id) ON DELETE CASCADE,
    FOREIGN KEY (round_id)       REFERENCES interview_round(round_id)   ON DELETE CASCADE,
    UNIQUE KEY unique_round_result (application_id, round_id)
);
