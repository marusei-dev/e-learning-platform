package com.example.elearning.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.elearning.model.Course;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.courseCreator WHERE c.id = :id")
    Optional<Course> findByIdWithCreator(Long id);

    @Transactional
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.learners WHERE c.id = :id")
    Optional<Course> findByIdWithLearners(Long id);

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.lessons WHERE c.id = :id")
    Optional<Course> findByIdWithLessons(Long id);

    @Query("SELECT c FROM Course c WHERE c.approvedBy IS NOT NULL")
    List<Course> findAllApproved();

    @Query("SELECT c FROM Course c WHERE c.approvedBy IS NULL AND c.isRejected = false")
    List<Course> findAllUnapproved();

    @Query("SELECT c FROM Course c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) AND c.approvedBy IS NOT NULL")
    List<Course> findByTitleContainingIgnoreCase(@Param("query") String query);
}
