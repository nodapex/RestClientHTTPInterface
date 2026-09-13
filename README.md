# Spring Boot 4 HTTP Interfaces Reference Implementation

This reference implementation demonstrates the HTTP Service Client capabilities introduced in Spring Framework 7 and Spring Boot 4. 
The project illustrates how to build declarative, type-safe HTTP clients using Spring's HTTP interfaces to consume external REST APIs, exemplified through integration with the [JSONPlaceholder API](https://jsonplaceholder.typicode.com/).

## Overview

Spring's HTTP Interfaces provide a declarative mechanism for defining HTTP service clients. By annotating Java interface methods with HTTP operation annotations, Spring Framework automatically generates a proxy implementation that handles HTTP request execution and response mapping. This approach offers a type-safe, contract-based alternative to traditional HTTP client libraries, with full compile-time type checking and minimal boilerplate code.

```java
@HttpExchange("/todos")
public interface TodoService {

    @GetExchange
    List<Todo> findAll();

    @GetExchange("/{id}")
    Todo findById(@PathVariable Integer id);

    @PostExchange
    Todo create(@RequestBody Todo todo);
}
```

## Key Features

- **Declarative HTTP Client Definitions** - Define HTTP service contracts using Java interfaces with standard annotations
- **Strong Type Safety** - Compile-time type checking for HTTP requests and responses
- **Annotation-Driven Configuration** - Utilize standard Spring Web annotations: `@HttpExchange`, `@GetExchange`, `@PostExchange`, `@PutExchange`, and `@DeleteExchange`
- **RestClient Integration** - Leverages Spring's modern RestClient implementation under the hood
- **Minimal Required Configuration** - Benefit from Spring Boot's auto-configuration capabilities

## Prerequisites

- **Java 25** or higher
- **Spring Boot 4.0.0** or higher (tested with 4.0.0-RC2)
- **Spring Framework 7.0** or higher
- **Maven 3.9** or higher

## Project Structure

```
src/main/java/com/nodapex/client/
├── Application.java              # Spring Boot application entry point
├── ModernConfig.java             # Modern HTTP interface configuration
├── TraditionalConfig.java        # Traditional RestClient configuration
├── todo/
│   ├── Todo.java                 # Todo data model (record)
│   ├── TodoService.java          # HTTP interface for /todos endpoint
│   ├── TodoController.java       # REST controller exposing /api/todos endpoint
│   └── TraditionalTodoService.java # Alternative RestClient-based implementation
└── post/
    ├── Post.java                 # Post data model (record)
    ├── PostService.java          # HTTP interface for /posts endpoint
    ├── PostController.java       # REST controller exposing /api/posts endpoint
    └── TraditionalPostService.java # Alternative RestClient-based implementation

src/main/resources/
├── application.yaml              # Application configuration properties
├── todo.http                     # HTTP client test file for todos endpoint
└── post.http                     # HTTP client test file for posts endpoint
```

## Getting Started

### Step 1: Clone the Repository

```bash
git clone <repository-url>
cd RestClientHTTPInterface
```

### Step 2: Build the Project

```bash
./mvnw clean install
```

### Step 3: Run the Application

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080` and will be ready to receive requests.

## Implementation Guide

### Step 1: Define Your Data Model

Utilize Java records for immutable data transfer objects. This pattern ensures thread-safety and provides a concise syntax:

```java
public record Todo(Integer id, Integer userId, String title, boolean completed) {
}
```

### Step 2: Create an HTTP Interface

Define service operations using Spring's HTTP exchange annotations. The interface serves as a contract specification for remote API interactions:

```java
@HttpExchange("/todos")
public interface TodoService {

    @GetExchange
    List<Todo> findAll();

    @GetExchange("/{id}")
    Todo findById(@PathVariable Integer id);

    @PostExchange
    Todo create(@RequestBody Todo todo);

    @PutExchange("/{id}")
    Todo update(@PathVariable Integer id, @RequestBody Todo todo);

    @DeleteExchange("/{id}")
    void delete(@PathVariable Integer id);
}
```

### Step 3: Configure the HTTP Client

Configure HTTP service proxies through a Spring configuration class. This example demonstrates the modern approach using `@ImportHttpServices` for streamlined configuration:

```java
@Configuration
@ImportHttpServices(types = {TodoService.class, PostService.class})
public class ModernConfig {

    @Bean
    RestClientHttpServiceGroupConfigurer groupConfigurer() {
        return groups -> {
            groups.forEachClient((group, builder) -> builder
                    .baseUrl("https://jsonplaceholder.typicode.com/")
                    .build());
        };
    }
}
```

Alternatively, for traditional RestClient configuration:

```java
@Configuration
public class TraditionalConfig {

    @Bean
    RestClient jsonplaceholderRestClient() {
        return RestClient.builder()
                .baseUrl("https://jsonplaceholder.typicode.com")
                .build();
    }

    @Bean
    HttpServiceProxyFactory jsonPlaceholderProxyFactory(RestClient jsonplaceholderRestClient) {
        return HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(jsonplaceholderRestClient))
                .build();
    }

    @Bean
    TodoService todoService(HttpServiceProxyFactory jsonPlaceholderProxyFactory) {
        return jsonPlaceholderProxyFactory.createClient(TodoService.class);
    }
}
```

### Step 4: Expose HTTP Services Through a REST Controller

Create a REST controller that delegates to the HTTP service interface, effectively creating an aggregation layer:

```java
@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public List<Todo> findAll() {
        return todoService.findAll();
    }

    @GetMapping("/{id}")
    public Todo findById(@PathVariable Integer id) {
        return todoService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Todo create(@RequestBody Todo todo) {
        return todoService.create(todo);
    }

    @PutMapping("/{id}")
    public Todo update(@PathVariable Integer id, @RequestBody Todo todo) {
        return todoService.update(id, todo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        todoService.delete(id);
    }
}
```

## Available API Endpoints

### Todo Resource

| HTTP Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/todos` | Retrieve all todos |
| GET | `/api/todos/{id}` | Retrieve a specific todo by identifier |
| POST | `/api/todos` | Create a new todo entry |
| PUT | `/api/todos/{id}` | Update an existing todo |
| DELETE | `/api/todos/{id}` | Delete a todo |

### Post Resource

| HTTP Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/posts` | Retrieve all posts |
| GET | `/api/posts/{id}` | Retrieve a specific post by identifier |
| POST | `/api/posts` | Create a new post entry |
| PUT | `/api/posts/{id}` | Update an existing post |
| DELETE | `/api/posts/{id}` | Delete a post |

## HTTP Client Testing

The project includes `.http` files for testing endpoints through integrated IDE tools. These files are compatible with IntelliJ IDEA's built-in HTTP client and the VS Code REST Client extension.

### Example: todo.http

```http
### Retrieve all todos
GET http://localhost:8080/api/todos
Accept: application/json

### Retrieve a specific todo
GET http://localhost:8080/api/todos/1
Accept: application/json

### Create a new todo
POST http://localhost:8080/api/todos
Content-Type: application/json

{
  "userId": 1,
  "title": "Learn Spring Boot HTTP Interfaces",
  "completed": false
}
```

## Spring Framework 7 HTTP Service Enhancements

This reference implementation demonstrates HTTP interface capabilities introduced in Spring Framework 6 and substantially enhanced in Spring Framework 7. This section outlines the key architectural improvements:

### Service Registry Layer

Spring Framework 7 introduces a dedicated registry layer for managing `HttpServiceProxyFactory` instances, providing:

- **Configuration Model** - Declarative registration and initialization of HTTP service clients
- **Proxy Management** - Automatic creation and registration of service proxy instances as Spring-managed beans
- **Service Discovery** - Access all registered client proxies through the `HttpServiceProxyRegistry` interface

### @ImportHttpServices Annotation

Spring Framework 7 introduces the `@ImportHttpServices` annotation, enabling declarative registration of HTTP service groups with significant reduction in boilerplate configuration code.

## Reference Materials

- [HTTP Service Client Enhancements - Spring Framework Blog](https://spring.io/blog/2025/09/23/http-service-client-enhancements)
- [Spring Framework 7.0 Documentation](https://spring.io/projects/spring-framework)
- [Spring Boot 4 Documentation](https://spring.io/projects/spring-boot)
- [JSONPlaceholder - Free Fake REST API](https://jsonplaceholder.typicode.com/)
