package com.example.examinator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ExaminatorApplication {

  @Bean
  public RestTemplateBuilder restTemplateBuilder() {
    return new RestTemplateBuilder();
  }


  @Bean
  @LoadBalanced
  public RestTemplate restTemplate (RestTemplateBuilder restTemplateBuilder) {
    return restTemplateBuilder.build();
  }

  public static void main(String[] args) {
    SpringApplication.run(ExaminatorApplication.class, args);
  }

}
