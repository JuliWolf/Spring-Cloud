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

