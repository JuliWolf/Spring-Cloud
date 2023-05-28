package com.example.examinator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author JuliWolf
 * @date 28.05.2023
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Section {
  private List<Question> questions;
}
