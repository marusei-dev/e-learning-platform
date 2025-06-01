package com.example.elearning.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    private String imageUrl;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons;

    @ManyToOne
    @JoinColumn(name = "course_creator_id")
    private CourseCreator courseCreator;

    @ManyToMany(mappedBy = "enrolledCourses")
    private List<Learner> learners = new ArrayList<>();

    @ManyToMany(mappedBy = "completedCourses")
    private List<Learner> completors = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name="administrator_id")
    private Administrator approvedBy = null;

    @Column(nullable = false)
    private boolean isRejected = false;

    public String getTitle() {
        return title;
    }

    public Long getId() {
        return id;
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

    public Administrator getApprovedBy() { return approvedBy;}

    public void setApprovedBy(Administrator approvedBy) { this.approvedBy = approvedBy;}

    public void setCourseCreator(CourseCreator creator) {
        this.courseCreator = creator;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public CourseCreator getCourseCreator() {
        return courseCreator;
    }

    public List<Learner> getLearners() {
        return learners;
    }

    public void enrollLearner(Learner learner) {
        if (!this.learners.contains(learner)) {
            this.learners.add(learner);
            learner.getEnrolledCourses().add(this);
        }
    }

    public void setRejected(boolean isRejected) { this.isRejected = isRejected;}

    public boolean getIsRejected() { return isRejected;}

    public List<Learner> getCompletors() { return completors; }

    public int getApprovedLessonsCount() {
        return (int) lessons.stream()
                .filter(lesson -> lesson.getApprovedBy() != null)
                .count();
    }
}
