package com.placement.tracker.model.entity;

import com.placement.tracker.enums.JobStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "job_posts")
public class JobPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "role_description", nullable = false, columnDefinition = "TEXT")
    private String roleDescription;

    @Column(name = "package_lpa", nullable = false, precision = 8, scale = 2)
    private BigDecimal packageLPA;

    @Column(name = "deadline", nullable = false)
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JobStatus status;

    @Column(name = "min_cgpa", nullable = false, precision = 4, scale = 2)
    private BigDecimal minCgpa;

    @Column(name = "min_10th", nullable = false, precision = 5, scale = 2)
    private BigDecimal min10th;

    @Column(name = "min_12th", nullable = false, precision = 5, scale = 2)
    private BigDecimal min12th;

    @Column(name = "max_backlogs", nullable = false)
    private Integer maxBacklogs;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_post_allowed_branches", joinColumns = @JoinColumn(name = "job_post_id"))
    @Column(name = "branch", nullable = false)
    private Set<String> allowedBranches = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @OneToMany(mappedBy = "jobPost", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Application> applications = new ArrayList<>();

    @OneToMany(mappedBy = "jobPost", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobRound> jobRounds = new ArrayList<>();

    public JobPost() {
    }

    public JobPost(Long id, String companyName, String roleDescription, BigDecimal packageLPA, LocalDate deadline,
                   JobStatus status, BigDecimal minCgpa, BigDecimal min10th, BigDecimal min12th,
                   Integer maxBacklogs, Set<String> allowedBranches, Admin admin,
                   List<Application> applications, List<JobRound> jobRounds) {
        this.id = id;
        this.companyName = companyName;
        this.roleDescription = roleDescription;
        this.packageLPA = packageLPA;
        this.deadline = deadline;
        this.status = status;
        this.minCgpa = minCgpa;
        this.min10th = min10th;
        this.min12th = min12th;
        this.maxBacklogs = maxBacklogs;
        this.allowedBranches = allowedBranches;
        this.admin = admin;
        this.applications = applications;
        this.jobRounds = jobRounds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public void setRoleDescription(String roleDescription) {
        this.roleDescription = roleDescription;
    }

    public BigDecimal getPackageLPA() {
        return packageLPA;
    }

    public void setPackageLPA(BigDecimal packageLPA) {
        this.packageLPA = packageLPA;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public BigDecimal getMinCgpa() {
        return minCgpa;
    }

    public void setMinCgpa(BigDecimal minCgpa) {
        this.minCgpa = minCgpa;
    }

    public BigDecimal getMin10th() {
        return min10th;
    }

    public void setMin10th(BigDecimal min10th) {
        this.min10th = min10th;
    }

    public BigDecimal getMin12th() {
        return min12th;
    }

    public void setMin12th(BigDecimal min12th) {
        this.min12th = min12th;
    }

    public Integer getMaxBacklogs() {
        return maxBacklogs;
    }

    public void setMaxBacklogs(Integer maxBacklogs) {
        this.maxBacklogs = maxBacklogs;
    }

    public Set<String> getAllowedBranches() {
        return allowedBranches;
    }

    public void setAllowedBranches(Set<String> allowedBranches) {
        this.allowedBranches = allowedBranches;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    public List<JobRound> getJobRounds() {
        return jobRounds;
    }

    public void setJobRounds(List<JobRound> jobRounds) {
        this.jobRounds = jobRounds;
    }
}
