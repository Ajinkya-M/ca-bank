# CA Bank Project Constitution

## Project Overview
CA Bank is a Spring Boot-based banking application designed to provide secure, reliable, and scalable member services for banking operations.

## Core Principles

### 1. Code Quality & Maintainability
- **Clean Code**: Follow clean code principles with meaningful names, single responsibility, and proper separation of concerns
- **SOLID Principles**: Adhere to SOLID design principles in all class and interface designs
- **Code Reviews**: All code must be reviewed before merging to ensure consistency and quality
- **Documentation**: Maintain comprehensive JavaDoc for public APIs and complex business logic
- **DRY Principle**: Avoid code duplication; extract reusable components and utilities

### 2. Security & Compliance
- **Security First**: Banking applications handle sensitive data; security is paramount
- **Data Protection**: Implement proper encryption for sensitive data (passwords, PII, financial data)
- **Authentication & Authorization**: Use industry-standard security mechanisms (JWT, OAuth2, Spring Security)
- **Input Validation**: Validate all user inputs to prevent injection attacks and data corruption
- **Audit Logging**: Log all critical operations for compliance and troubleshooting
- **Compliance**: Adhere to banking industry standards and regulations (PCI-DSS, GDPR where applicable)

### 3. Testing Standards
- **Test Coverage**: Minimum 80% code coverage for critical business logic
- **Test Pyramid**: Follow the test pyramid - more unit tests, fewer integration tests, minimal E2E tests
- **Unit Tests**: All service layer methods must have corresponding unit tests
- **Integration Tests**: Test database interactions and external service integrations
- **Test Data**: Use realistic but anonymized test data; never use production data in tests
- **Continuous Testing**: Tests must pass before any code merge

### 4. Performance & Scalability
- **Response Time**: API endpoints should respond within 200ms for 95th percentile
- **Database Optimization**: Use proper indexing, query optimization, and connection pooling
- **Caching**: Implement caching strategies for frequently accessed, slowly-changing data
- **Async Processing**: Use asynchronous processing for long-running operations
- **Load Testing**: Regular load testing to ensure the system handles expected traffic
- **Resource Management**: Proper cleanup of resources (connections, file handles, etc.)

### 5. API Design & Consistency
- **RESTful Standards**: Follow REST principles for API design
- **Consistent Response Format**: Use standardized response DTOs (ApiResponse pattern)
- **Versioning**: Implement API versioning to manage breaking changes
- **Error Handling**: Return meaningful error messages with proper HTTP status codes
- **Documentation**: Maintain up-to-date API documentation (Swagger/OpenAPI)
- **Idempotency**: POST operations should be idempotent where applicable

### 6. Database & Data Management
- **Transactions**: Use proper transaction management for data consistency
- **Data Integrity**: Enforce referential integrity at the database level
- **Migration Strategy**: Use Flyway or Liquibase for database version control
- **Backup Strategy**: Regular automated backups with tested recovery procedures
- **Data Retention**: Implement proper data retention and archival policies

### 7. Architecture & Design Patterns
- **Layered Architecture**: Maintain clear separation: Controller → Service → Repository
- **DTOs**: Use DTOs for API requests/responses; never expose entities directly
- **Dependency Injection**: Leverage Spring's DI container; avoid manual object creation
- **Configuration Management**: Externalize configuration using application.properties/yml
- **Exception Handling**: Global exception handling with custom exception types
- **Logging Strategy**: Structured logging with appropriate log levels

### 8. Development Practices
- **Version Control**: Use Git with meaningful commit messages and branching strategy
- **CI/CD**: Automated build, test, and deployment pipelines
- **Code Formatting**: Consistent code formatting (use IDE formatters or Checkstyle)
- **Dependency Management**: Keep dependencies up-to-date; address security vulnerabilities promptly
- **Environment Parity**: Development, staging, and production environments should be similar
- **Feature Flags**: Use feature flags for gradual rollouts and A/B testing

### 9. Monitoring & Observability
- **Application Monitoring**: Use Spring Boot Actuator for health checks and metrics
- **Error Tracking**: Implement error tracking and alerting (e.g., Sentry, New Relic)
- **Log Aggregation**: Centralized logging for easier debugging and analysis
- **Performance Metrics**: Track key performance indicators (response times, error rates, throughput)
- **Alerting**: Set up alerts for critical system failures and anomalies

### 10. User Experience
- **API Usability**: APIs should be intuitive and easy to use
- **Error Messages**: User-facing error messages should be clear and actionable
- **Consistency**: Consistent behavior across all endpoints and features
- **Backwards Compatibility**: Maintain backwards compatibility when possible
- **Response Time**: Optimize for fast response times to enhance user experience

### 11. AI Development Practices
- **Strict Execution Rule**: **CRITICAL** - Do not proceed to the next task or execute any code/commands without explicit human permission
  - For **every single task**, first explain exactly what you are about to do
  - Clearly state which files will be created, modified, or deleted
  - Describe the changes that will be made
  - **Pause and wait for user approval** before executing
  - Only proceed after receiving explicit "proceed", "yes", "go ahead", or similar confirmation
  - If uncertain about approval, always ask again rather than assuming
- **Incremental Implementation**: Break down large features into small, manageable steps
- **Transparency**: Always explain the reasoning behind technical decisions
- **Error Recovery**: If an error occurs, explain what went wrong and proposed fixes before proceeding

## Technology Stack Guidelines
- **Spring Boot**: Primary framework for application development
- **Spring Data JPA**: For database interactions
- **Spring Security**: For authentication and authorization
- **PostgreSQL/MySQL**: Primary relational database
- **Maven/Gradle**: Build and dependency management
- **JUnit 5 & Mockito**: Testing frameworks
- **Lombok**: To reduce boilerplate code (use judiciously)

## Decision-Making Framework
When making technical decisions, consider:
1. **Security implications** - Does this introduce any security risks?
2. **Performance impact** - How does this affect system performance?
3. **Maintainability** - Will this be easy to maintain and extend?
4. **Testing** - Can this be easily tested?
5. **Scalability** - Does this scale with increased load?
6. **Cost** - What are the infrastructure and maintenance costs?

## Definition of Done
A feature is considered "done" when:
- [ ] Code is written following the above principles
- [ ] Unit tests are written and passing (minimum 80% coverage)
- [ ] Integration tests are written where applicable
- [ ] Code has been reviewed and approved
- [ ] Documentation is updated (JavaDoc, API docs, README)
- [ ] No critical security vulnerabilities
- [ ] Performance benchmarks are met
- [ ] Feature has been tested in a staging environment
- [ ] Logging and monitoring are in place

---

**Last Updated**: 2026-01-23  
**Version**: 1.1
