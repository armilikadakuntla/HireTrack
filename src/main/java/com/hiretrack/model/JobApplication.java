package com.hiretrack.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "job_applications")
public class JobApplication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String company;
    @Column(nullable = false) private String role;
    private String location;
    private String jobUrl;
    @Column(nullable = false) private String status;
    private LocalDate appliedDate;
    @Column(length = 2000) private String notes;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id") private User user;

    public JobApplication() {}
    public Long getId() { return id; }
    public String getCompany() { return company; }
    public String getRole() { return role; }
    public String getLocation() { return location; }
    public String getJobUrl() { return jobUrl; }
    public String getStatus() { return status; }
    public LocalDate getAppliedDate() { return appliedDate; }
    public String getNotes() { return notes; }
    public User getUser() { return user; }
    public void setCompany(String v) { company = v; }
    public void setRole(String v) { role = v; }
    public void setLocation(String v) { location = v; }
    public void setJobUrl(String v) { jobUrl = v; }
    public void setStatus(String v) { status = v; }
    public void setAppliedDate(LocalDate v) { appliedDate = v; }
    public void setNotes(String v) { notes = v; }
    public void setUser(User v) { user = v; }
}
