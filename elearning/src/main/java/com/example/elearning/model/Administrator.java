package com.example.elearning.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Administrator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @OneToMany(mappedBy = "approvedBy")
    private List<Course> approvedCourses;

    public Long getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public List<Course> getApprovedCourses() { return approvedCourses; }
}
