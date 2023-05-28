package com.example.history;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author JuliWolf
 * @date 28.05.2023
 */
public interface QuestionRepo extends JpaRepository<Question, Integer> {

}
