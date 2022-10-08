Создание и настройка проекта Limits (I)

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

Создание и настройка локального гит репозитория (II)

1. Создать папку на устройстве
2. В созданной папке создать файл `limits-service.properties`
3. Перенести настройки из проекта I в созданный файл
```
	limits-service.minimum=3
	limits-service.maximum=997
```
4. В созданной папке выполнить команду `git init`
5. Выполнить команду `git commit -m "{commit message}"`


Создание и настройка проекта Spring cloud (III)

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

