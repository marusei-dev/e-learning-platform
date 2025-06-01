package com.example.elearning.controller;

import com.example.elearning.model.Administrator;
import com.example.elearning.model.Course;
import com.example.elearning.model.CourseCreator;
import com.example.elearning.model.Learner;
import com.example.elearning.repository.AdministratorRepository;
import com.example.elearning.repository.CourseCreatorRepository;
import com.example.elearning.repository.LearnerRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.security.Principal;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class RegistrationController {

    private final LearnerRepository learnerRepository;
    private final CourseCreatorRepository courseCreatorRepository;
    private final AdministratorRepository administratorRepository;

    public RegistrationController(LearnerRepository learnerRepository, CourseCreatorRepository courseCreatorRepository, AdministratorRepository administratorRepository) {
        this.learnerRepository = learnerRepository;
        this.courseCreatorRepository = courseCreatorRepository;
        this.administratorRepository = administratorRepository;
    }

    @GetMapping("/choose-role")
    public String chooseRole() {
        return "choose-role";
    }

    @PostMapping("/choose-role")
    public String saveRole(@RequestParam String role, Principal principal) {
        OAuth2User oauthUser = (OAuth2User) ((Authentication) principal).getPrincipal();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        if ("learner".equals(role)) {
            Learner learner = new Learner();
            learner.setEmail(email);
            learner.setName(name);
            learnerRepository.save(learner);
        } else if ("courseCreator".equals(role)) {
            CourseCreator creator = new CourseCreator();
            creator.setEmail(email);
            creator.setName(name);
            courseCreatorRepository.save(creator);
        }

        return "redirect:/";
    }

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        OAuth2User oauthUser = (OAuth2User) ((Authentication) principal).getPrincipal();
        String email = oauthUser.getAttribute("email");

        Optional<CourseCreator> creatorOptional = courseCreatorRepository.findByEmailWithCourses(email);
        Optional<Administrator> administratorOptional = administratorRepository.findByEmail(email);
        Optional<Learner> learnerOptional = learnerRepository.findByEmailWithCompletedCourses(email);

        boolean isCourseCreator = creatorOptional.isPresent();
        boolean isAdministrator = administratorOptional.isPresent();
        boolean isLearner = learnerOptional.isPresent();

        model.addAttribute("email", email);
        model.addAttribute("isCourseCreator", isCourseCreator);
        model.addAttribute("isAdministrator", isAdministrator);
        model.addAttribute("isLearner", isLearner);

        if (isCourseCreator) {
            model.addAttribute("myCourses", creatorOptional.get().getCourses());
        }

        if (isLearner) {
            Learner learner = learnerOptional.get();
            Set<Long> completedCourseIds = learner.getCompletedCourses().stream()
                    .map(Course::getId)
                    .collect(Collectors.toSet());

            model.addAttribute("myEnrolledCourses", learner.getEnrolledCourses());
            model.addAttribute("completedCourseIds", completedCourseIds);
        }

        return "profile";
    }
}