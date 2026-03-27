# Task Management API

A simplified Task Management REST API built with **Java 17** and **Spring Boot 3**, following **Domain-Driven Design (DDD)** principles and a **Test-Driven Development (TDD)** approach.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Running the Application](#running-the-application)
- [Running Tests](#running-tests)
- [API Reference](#api-reference)
- [Design Decisions](#design-decisions)

---

## Architecture Overview

The project is organised into four clearly separated layers following DDD:

| Layer | Package | Responsibility |
|---|---|---|
| **Domain** | `domain.model`, `domain.repository`, `domain.service` | Core business entities, rules, and interface contracts |
| **Application** | `application.service`, `application.dto` | Use-case orchestration, DTOs, input/output mapping |
| **Infrastructure – Persistence** | `infrastructure.persistence` | In-memory repository adapter |
| **Infrastructure – Web** | `infrastructure.web.controller`, `infrastructure.web.handler` | HTTP layer: REST controllers and global exception handling |

---

## Project Structure

```
task-management-api/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/taskmanagement/
    │   │   ├── TaskManagementApplication.java          # Entry point
    │   │   ├── domain/
    │   │   │   ├── model/
    │   │   │   │   ├── Task.java                       # Aggregate root
    │   │   │   │   └── TaskStatus.java                 # Status enum
    │   │   │   ├── repository/
    │   │   │   │   └── TaskRepository.java             # Repository port (interface)
    │   │   │   └── service/
    │   │   │       └── TaskDomainService.java          # Service interface
    │   │   ├── application/
    │   │   │   ├── dto/
    │   │   │   │   ├── CreateTaskRequest.java
    │   │   │   │   ├── UpdateTaskRequest.java
    │   │   │   │   ├── TaskResponse.java
    │   │   │   │   ├── ErrorResponse.java
    │   │   │   │   └── PagedResponse.java
    │   │   │   └── service/
    │   │   │       └── TaskService.java                # Business logic implementation
    │   │   └── infrastructure/
    │   │       ├── persistence/
    │   │       │   └── InMemoryTaskRepository.java     # In-memory store
    │   │       └── web/
    │   │           ├── controller/
    │   │           │   └── TaskController.java         # REST endpoints
    │   │           └── handler/
    │   │               ├── GlobalExceptionHandler.java
    │   │               ├── TaskNotFoundException.java
    │   │               └── InvalidTaskException.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/taskmanagement/
            ├── domain/
            │   ├── model/
            │   │   └── TaskTest.java                   # Unit: domain entity
            │   └── service/
            │       └── TaskServiceTest.java            # Unit: service (mocked repo)
            └── infrastructure/
                ├── persistence/
                │   └── InMemoryTaskRepositoryTest.java # Unit: repository
                └── web/controller/
                    └── TaskControllerIntegrationTest.java # Integration: full API
```

---

## Prerequisites

| Tool | Minimum Version |
|---|---|
| Java (JDK) | 17 |
| Maven | 3.8+ |

Verify your versions:

```bash
java -version
mvn -version
```

---

## Running the Application

### 1. Clone / unzip the project

```bash
cd task-management-api
```

### 2. Build

```bash
mvn clean package -DskipTests
```

### 3. Run

```bash
java -jar target/task-management-api-1.0.0.jar
```

The server starts on **http://localhost:8080**.

Alternatively, run without packaging:

```bash
mvn spring-boot:run
```

---

## Running Tests

### All tests (unit + integration)

```bash
mvn test
```

### A specific test class

```bash
mvn test -Dtest=TaskServiceTest
mvn test -Dtest=TaskControllerIntegrationTest
```

---

## API Reference

### Task Model

```json
{
  "id":          "uuid-string",
  "title":       "string (required)",
  "description": "string (optional)",
  "status":      "PENDING | IN_PROGRESS | DONE",
  "due_date":    "yyyy-MM-dd (required, must be a future date)"
}
```

---

### Endpoints

#### Create a Task

```
POST /tasks
Content-Type: application/json

{
  "title":       "Finish report",
  "description": "Q3 quarterly report",
  "status":      "PENDING",
  "due_date":    "2099-12-31"
}
```

**Response:** `201 Created` with the created task object.

---

#### Get a Task

```
GET /tasks/{id}
```

**Response:** `200 OK` with the task object, or `404 Not Found`.

---

#### Update a Task

```
PUT /tasks/{id}
Content-Type: application/json

{
  "title":    "Updated title",
  "status":   "IN_PROGRESS",
  "due_date": "2099-11-15"
}
```

All fields are optional — only provided fields are applied. **Response:** `200 OK` or `404 Not Found`.

---

#### Delete a Task

```
DELETE /tasks/{id}
```

**Response:** `204 No Content` or `404 Not Found`.

---

#### List All Tasks

```
GET /tasks
```

Optional query parameters:

| Parameter | Type   | Description |
|---|---|---|
| `status`  | string | Filter by `PENDING`, `IN_PROGRESS`, or `DONE` |
| `page`    | int    | Zero-based page number (requires `size`) |
| `size`    | int    | Number of tasks per page (requires `page`) |

**Examples:**

```bash
# All tasks
GET /tasks

# Filter by status
GET /tasks?status=PENDING

# Paginate (page 0, 5 per page)
GET /tasks?page=0&size=5

# Combined
GET /tasks?status=IN_PROGRESS&page=0&size=10
```

Without pagination, the response is a JSON array. With pagination, it returns a paginated object:

```json
{
  "content":       [...],
  "page":          0,
  "size":          5,
  "totalElements": 12
}
```

---

### Error Response Format

All errors return:

```json
{
  "status":    400,
  "error":     "Bad Request",
  "message":   "due_date must be a future date",
  "timestamp": "2099-01-01T10:00:00Z"
}
```

---

## Design Decisions

**DDD Layers** — The domain layer (`Task`, `TaskRepository` interface, `TaskDomainService` interface) is completely free of Spring annotations, making it portable and independently testable.

**Repository as Port** — `TaskRepository` is a domain interface. `InMemoryTaskRepository` is the infrastructure adapter. Swapping in a JPA or MongoDB implementation requires zero changes to business logic.

**Service owns validation** — Domain rules (future dates, mandatory fields) live in `TaskService`, not in the HTTP layer, so they're enforced regardless of the entry point.

**TDD approach** — Tests were written before implementations. The test suite covers:
- Domain entity behaviour (construction, update, equality)
- Repository CRUD operations in isolation
- Service logic with a mocked repository (Mockito)
- Full end-to-end API flows with MockMvc integration tests

**Thread safety** — `InMemoryTaskRepository` uses `ConcurrentHashMap` making it safe for concurrent requests.
