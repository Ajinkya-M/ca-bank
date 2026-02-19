# Project Overview

This is a Spring Boot application that provides a `member-service`. It manages member data, offering RESTful endpoints for creating, retrieving, and managing members. The service uses a PostgreSQL database for persistence and is secured with JWT authentication.

## Building and Running

### Prerequisites

*   Java 21
*   Maven

### Build

To build the project, run the following command in the `member-service` directory:

```bash
./mvnw clean install
```

### Run

To run the application, use the following command in the `member-doing-the-same-thing-as-the-other-one` directory:

```bash
./mvnw spring-boot:run
```

The application will start on port `8080` by default.

### Test

To run the tests, execute the following command in the `member-service` directory:

```bash
./mvnw test
```

## Development Conventions

*   **Code Style:** The project follows standard Java conventions.
*   **Testing:** JUnit 5 is used for unit and integration testing. The project also uses Jacoco for code coverage analysis.
*   **API:** The application exposes RESTful APIs for member management.
*   **Authentication:** Authentication is handled using JWT.
