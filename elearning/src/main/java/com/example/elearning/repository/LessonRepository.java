package com.example.elearning.repository;

import com.example.elearning.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    @Query("SELECT l FROM Lesson l WHERE l.approvedBy IS NOT NULL")
    List<Lesson> findAllApproved();

    @Query("SELECT l FROM Lesson l WHERE l.approvedBy IS NULL AND l.isRejected = false")
    List<Lesson> findAllUnapproved();
}