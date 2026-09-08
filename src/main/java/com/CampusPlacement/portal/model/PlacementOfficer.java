package com.CampusPlacement.portal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "placement_officer")
public class PlacementOfficer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "officer_id")
    private int officerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    // CONSTRUCTORS
    public PlacementOfficer() {}

    public PlacementOfficer(String name, String email,
                            String username, String password) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    // GETTERS
    public int getOfficerId() { return officerId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    // SETTERS
    public void setOfficerId(int officerId) { this.officerId = officerId; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }

    // Password validation
    public boolean validatePassword(String inputPassword) {
        return this.password != null &&
                this.password.equals(inputPassword.trim());
    }
}