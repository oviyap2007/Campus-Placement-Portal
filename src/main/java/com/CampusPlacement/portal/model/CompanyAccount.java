package com.CampusPlacement.portal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_account")
public class CompanyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private int accountId;

    @Column(name = "company_id", nullable = false)
    private int companyId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "is_approved")
    private boolean approved = false;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt = LocalDateTime.now();

    // Transient — filled from join
    @Transient private String companyName;
    @Transient private String industry;
    @Transient private String location;
    @Transient private String contactEmail;

    public CompanyAccount() {}

    public CompanyAccount(int companyId, String username, String password) {
        this.companyId     = companyId;
        this.username      = username;
        this.password      = password;
        this.approved      = false;
        this.registeredAt  = LocalDateTime.now();
    }

    // Getters
    public int getAccountId()         { return accountId; }
    public int getCompanyId()          { return companyId; }
    public String getUsername()        { return username; }
    public String getPassword()        { return password; }
    public boolean isApproved()        { return approved; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public String getCompanyName()     { return companyName; }
    public String getIndustry()        { return industry; }
    public String getLocation()        { return location; }
    public String getContactEmail()    { return contactEmail; }

    // Setters
    public void setAccountId(int accountId)            { this.accountId = accountId; }
    public void setCompanyId(int companyId)            { this.companyId = companyId; }
    public void setUsername(String username)           { this.username = username; }
    public void setPassword(String password)           { this.password = password; }
    public void setApproved(boolean approved)          { this.approved = approved; }
    public void setRegisteredAt(LocalDateTime dt)      { this.registeredAt = dt; }
    public void setCompanyName(String companyName)     { this.companyName = companyName; }
    public void setIndustry(String industry)           { this.industry = industry; }
    public void setLocation(String location)           { this.location = location; }
    public void setContactEmail(String contactEmail)   { this.contactEmail = contactEmail; }
}
