package com.placement.tracker.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "student_profiles")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "current_cgpa", nullable = false, precision = 4, scale = 2)
    private BigDecimal currentCgpa;

    @Column(name = "tenth_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal tenthPercent;

    @Column(name = "twelfth_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal twelfthPercent;

    @Column(name = "active_backlogs", nullable = false)
    private Integer activeBacklogs;

    @Column(name = "branch", nullable = false)
    private String branch;

    @Column(name = "resume_url")
    private String resumeUrl;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    public StudentProfile() {
    }

    public StudentProfile(Long id, BigDecimal currentCgpa, BigDecimal tenthPercent, BigDecimal twelfthPercent,
                          Integer activeBacklogs, String branch, String resumeUrl, Student student) {
        this.id = id;
        this.currentCgpa = currentCgpa;
        this.tenthPercent = tenthPercent;
        this.twelfthPercent = twelfthPercent;
        this.activeBacklogs = activeBacklogs;
        this.branch = branch;
        this.resumeUrl = resumeUrl;
        this.student = student;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getCurrentCgpa() {
        return currentCgpa;
    }

    public void setCurrentCgpa(BigDecimal currentCgpa) {
        this.currentCgpa = currentCgpa;
    }

    public BigDecimal getTenthPercent() {
        return tenthPercent;
    }

    public void setTenthPercent(BigDecimal tenthPercent) {
        this.tenthPercent = tenthPercent;
    }

    public BigDecimal getTwelfthPercent() {
        return twelfthPercent;
    }

    public void setTwelfthPercent(BigDecimal twelfthPercent) {
        this.twelfthPercent = twelfthPercent;
    }

    public Integer getActiveBacklogs() {
        return activeBacklogs;
    }

    public void setActiveBacklogs(Integer activeBacklogs) {
        this.activeBacklogs = activeBacklogs;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getResumeUrl() {
        return resumeUrl;
    }

    public void setResumeUrl(String resumeUrl) {
        this.resumeUrl = resumeUrl;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }
}
