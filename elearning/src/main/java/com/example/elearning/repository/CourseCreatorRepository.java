package com.example.elearning.repository;

import com.example.elearning.model.CourseCreator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CourseCreatorRepository extends JpaRepository<CourseCreator, Long> {
    Optional<CourseCreator> findByEmail(String email);

    @Query("SELECT cc FROM CourseCreator cc LEFT JOIN FETCH cc.courses WHERE cc.email = :email")
    Optional<CourseCreator> findByEmailWithCourses(String email);
}