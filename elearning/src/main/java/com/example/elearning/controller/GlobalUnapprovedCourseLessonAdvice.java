package com.example.elearning.controller;

import com.example.elearning.repository.CourseRepository;
import com.example.elearning.repository.LessonRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalUnapprovedCourseLessonAdvice {
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    
    public GlobalUnapprovedCourseLessonAdvice(CourseRepository courseRepository, LessonRepository lessonRepository) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
    }
    
    @ModelAttribute
    public void addUnapprovedCourseLessonCountAttribute(Model model) {
        model.addAttribute("unapprovedCourseCount", courseRepository.findAllUnapproved().size());
        model.addAttribute("unapprovedLessonCount", lessonRepository.findAllUnapproved().size());
    }
}
