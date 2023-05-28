# Микросервис математических задач

## Контроллер
- Контроллер будет возвращать список вопросов

```
@RestController
@RequestMapping("/api")
public class MathController {

  @Autowired
  private MathService mathService;

  @GetMapping("/questions")
  public List<Question> getRandomQuestions (@RequestParam int amount) {
    List<Question> questions = new ArrayList<>();

    for (int i = 0; i < amount; i++) {
      questions.add(mathService.getRandom());
    }

    return questions;
  }
}
```

## Сервис для генерации вопросов

```
@Service
public class MathService {
  private Random random = new Random();

  private int max = 10;

  public Question getRandom () {
    int a = random.nextInt(max);
    int b = random.nextInt(max);

    return Question
        .builder()
        .question(a + " + " + b + " = ?")
        .answer(String.valueOf(a+b))
        .build();
  }
}
```

# Микросервис вопросов по истории

## Entity

```
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
```

## Repo будет реализован через spring-data
```
public interface QuestionRepo extends JpaRepository<Question, Integer> {

}
```

## Контроллер
```
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
```

# Discovery microservice 
- Сервис для управления микросервисами

- Основной зависимостью сервиса будет являться eureka
```
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

## Активации Eureka
- Добавляем аннотацию `@EnableEurekaServer` над основным классом приложения
```
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryMicroserviceApplication {

  public static void main(String[] args) {
    SpringApplication.run(DiscoveryMicroserviceApplication.class, args);
  }

}
```

- Добавляем настройки в application.properties</br>
По дефолту все микросервисы будут искать eureka на порту 8761
```
server.port=8761
eureka.client.register-with-eureka=false
eureka.server.enable-self-preservation=false
eureka.client.fetch-registry=false
```

# Подключение клиентов к eureka

Для автоматического подключения необходимо добавить зависимость eureka-client
```
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

- Для указания названия приложения необходимо добавить spring.application.name в `application.properties`
```
spring.application.name=mathematics
```

# Examinator

Создаем еще 1 клиент

## Класс экзамена
```
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Exam {
  private String title;
  private List<Section> sections;
}
```

## Класс вопроса
```
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {
  private String question;
  private String answer;
}
```

## Класс секции экзамена
```
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Section {
  private List<Question> questions;
}
```

## Контроллер

- `DiscoveryClient` это бин, который нам добавляет eureka при подключении либы клиента 
- `RestTemplate` по заданному адресу получает данные из других микросервисов

```
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
```

## Добавляем `@LoadBalanced` для балансировки нескольких инстансов
```
  @Bean
  @LoadBalanced
  public RestTemplate restTemplate (RestTemplateBuilder restTemplateBuilder) {
    return restTemplateBuilder.build();
  }
```