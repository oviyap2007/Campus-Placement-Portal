package com.CampusPlacement.portal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private int studentId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "cgpa", nullable = false)
    private double cgpa;

    @Column(name = "pass_year", nullable = false)
    private int passYear;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "is_placed")
    private boolean placed = false;

    @Column(name = "skills")
    private String skills = "";

    @Column(name = "preferred_role")
    private String preferredRole = "";

    @Column(name = "profile_photo")
    private String profilePhoto = "";   // filename stored in uploads/photos/

    @Column(name = "resume_path")
    private String resumePath = "";     // filename stored in uploads/resumes/

    // ── CONSTRUCTORS ──────────────────────────────────────────────────────────
    public Student() {}

    public Student(String name, String email, String phone,
                   String department, double cgpa, int passYear,
                   String username, String password,
                   String skills, String preferredRole) {
        this.name          = name;
        this.email         = email;
        this.phone         = phone;
        this.department    = department;
        this.cgpa          = cgpa;
        this.passYear      = passYear;
        this.username      = username;
        this.password      = password;
        this.placed        = false;
        this.skills        = skills != null ? skills : "";
        this.preferredRole = preferredRole != null ? preferredRole : "";
        this.profilePhoto  = "";
        this.resumePath    = "";
    }

    // ── GETTERS ───────────────────────────────────────────────────────────────
    public int    getStudentId()     { return studentId; }
    public String getName()          { return name; }
    public String getEmail()         { return email; }
    public String getPhone()         { return phone; }
    public String getDepartment()    { return department; }
    public double getCgpa()          { return cgpa; }
    public int    getPassYear()      { return passYear; }
    public String getUsername()      { return username; }
    public String getPassword()      { return password; }
    public boolean isPlaced()        { return placed; }
    public String getSkills()        { return skills; }
    public String getPreferredRole() { return preferredRole; }
    public String getProfilePhoto()  { return profilePhoto; }
    public String getResumePath()    { return resumePath; }

    // ── SETTERS ───────────────────────────────────────────────────────────────
    public void setStudentId(int id)           { this.studentId = id; }
    public void setName(String name)           { this.name = name; }
    public void setEmail(String email)         { this.email = email; }
    public void setPhone(String phone)         { this.phone = phone; }
    public void setDepartment(String dept)     { this.department = dept; }
    public void setCgpa(double cgpa)           { this.cgpa = cgpa; }
    public void setPassYear(int passYear)      { this.passYear = passYear; }
    public void setUsername(String username)   { this.username = username; }
    public void setPassword(String password)   { this.password = password; }
    public void setPlaced(boolean placed)      { this.placed = placed; }
    public void setSkills(String skills)       { this.skills = skills; }
    public void setPreferredRole(String role)  { this.preferredRole = role; }
    public void setProfilePhoto(String photo)  { this.profilePhoto = photo; }
    public void setResumePath(String resume)   { this.resumePath = resume; }

    // ── HELPERS ───────────────────────────────────────────────────────────────
    public String[] getSkillArray() {
        if (skills == null || skills.trim().isEmpty()) return new String[0];
        return skills.split(",");
    }

    public boolean hasPhoto()  {
        return profilePhoto != null && !profilePhoto.trim().isEmpty();
    }

    public boolean hasResume() {
        return resumePath != null && !resumePath.trim().isEmpty();
    }

    public boolean validatePassword(String inputPassword) {
        return this.password != null && this.password.equals(inputPassword);
    }
}
