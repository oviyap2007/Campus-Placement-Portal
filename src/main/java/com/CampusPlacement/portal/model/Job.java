package com.CampusPlacement.portal.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "job")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private int jobId;

    @Column(name = "company_id", nullable = false)
    private int companyId;

    @Transient
    private String companyName; // filled from JOIN — not in DB

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "salary")
    private double salary;

    @Column(name = "min_cgpa")
    private double minCgpa;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "required_skills")
    private String requiredSkills = "";  // e.g. "Java,SQL,Spring Boot"

    @Column(name = "domain")
    private String domain = "";          // e.g. "Software Development"

    @Column(name = "job_role")
    private String jobRole = "";         // e.g. "Backend Developer"

    // CONSTRUCTORS
    public Job() {}

    public Job(int companyId, String title, String description,
               double salary, double minCgpa, LocalDate deadline,
               String requiredSkills, String domain, String jobRole) {
        this.companyId      = companyId;
        this.title          = title;
        this.description    = description;
        this.salary         = salary;
        this.minCgpa        = minCgpa;
        this.deadline       = deadline;
        this.isActive       = true;
        this.requiredSkills = requiredSkills != null ? requiredSkills : "";
        this.domain         = domain != null ? domain : "";
        this.jobRole        = jobRole != null ? jobRole : "";
    }

    // GETTERS
    public int getJobId()             { return jobId; }
    public int getCompanyId()         { return companyId; }
    public String getCompanyName()    { return companyName; }
    public String getTitle()          { return title; }
    public String getDescription()    { return description; }
    public double getSalary()         { return salary; }
    public double getMinCgpa()        { return minCgpa; }
    public LocalDate getDeadline()    { return deadline; }
    public boolean isActive()         { return isActive; }
    public String getRequiredSkills() { return requiredSkills; }
    public String getDomain()         { return domain; }
    public String getJobRole()        { return jobRole; }

    // SETTERS
    public void setJobId(int jobId)                  { this.jobId = jobId; }
    public void setCompanyId(int companyId)          { this.companyId = companyId; }
    public void setCompanyName(String companyName)   { this.companyName = companyName; }
    public void setTitle(String title)               { this.title = title; }
    public void setDescription(String description)   { this.description = description; }
    public void setSalary(double salary)             { this.salary = salary; }
    public void setMinCgpa(double minCgpa)           { this.minCgpa = minCgpa; }
    public void setDeadline(LocalDate deadline)      { this.deadline = deadline; }
    public void setActive(boolean active)            { this.isActive = active; }
    public void setRequiredSkills(String skills)     { this.requiredSkills = skills; }
    public void setDomain(String domain)             { this.domain = domain; }
    public void setJobRole(String jobRole)           { this.jobRole = jobRole; }

    // Helper: split required skills for AI engine
    public String[] getRequiredSkillArray() {
        if (requiredSkills == null || requiredSkills.trim().isEmpty()) return new String[0];
        return requiredSkills.split(",");
    }

    public boolean isEligible(double studentCgpa) { return studentCgpa >= this.minCgpa; }

    public String getFormattedSalary() {
        if (salary >= 100000) return String.format("%.1f LPA", salary / 100000);
        return String.valueOf(salary);
    }
}