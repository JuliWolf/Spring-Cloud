package com.example.history;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JuliWolf
 * @date 28.05.2023
 */
@RestController
@RequestMapping("/api")
public class HistoryQuestionsController {

  @Autowired
  private QuestionRepo questionRepo;

  @GetMapping("/questions")
  public List<Question> getQuestions (@RequestParam int amount) {
    List<Question> questions = questionRepo.findAll();
    Collections.shuffle(questions);
    return questions.stream().limit(amount).collect(Collectors.toList());
  }
}
