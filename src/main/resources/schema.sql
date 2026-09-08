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

-- 2. Student (skills + preferred_role for AI engine)
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
    preferred_role VARCHAR(100) DEFAULT ''
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

-- 4. Job (required_skills + domain + job_role for AI)
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
    status         ENUM('APPLIED','SHORTLISTED','SELECTED','REJECTED') DEFAULT 'APPLIED',
    FOREIGN KEY (student_id) REFERENCES student(student_id)  ON DELETE CASCADE,
    FOREIGN KEY (job_id)     REFERENCES job(job_id)          ON DELETE CASCADE,
    UNIQUE KEY unique_application (student_id, job_id)
);
