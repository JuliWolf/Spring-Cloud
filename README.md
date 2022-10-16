# PART 1

## Создание и настройка проекта Limits (I)

1. Создать проект с помощью https://start.spring.io/
2. Выбрать в Dependencies 
 - Spring Boot Devtools
 - Spring Web
 - Spring Boot Actuator
 - Config Client

3. Создать Контроллер для примера в папке contrllers
4. Добавить контроллеру mapping для получения данных по адресу ("/limits")
5. Создать bean/entity класс для get запроса "/limits"
6. Создать класс Configuration для получения данных из `application.properties` с использованием @ConfigurationProperties({property-name}) ("limits-service")
7. Добавить в файл `application.properties` значения 
```
	limits-service.minimum=3
	limits-service.maximum=997
```	
8. Запустить приложение и проверить что данные корректно возвращаются в виде json
```
{
	minLimit: 3,
	maxLimit: 997
}
```


## Создание и настройка локального гит репозитория (II

1. Создать папку на устройстве
2. В созданной папке создать файл `limits-service.properties`
3. Перенести настройки из проекта I в созданный файл
```
limits-service.minimum=3
limits-service.maximum=997
```
4. В созданной папке выполнить команду `git init`
5. Выполнить команду `git commit -m "{commit message}"`



## Создание и настройка проекта Spring cloud (III)

1. Создать проект с помощью https://start.spring.io/
2. Выбрать в Dependencies 
 - Spring Boot Devtools
 - Config Client

3. В файле `application.properties` задать имя проекту

```
spring.application.name=spring-cloud-config-server
```

4. В файле `application.properties` выставить порт 8888
```
server.port=8888
```

5. В файле `application.properties` настроить путь до папки гит-пепозитория (II)
```
spring.cloud.config.server.git.uri=file:///home/juliwolf/Developing/Projects/Java/Spring-Cloud/git-localconfig-repo
```

6. В файле инициализации Spring-приложения активировать настройку `@EnableConfigServer`
```
@EnableConfigServer
@SpringBootApplication
public class SpringCloudConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudConfigServerApplication.class, args);
	}

}
```

7. Запустить приложение
8. По адресу `http://localhost:8888/limits-service/default` должен вернуться json ответ
```
{
	name: "limits-service",
	profiles: [
		"default"
	],
	label: null,
	version: "d367cb98aa71e705664eef4edf211e060fcd36c9",
	state: null,
	propertySources: [
		{
			name: "file:///home/juliwolf/Developing/Projects/Java/Spring-Cloud/git-localconfig-repo/limits-service.properties",
			source: {
				limits-service.minimum: "4",
				limits-service.maximum: "996"
			}
		}
	]
}
```

9. В приложении (I) в файле `application.properties` добавить настройки для получения данных с Spring Cloud
```
spring.application.name=limits-service
spring.config.import=optional:configserver:http://localhost:8888

```
Порт localhost должен соответствовать тому порту, что был выставлен в пункет (III.4)

10. Запустить приложение
11. По адресу `http://localhost:8080/limits` должен прийти ответ с данными из файла, созданного в пункте (II.3)


# PART 2 (создание 2х микросервисов, которые будут общаться между собой по Rest)

## Создание и настройка проекта currency-exchange-service (IV)

1. Создать проект с помощью https://start.spring.io/
2. Выбрать в Dependencies 
 - Spring Boot Devtools
 - Spring Web
 - Spring Boot Actuator
 - Config Client

3. В файле `application.properties` настроить название проекта и порт
```
spring.application.name=currency-exchange
server.port=8000
```

4. Создать Entity класс `CurrencyExchange` со следующими полями, добавить геттеры и сеттеры и конструктор
```
id
from
to
conversionMultiple
environment
```

5. Создать Контроллер для получения данных по адресу `currency-exchange/from/{from}/to/{to}`

6. Можно создать массив заглушку для возврата данных или настроить подключение к базе данных


## Создание и настройка проекта currency-conversion (V)

1. Создать проект с помощью https://start.spring.io/
2. Выбрать в Dependencies 
 - Spring Boot Devtools
 - Spring Web
 - Spring Boot Actuator
 - Config Client

3. В файле `application.properties` настроить название проекта и порт
```
spring.application.name=currency-conversion
server.port=8100
```

4. Создать Entity класс CurrencyConvertion со следующими полями, создать геттеры и сеттеры и конструктор
```
id
from
to
quantity
conversionMultiple
totalCalculatedAmount
environment
```

5. Создать Контроллер для получение данных по адресу `currency-conversion/from/{from}/to/{to}/quantity/{quantity}`

6. Контроллер будет обращаться через RestTemplate к проекту IV по адресу, указанному в пункте IV.5 и получать в ответ `ResponseEntity<CurrencyConversion> responseEntity`
```
ResponseEntity<CurrencyConversion> responseEntity = new RestTemplate()
        .getForEntity(
          "http://localhost:8000/currency-exchange/from/{from}/to/{to}",
          CurrencyConversion.class,
          uriVariables
      );
```
	
7. Создаем новый эксземпляр класса CurrencyConversion с полученными данными
```
    return new CurrencyConversion(
        currencyConversion.getId(),
        from,
        to,
        quantity,
        currencyConversion.getConversionMultiple(),
        quantity.multiply(currencyConversion.getConversionMultiple()),
        currencyConversion.getEnvironment() + " rest"
    );	
```

*** Заменить `new RestTemplate()` на `CurrencyExchangeProxy` ***

8. Добавить `dependency` в `pom.xml`
```
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-openfeign</artifactId>
		</dependency>
```

9. Создать proxy интерфейс
```
@FeignClient(name="currency-exchange", url="localhost:8000")
public interface CurrencyExchangeProxy {

  @GetMapping("currency-exchange/from/{from}/to/{to}")
  public CurrencyConversion retrieveExchangeValue (
      @PathVariable String from,
      @PathVariable String to
  );
}	
```

10. В Контроллере `CurrencyConversionController` получать данные из currency-exchange сервиса через прокси
```
CurrencyConversion currencyConversion = proxy.retrieveExchangeValue(from, to);	
``` 

# PART 3 (создание naming-server для объединения микросервисов)

## Создание и настройка проекта naming-server (VI)

1. Создать проект с помощью https://start.spring.io/
2. Выбрать в Dependencies 
 - Spring Boot Devtools
 - Spring Boot Actuator
 - Config Client
 - Eureka Server
 
3. В файле `application.properties` настроить название проекта и порт
```
spring.application.name=naming-server
server.port=8761
```

4. Запустить приложение и проверить адрес `http://localhost:8761`

5. В проекте `currency-conversion (V)` в файле `application.properties` добавить следющие настройки и запустить проект 
```
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
```

6. В проекте `currency-exchange-service (IV)` в файле `application.properties` добавить следющие настройки и запустить проект 
```
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
```

7. На странице `http://localhost:8761` в блоке `Instances currently registered with Eureka` должны появиться зарегестрированные сервисы

8. В проекте `currency-exchange-service (IV)` в файле `CurrencyExchangeProxy` убрать url и перезапустить проект
```
@FeignClient(name="currency-exchange")
```

9. Запустить проект `currency-conversion (V)` на порте 8001

10. На странице `http://localhost:8100/currency-conversion/from/USD/to/INR/quantity/10` в поле `environment` порт будет меняться при перезагрузке страницы


# PART 4 (создание api-gateway для управления эндпоинтами)

## Создание и настройка проекта api-gateway (VII)

1. Создать проект с помощью https://start.spring.io/
2. Выбрать в Dependencies 
 - Spring Boot Devtools
 - Spring Boot Actuator
 - Config Client
 - Eureka Discovery Client
 - Gateway
 
3. В файле `application.properties` настроить название проекта и порт
```
spring.application.name=api-gateway
server.port=8765
```

4. В файле `application.properties` настроить ссылку на eureka сервер
```
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
```

5. Создать класс конфигурационный класс `ApiGatewayConfiguration` для управления эндпоинтами
```
@Configuration
public class ApiGatewayConfiguration {

  @Bean
  public RouteLocator gatewayRouter (RouteLocatorBuilder builder) {
    return builder.routes().build();
  }
}
```

6. Запустить приложение и проверить работоспособность по ссылке `http://localhost:8765/CURRENCY-EXCHANGE/currency-exchange/from/USD/to/INR`

7. Чтобы изменить адрес на нижний регистр, необходимо в файле `application.properties` добавить следующие настройки
```
spring.cloud.gateway.discovery.locator.enabled=true
spring.cloud.gateway.discovery.locator.lower-case-service-id=true
```

8. В запрос можно добавить свои заголовки
```
@Configuration
public class ApiGatewayConfiguration {

  @Bean
  public RouteLocator gatewayRouter (RouteLocatorBuilder builder) {
    Function<PredicateSpec, Buildable<Route>> routerFunction
        = p -> p.path("/get")
                .filters(filter -> filter
                    .addRequestHeader("MyHeader", "MyURI")
                    .addRequestParameter("Param", "MyValue")
                )
                .uri("http://httpbin.org:80");

    return builder.routes()
        .route(routerFunction)
        .build();
  }
}
```

9. Точная настройка эндпоинтов
```
@Configuration
public class ApiGatewayConfiguration {

  @Bean
  public RouteLocator gatewayRouter (RouteLocatorBuilder builder) {
    Function<PredicateSpec, Buildable<Route>> routerFunction
        = p -> p.path("/get")
                .filters(filter -> filter
                    .addRequestHeader("MyHeader", "MyURI")
                    .addRequestParameter("Param", "MyValue")
                )
                .uri("http://httpbin.org:80");

    return builder.routes()
        .route(routerFunction)
        .route(p -> p.path("/currency-exchange/**").uri("lb://currency-exchange"))
        .route(p -> p.path("/currency-conversion/**").uri("lb://currency-conversion"))
        .route(p -> p.path("/currency-conversion-feign/**").uri("lb://currency-conversion"))
        .route(p -> p.path("/currency-conversion-new/**")
            .filters(f -> f.rewritePath("/currency-conversion-new/", "/currency-conversion-feign/"))
            .uri("lb://currency-conversion"))
        .build();
  }
}
```

Теперь `currency-exchange` сервис и `currency-conversion` сервисы доступны по следующим адресам
```
http://localhost:8765/currency-exchange/from/USD/to/INR
http://localhost:8765/currency-conversion-feign/from/USD/to/INR/quantity/10
http://localhost:8765/currency-conversion-new/from/USD/to/INR/quantity/10
```

## Настройка api-gateway логгера (VIII)
	
1. Создать класс `LoggingFilter`
```
@Component
public class LoggingFilter implements GlobalFilter {

  private Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
}	
```
	
2. Реализовать метод `filter`, где после выполнения логирования запрос все данные запроса отправляются дальше без изменений
```
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    logger.info("Path of the request received -> {}", exchange.getRequest().getPath());

    return chain.filter(exchange);
  }	
```

# PART 5 (Настройка сервиса переподключения)

1. Подключить зависимости для сервиса currency-exchange  (IV)
```
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-aop</artifactId>
	</dependency>

	<dependency>
		<groupId>io.github.resilience4j</groupId>
		<artifactId>resilience4j-spring-boot2</artifactId>
	</dependency>
```
	
2. Создать контроллер `CircuitBreakerController` с одним эндпоинтом, который будет всегда возвращать ошибку
```
@RestController
public class CircuitBreakerController {

  @GetMapping("/sample-api")
  public String sampleApi () {
    ResponseEntity<String> forEntity = new RestTemplate().getForEntity("http://localhost:8080/some-dummy-url", String.class);

    return forEntity.getBody();
  }
}
```

3. Подключить функцию переподключения к эндпоинту прежде чем вернуть ошибку 
```
@RestController
public class CircuitBreakerController {

  @GetMapping("/sample-api")
  @Retry(name="default")
  public String sampleApi () {
    ResponseEntity<String> forEntity = new RestTemplate().getForEntity("http://localhost:8080/some-dummy-url", String.class);

    return forEntity.getBody();
  }
}
```
Теперь, прежде чем вернуть ошибку метод будет вызываться 3 раза и только потом вернется ошибка запроса

4. Настройка количества запросов прежде чем вернуть ошибку.
В файле проекта `application.properties`
```
resilience4j.retry.instances.sample-api.max-attempts=5	
```

В файле класса `CircuitBreakerController` заменить значения свойства `name` 
```
@RestController
public class CircuitBreakerController {

  @GetMapping("/sample-api")
  @Retry(name="sample-api")
  public String sampleApi () {
    ResponseEntity<String> forEntity = new RestTemplate().getForEntity("http://localhost:8080/some-dummy-url", String.class);

    return forEntity.getBody();
  }
}	
```
	
Теперь будет проихсходить 5 запросов, прежде чем вернутся ошибка

5. Настройка времени ожидания между запросами
В файле проекта `application.properties`
```
resilience4j.retry.instances.sample-api.wait-duration=1s	
```

6. Настройка callback метода, в случае если метод все 5 раз возвращает ошибку
```
@RestController
public class CircuitBreakerController {

  @GetMapping("/sample-api")
  @Retry(name="sample-api", fallbackMethod = "hardcodedResponse")
  public String sampleApi () {
    ResponseEntity<String> forEntity = new RestTemplate().getForEntity("http://localhost:8080/some-dummy-url", String.class);

    return forEntity.getBody();
  }
	
  public String hardcodedResponse (Exception exc) {
    return "fallback-response";
  }
}	
```
	
Теперь после 5 запросов будет возвращаться не ошибка, а строка `fallback-response`
	
7. Настройка `CircuitBreaker`
```
@RestController
public class CircuitBreakerController {

  @GetMapping("/sample-api")
  @CircuitBreaker(name="default", fallbackMethod = "hardcodedResponse")
  public String sampleApi () {
    ResponseEntity<String> forEntity = new RestTemplate().getForEntity("http://localhost:8080/some-dummy-url", String.class);

    return forEntity.getBody();
  }
	
  public String hardcodedResponse (Exception exc) {
    return "fallback-response";
  }
}
```

При большом количестве запросов `CircuitBreaker` фиксирует, что метод возвращает ошибку в 100% случаев, после чего уходит в состояние ожидания. По истечении некоторого времени `CircuitBreaker` снова открывает эндпоин для запросов и снова собирает статистику по количеству успешных ответов. Если все ответы вернули ошибку, то эндпоинт снова становится недоступным. Больше информации тут https://sysout.ru/otkazoustojchivost-mikroservisov-shablon-circuit-breaker/

