package com.example.elearning.controller;

import org.springframework.web.bind.annotation.*;
import com.example.elearning.model.Learner;
import com.example.elearning.repository.LearnerRepository;

@RestController
@RequestMapping("/learners")
public class LearnerController {
    private final LearnerRepository learnerRepository;

    public LearnerController(LearnerRepository learnerRepository) {
        this.learnerRepository = learnerRepository;
    }
}
