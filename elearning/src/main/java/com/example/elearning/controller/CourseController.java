package com.example.elearning.controller;

import com.example.elearning.exception.CourseNotFoundException;
import com.example.elearning.exception.LearnerNotFoundException;
import com.example.elearning.model.*;
import com.example.elearning.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.*;

@Controller
public class CourseController {
    private final CourseRepository courseRepository;
    private final CourseCreatorRepository courseCreatorRepository;
    private final LearnerRepository learnerRepository;
    private final LessonRepository lessonRepository;
    private final AdministratorRepository administratorRepository;

    public CourseController(CourseRepository courseRepository, CourseCreatorRepository courseCreatorRepository, LearnerRepository learnerRepository, LessonRepository lessonRepository, AdministratorRepository administratorRepository) {
        this.courseRepository = courseRepository; this.courseCreatorRepository = courseCreatorRepository; this.learnerRepository = learnerRepository; this.lessonRepository = lessonRepository; this.administratorRepository = administratorRepository;
    }

    @GetMapping("/")
    public String getAll(Model model) {
        List<Course> approvedCourses = courseRepository.findAllApproved();
        model.addAttribute("courses", approvedCourses);
        return "index";
    }

    @GetMapping("/course/{id}")
    public String getById(@PathVariable Long id, Model model, Principal principal) {
        Course course = courseRepository.findByIdWithCreator(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid course ID"));

        model.addAttribute("course", course);

        int completionPercentage = 0;
        boolean isCourseCompleted = false;
        Map<Long, Boolean> lessonCompletionMap = new HashMap<>();
        boolean isEnrolled = false;

        if (principal != null) {
            OAuth2User oauthUser = (OAuth2User) ((Authentication) principal).getPrincipal();
            String email = oauthUser.getAttribute("email");

            // Check enrollment
            isEnrolled = course.getLearners().stream()
                    .anyMatch(l -> l.getEmail().equals(email));

            if (isEnrolled) {
                Learner learner = learnerRepository.findByEmailWithCompletedLessons(email).orElseThrow();

                // Calculate completed lessons for this course
                long completedCount = course.getLessons().stream()
                        .filter(lesson -> lesson.getApprovedBy() != null)
                        .filter(lesson -> learner.getCompletedLessons().contains(lesson))
                        .count();

                long totalLessons = course.getLessons().stream()
                        .filter(lesson -> lesson.getApprovedBy() != null)
                        .count();

                // Calculate percentage
                if(totalLessons > 0) {
                    completionPercentage = (int) Math.round((double) completedCount / totalLessons * 100);
                }

                // Check course completion using query
                boolean courseCompleted = learnerRepository.isCourseCompleted(learner, course);

                if(completedCount == totalLessons && totalLessons > 0 && !courseCompleted) {
                    learner.getCompletedCourses().add(course);
                    course.getCompletors().add(learner);
                    learnerRepository.save(learner);
                    courseRepository.save(course);
                    isCourseCompleted = true;
                } else {
                    isCourseCompleted = courseCompleted;
                }

                // Build lesson completion map
                for (Lesson lesson : course.getLessons()) {
                    lessonCompletionMap.put(lesson.getId(), learner.getCompletedLessons().contains(lesson));
                }
            }
        }

        model.addAttribute("isEnrolled", isEnrolled);
        model.addAttribute("lessonCompletionMap", lessonCompletionMap);
        model.addAttribute("completionPercentage", completionPercentage);
        model.addAttribute("isCourseCompleted", isCourseCompleted);

        return "course";
    }

    @GetMapping("/create-course")
    public String showCreateForm() {
        return "create-course";
    }

    @PostMapping("/create-course")
    public String createCourse(@RequestParam String title,
                               @RequestParam String description,
                               @RequestParam String imageUrl,
                               Principal principal) {
        OAuth2User oauthUser = (OAuth2User) ((Authentication) principal).getPrincipal();
        String email = oauthUser.getAttribute("email");

        CourseCreator creator = courseCreatorRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid course creator"));

        Course course = new Course();
        course.setTitle(title);
        course.setDescription(description);
        course.setImageUrl(imageUrl);
        course.setCourseCreator(creator);

        courseRepository.save(course);
        return "redirect:/";
    }

    @GetMapping("/course/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, Principal principal) {
        Course course = courseRepository.findById(id).orElseThrow();

        // Verify the current user is the creator
        OAuth2User oauthUser = (OAuth2User) ((Authentication) principal).getPrincipal();
        String email = oauthUser.getAttribute("email");

        if(!course.getCourseCreator().getEmail().equals(email)) {
            throw new AccessDeniedException("You don't own this course");
        }

        model.addAttribute("course", course);
        return "edit-course";
    }

    @PostMapping("/course/{id}/edit")
    public String updateCourse(@PathVariable Long id,
                               @RequestParam String title,
                               @RequestParam String description,
                               @RequestParam String imageUrl,
                               Principal principal) {
        Course course = courseRepository.findById(id).orElseThrow();

        // Verify ownership
        OAuth2User oauthUser = (OAuth2User) ((Authentication) principal).getPrincipal();
        String email = oauthUser.getAttribute("email");

        if(!course.getCourseCreator().getEmail().equals(email)) {
            throw new AccessDeniedException("You don't own this course");
        }

        course.setTitle(title);
        course.setDescription(description);
        course.setImageUrl(imageUrl);
        course.setApprovedBy(null);
        course.setRejected(false);
        courseRepository.save(course);

        return "redirect:/course/" + id;
    }

    @PostMapping("/course/{id}/delete")
    @Transactional
    public String deleteCourse(@PathVariable Long id, Principal principal) {
        Course course = courseRepository.findByIdWithLearners(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid course ID"));

        // Verify ownership
        OAuth2User oauthUser = (OAuth2User) ((Authentication) principal).getPrincipal();
        String email = oauthUser.getAttribute("email");

        if(!course.getCourseCreator().getEmail().equals(email)) {
            throw new AccessDeniedException("You don't own this course");
        }

        // Remove course from all enrolled learners
        List<Learner> learners = new ArrayList<>(course.getLearners());
        for (Learner learner : learners) {
            learner.getEnrolledCourses().remove(course);
            learnerRepository.save(learner);
        }
        course.getLearners().clear();

        List<Lesson> lessons = new ArrayList<>(course.getLessons());
        for (Lesson lesson : lessons) {

        }

        // Now delete the course
        courseRepository.delete(course);

        return "redirect:/";
    }

    @PostMapping("/course/{courseId}/enroll")
    public String enrollInCourse(@PathVariable Long courseId, Principal principal) {
        try {
            Course course = courseRepository.findByIdWithLearners(courseId)
                    .orElseThrow(() -> new CourseNotFoundException("Course not found"));

            OAuth2User oauthUser = (OAuth2User) ((Authentication) principal).getPrincipal();
            String email = oauthUser.getAttribute("email");

            Learner learner = learnerRepository.findByEmail(email)
                    .orElseThrow(() -> new LearnerNotFoundException("Learner not registered"));

            course.enrollLearner(learner);
            courseRepository.save(course);
            learnerRepository.save(learner);

            return "redirect:/course/" + courseId + "?enrolled=true";

        } catch (Exception e) {
            return "redirect:/course/" + courseId + "?error=enrollment_failed";
        }
    }

    @PostMapping("/course/{courseId}/add-lesson")
    public String addLesson(@PathVariable Long courseId,
                            @RequestParam String title,
                            @RequestParam String description,
                            @RequestParam String imageUrl,
                            @RequestParam String videoUrl,
                            Principal principal) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid course ID"));

        // Verify ownership
        OAuth2User oauthUser = (OAuth2User) ((Authentication) principal).getPrincipal();
        String email = oauthUser.getAttribute("email");

        if(!course.getCourseCreator().getEmail().equals(email)) {
            throw new AccessDeniedException("You don't own this course");
        }

        // Create and save the lesson
        Lesson lesson = new Lesson();
        lesson.setTitle(title);
        lesson.setDescription(description);
        lesson.setImageUrl(imageUrl);
        lesson.setVideoUrl(videoUrl);
        lesson.setCourse(course);

        lessonRepository.save(lesson);

        return "redirect:/course/" + courseId;
    }

    @GetMapping("/approve-courses")
    public String showApproveCourses(Model model) {
        List<Course> unapprovedCourses = courseRepository.findAllUnapproved();
        model.addAttribute("unapprovedCourses", unapprovedCourses);
        return "approve-courses";
    }

    @PostMapping("/course/{courseId}/approve")
    public String approveCourse(@PathVariable Long courseId, Principal principal) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid course ID"));

        OAuth2User oauthUser = (OAuth2User) ((Authentication) principal).getPrincipal();
        String email = oauthUser.getAttribute("email");
        Administrator admin = administratorRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Administrator not found"));

        course.setApprovedBy(admin);
        courseRepository.save(course);

        return "redirect:/approve-courses";
    }

    @PostMapping("/course/{courseId}/reject")
    public String rejectCourse(@PathVariable Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid course ID"));

        course.setRejected(true);
        courseRepository.save(course);

        return "redirect:/approve-courses";
    }

    @GetMapping("/approve-lessons")
    public String showApproveLessons(Model model) {
        List<Lesson> unapprovedLessons = lessonRepository.findAllUnapproved();
        model.addAttribute("unapprovedLessons", unapprovedLessons);
        return "approve-lessons";
    }

    @PostMapping("/lesson/{lessonId}/approve")
    public String approveLesson(@PathVariable Long lessonId, Principal principal) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid lesson ID"));

        OAuth2User oauthUser = (OAuth2User) ((Authentication) principal).getPrincipal();
        String email = oauthUser.getAttribute("email");
        Administrator admin = administratorRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Administrator not found"));

        lesson.setApprovedBy(admin);
        lessonRepository.save(lesson);

        return "redirect:/approve-lessons";
    }

    @PostMapping("/lesson/{lessonId}/reject")
    public String rejectLesson(@PathVariable Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid lesson ID"));

        lesson.setRejected(true);
        lessonRepository.save(lesson);

        return "redirect:/approve-lessons";
    }

    @GetMapping("/lesson/{id}")
    public String getLessonById(@PathVariable Long id, Model model, Principal principal) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid lesson ID"));
        model.addAttribute("lesson", lesson);

        OAuth2User oauthUser = (OAuth2User) ((Authentication) principal).getPrincipal();
        String email = oauthUser.getAttribute("email");

        // Check enrollment
        boolean isEnrolled = lesson.getCourse().getLearners().stream()
                .anyMatch(l -> l.getEmail().equals(email));
        model.addAttribute("isEnrolled", isEnrolled);

        // Check if the lesson is completed
        boolean isCompleted = false;
        if (isEnrolled) {
            isCompleted = learnerRepository.hasCompletedLesson(email, lesson.getId());
        }
        model.addAttribute("isCompleted", isCompleted);

        String videoUrl = lesson.getVideoUrl();
        String embedUrl = videoUrl.contains("youtube.com")
                ? "https://www.youtube.com/embed/" + videoUrl.split("v=")[1].split("&")[0]
                : videoUrl;
        model.addAttribute("embedUrl", embedUrl);

        return "lesson";
    }

    @PostMapping("/lesson/{id}/complete")
    public String markLessonCompleted(@PathVariable Long id, Principal principal) {
        // Get the lesson
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid lesson ID"));

        // Get the learner
        OAuth2User oauthUser = (OAuth2User) ((Authentication) principal).getPrincipal();
        String email = oauthUser.getAttribute("email");
        Learner learner = learnerRepository.findByEmail(email).orElseThrow();

        // Add learner to lesson's completors (and vice versa)
        if (!lesson.getCompletors().contains(learner)) {
            lesson.getCompletors().add(learner);
            learner.getCompletedLessons().add(lesson);
            learnerRepository.save(learner);
            lessonRepository.save(lesson);
        }

        return "redirect:/lesson/" + id;
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/search")
    public String searchCourses(@RequestParam(name = "query", required = false) String query, Model model) {
        if (query != null && !query.trim().isEmpty()) {
            List<Course> searchResults = courseRepository.findByTitleContainingIgnoreCase(query.trim());
            model.addAttribute("results", searchResults);
            model.addAttribute("searchQuery", query);
        } else {
            model.addAttribute("results", Collections.emptyList());
        }
        return "search-results";
    }
}
