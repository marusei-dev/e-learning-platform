package com.example.elearning.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class CourseCreator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @OneToMany(mappedBy = "courseCreator")
    private List<Course> courses;

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
