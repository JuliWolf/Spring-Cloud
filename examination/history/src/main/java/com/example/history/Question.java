package com.example.history;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
@Entity
public class Question {
  @Id
  @GeneratedValue
  private Integer id;

  private String question;
  private String answer;
}