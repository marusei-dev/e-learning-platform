package com.example.elearning.repository;

import com.example.elearning.model.Course;
import com.example.elearning.model.Learner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LearnerRepository extends JpaRepository<Learner, Long> {
    // Fetch learner with completed lessons
    @Query("SELECT l FROM Learner l LEFT JOIN FETCH l.completedLessons WHERE l.email = :email")
    Optional<Learner> findByEmailWithCompletedLessons(@Param("email") String email);

    @Query("SELECT l FROM Learner l LEFT JOIN FETCH l.completedCourses WHERE l.email = :email")
    Optional<Learner> findByEmailWithCompletedCourses(@Param("email") String email);

    @Query("SELECT l FROM Learner l WHERE l.email = :email")
    Optional<Learner> findByEmail(String email);

    // Check course completion status without fetching the entire collection
    @Query("SELECT CASE WHEN :course MEMBER OF l.completedCourses THEN true ELSE false END " +
            "FROM Learner l WHERE l = :learner")
    boolean isCourseCompleted(@Param("learner") Learner learner, @Param("course") Course course);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END " +
            "FROM Learner l " +
            "JOIN l.completedLessons cl " +
            "WHERE l.email = :email AND cl.id = :lessonId")
    boolean hasCompletedLesson(@Param("email") String email, @Param("lessonId") Long lessonId);
}