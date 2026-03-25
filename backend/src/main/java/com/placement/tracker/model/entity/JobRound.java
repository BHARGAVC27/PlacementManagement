package com.placement.tracker.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_rounds")
public class JobRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "round_name", nullable = false)
    private String roundName;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    @Column(name = "venue_or_link", nullable = false)
    private String venueOrLink;

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "job_post_id", nullable = false)
    private JobPost jobPost;

    public JobRound() {
    }

    public JobRound(Long id, String roundName, LocalDateTime scheduledTime, String venueOrLink,
                    String instructions, JobPost jobPost) {
        this.id = id;
        this.roundName = roundName;
        this.scheduledTime = scheduledTime;
        this.venueOrLink = venueOrLink;
        this.instructions = instructions;
        this.jobPost = jobPost;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoundName() {
        return roundName;
    }

    public void setRoundName(String roundName) {
        this.roundName = roundName;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getVenueOrLink() {
        return venueOrLink;
    }

    public void setVenueOrLink(String venueOrLink) {
        this.venueOrLink = venueOrLink;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public JobPost getJobPost() {
        return jobPost;
    }

    public void setJobPost(JobPost jobPost) {
        this.jobPost = jobPost;
    }
}
