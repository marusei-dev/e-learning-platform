package com.example.elearning.controller;

import com.example.elearning.repository.AdministratorRepository;
import com.example.elearning.repository.CourseCreatorRepository;
import com.example.elearning.repository.LearnerRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalUserRoleAdvice {
    private final LearnerRepository learnerRepository;
    private final CourseCreatorRepository courseCreatorRepository;
    private final AdministratorRepository administratorRepository;

    public GlobalUserRoleAdvice(LearnerRepository learnerRepository,
                                CourseCreatorRepository courseCreatorRepository,
                                AdministratorRepository administratorRepository) {
        this.learnerRepository = learnerRepository;
        this.courseCreatorRepository = courseCreatorRepository;
        this.administratorRepository = administratorRepository;
    }

    @ModelAttribute
    public void addUserRoleAttributes(Model model, Principal principal) {
        if (principal instanceof Authentication) {
            Authentication auth = (Authentication) principal;
            if (auth.isAuthenticated() && auth.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauthUser = (OAuth2User) auth.getPrincipal();
                String email = oauthUser.getAttribute("email");

                boolean isLearner = learnerRepository.findByEmail(email).isPresent();
                boolean isCourseCreator = courseCreatorRepository.findByEmail(email).isPresent();
                boolean isAdministrator = administratorRepository.findByEmail(email).isPresent();

                model.addAttribute("isLearner", isLearner);
                model.addAttribute("isCourseCreator", isCourseCreator);
                model.addAttribute("isAdministrator", isAdministrator);
                model.addAttribute("userEmail", email);
            }
        }
    }

}
