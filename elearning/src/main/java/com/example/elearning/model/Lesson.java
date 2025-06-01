package com.example.elearning.model;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    private String imageUrl;

    private String videoUrl;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name="administrator_id")
    private Administrator approvedBy = null;

    @Column(nullable = false)
    private boolean isRejected = false;

    @ManyToMany
    @JoinTable(
            name="learners_lessons",
            joinColumns = @JoinColumn(name="lesson_id"),
            inverseJoinColumns = @JoinColumn(name="learner_id")
    )
    private List<Learner> completors = new ArrayList<>();

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setApprovedBy(Administrator approvedBy) { this.approvedBy = approvedBy;}

    public Administrator getApprovedBy() { return approvedBy;}

    public boolean getIsRejected() { return isRejected;}

    public void setRejected(boolean isRejected) { this.isRejected = isRejected;}

    public List<Learner> getCompletors() {
        return completors;
    }
}
