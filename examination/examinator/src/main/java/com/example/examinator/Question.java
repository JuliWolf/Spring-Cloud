package com.example.examinator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author JuliWolf
 * @date 28.05.2023
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {
  private String question;
  private String answer;
}
