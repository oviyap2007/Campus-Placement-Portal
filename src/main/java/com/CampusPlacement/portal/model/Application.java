package com.CampusPlacement.portal.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "application",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id","job_id"}))
public class Application {

    public enum Status {
        APPLIED, SHORTLISTED, IN_PROGRESS, SELECTED, REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private int applicationId;

    @Column(name = "student_id", nullable = false)
    private int studentId;

    @Column(name = "job_id", nullable = false)
    private int jobId;

    @Column(name = "apply_date")
    private LocalDate applyDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.APPLIED;

    // Display fields (populated from JOIN — not stored)
    @Transient private String studentName;
    @Transient private String jobTitle;
    @Transient private String companyName;

    // CONSTRUCTORS
    public Application() {}

    public Application(int studentId, int jobId) {
        this.studentId = studentId;
        this.jobId     = jobId;
        this.applyDate = LocalDate.now();
        this.status    = Status.APPLIED;
    }

    // GETTERS
    public int getApplicationId()    { return applicationId; }
    public int getStudentId()        { return studentId; }
    public int getJobId()            { return jobId; }
    public LocalDate getApplyDate()  { return applyDate; }
    public Status getStatus()        { return status; }
    public String getStudentName()   { return studentName; }
    public String getJobTitle()      { return jobTitle; }
    public String getCompanyName()   { return companyName; }

    // SETTERS
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }
    public void setStudentId(int studentId)         { this.studentId = studentId; }
    public void setJobId(int jobId)                 { this.jobId = jobId; }
    public void setApplyDate(LocalDate applyDate)   { this.applyDate = applyDate; }
    public void setStatus(Status status)            { this.status = status; }
    public void setStudentName(String name)         { this.studentName = name; }
    public void setJobTitle(String title)           { this.jobTitle = title; }
    public void setCompanyName(String name)         { this.companyName = name; }

    // CSS badge class for status
    public String getStatusBadgeClass() {
        if (status == null) return "badge-applied";
        return switch (status) {
            case SHORTLISTED -> "badge-shortlisted";
            case IN_PROGRESS -> "badge-progress";
            case SELECTED    -> "badge-selected";
            case REJECTED    -> "badge-rejected";
            default          -> "badge-applied";
        };
    }
}