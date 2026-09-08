package com.CampusPlacement.portal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "company")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private int companyId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "industry")
    private String industry;

    @Column(name = "location")
    private String location;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    // CONSTRUCTORS
    public Company() {}

    public Company(String name, String industry, String location,
                   String contactEmail, String contactPhone) {
        this.name         = name;
        this.industry     = industry;
        this.location     = location;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
    }

    // GETTERS
    public int getCompanyId()       { return companyId; }
    public String getName()         { return name; }
    public String getIndustry()     { return industry; }
    public String getLocation()     { return location; }
    public String getContactEmail() { return contactEmail; }
    public String getContactPhone() { return contactPhone; }

    // SETTERS
    public void setCompanyId(int companyId)       { this.companyId = companyId; }
    public void setName(String name)              { this.name = name; }
    public void setIndustry(String industry)      { this.industry = industry; }
    public void setLocation(String location)      { this.location = location; }
    public void setContactEmail(String email)     { this.contactEmail = email; }
    public void setContactPhone(String phone)     { this.contactPhone = phone; }
}