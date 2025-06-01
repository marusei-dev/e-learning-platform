package com.example.elearning.repository;

import com.example.elearning.model.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AdministratorRepository extends JpaRepository<Administrator, Long> {
    @Query("SELECT a FROM Administrator a WHERE a.email = :email")
    Optional<Administrator> findByEmail(String email);
}
