package com.example.examinator;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author JuliWolf
 * @date 28.05.2023
 */
@RestController
public class ExamController {

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private DiscoveryClient discoveryClient;

  @PostMapping("/exam")
  public Exam getExam (@RequestBody Map<String, Integer> spec) {
    List<Section> sections = spec.entrySet().stream()
        .map(this::getUrl)
        .map(url -> restTemplate.getForObject(url, Question[].class))
        .map(Arrays::asList)
        .map(Section::new)
        .collect(Collectors.toList());

    return Exam.builder()
        .title("EXAM")
        .sections(sections)
        .build();
  }

  private String getUrl(Map.Entry<String, Integer> entry) {
    return "http://"+entry.getKey()+"/api/questions?amount="+entry.getValue();
  }
}
