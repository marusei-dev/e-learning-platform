package com.example.elearning.model;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Learner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @ManyToMany
    @JoinTable(
            name="learners_courses",
            joinColumns = @JoinColumn(name="learner_id"),
            inverseJoinColumns = @JoinColumn(name="course_id")
    )
    private List<Course> enrolledCourses = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name="learners_courses_completed",
            joinColumns = @JoinColumn(name="learner_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> completedCourses = new ArrayList<>();

    @ManyToMany(mappedBy = "completors")
    private List<Lesson> completedLessons = new ArrayList<>();

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String username) {
        this.name = username;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<Course> getEnrolledCourses() {
        return enrolledCourses;
    }

    public List<Lesson> getCompletedLessons() { return completedLessons; }

    public List<Course> getCompletedCourses() {
        return completedCourses;
    }
}
