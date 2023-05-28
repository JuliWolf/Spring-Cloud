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