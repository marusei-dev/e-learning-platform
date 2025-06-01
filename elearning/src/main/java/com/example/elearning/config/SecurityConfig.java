package com.example.elearning.config;

import com.example.elearning.repository.CourseCreatorRepository;
import com.example.elearning.repository.LearnerRepository;
import com.example.elearning.repository.AdministratorRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final LearnerRepository learnerRepository;
    private final CourseCreatorRepository courseCreatorRepository;
    private final AdministratorRepository administratorRepository;

    public SecurityConfig(LearnerRepository learnerRepository, CourseCreatorRepository courseCreatorRepository, AdministratorRepository administratorRepository) {
        this.learnerRepository = learnerRepository;
        this.courseCreatorRepository = courseCreatorRepository;
        this.administratorRepository = administratorRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Keep public routes first
                        .requestMatchers("/choose-role", "/error").permitAll()
                        .requestMatchers("/", "/login**", "/webjars/**", "/images/**").permitAll()

                        // Specific enrollment endpoint for learners
                        .requestMatchers(HttpMethod.POST, "/course/*/enroll").hasRole("LEARNER")

                        // Allow course and lesson modification for administrators
                        .requestMatchers(HttpMethod.POST, "/course/*/approve").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/course/*/reject").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/lesson/*/approve").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/lesson/*/reject").hasRole("ADMIN")

                        // Course creation/modification
                        .requestMatchers(HttpMethod.POST, "/course/**").hasRole("COURSE_CREATOR")
                        .requestMatchers(HttpMethod.PUT, "/course/**").hasRole("COURSE_CREATOR")
                        .requestMatchers(HttpMethod.DELETE, "/course/**").hasRole("COURSE_CREATOR")
                        .requestMatchers(HttpMethod.POST, "/course/*/add-lesson").hasRole("COURSE_CREATOR")



                        // General authenticated access
                        .requestMatchers("/course/**").authenticated()

                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oauth2AuthSuccessHandler())
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/").permitAll()
                );
        return http.build();
    }

    private AuthenticationSuccessHandler oauth2AuthSuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String email = oauth2User.getAttribute("email");

            // Check for admin first
            boolean isAdmin = administratorRepository.findByEmail(email).isPresent();
            if (isAdmin) {
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                Authentication newAuth = new OAuth2AuthenticationToken(
                        oauth2User,
                        authorities,
                        ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId()
                );
                SecurityContextHolder.getContext().setAuthentication(newAuth);
                response.sendRedirect("/");
                return;
            }

            // Check existing roles
            boolean isLearner = learnerRepository.findByEmail(email).isPresent();
            boolean isCourseCreator = courseCreatorRepository.findByEmail(email).isPresent();

            if (isLearner || isCourseCreator) {
                List<GrantedAuthority> authorities = new ArrayList<>();
                if (isCourseCreator) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_COURSE_CREATOR"));
                } else {
                    authorities.add(new SimpleGrantedAuthority("ROLE_LEARNER"));
                }
                Authentication newAuth = new OAuth2AuthenticationToken(
                        oauth2User,
                        authorities,
                        ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId()
                );
                SecurityContextHolder.getContext().setAuthentication(newAuth);
                response.sendRedirect("/");
                return;
            }

            // Redirect to role selection if no roles found
            response.sendRedirect("/choose-role");
        };
    }
}