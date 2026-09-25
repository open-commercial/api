# Copilot instructions for Open Commercial API

## Build, test, and analysis

This is a Maven Spring Boot application targeting Java 21. Run commands from the repository root:

```bash
# Compile, run tests, generate JaCoCo coverage, and package
mvn -B verify

# Run the complete test suite
mvn test

# Run one test class
mvn -Dtest=ProductoServiceImplTest test

# Run one test method
mvn -Dtest=ProductoServiceImplTest#shouldValidarCalculos test

# Run the same analysis used by CI (requires SonarCloud credentials/configuration)
mvn -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.organization=open-commercial \
  -Dsonar.projectKey=api:api
```

There is no repository-configured formatter, Checkstyle, Spotless, or separate lint task; `verify` is the normal validation command. The build includes JAXB generation from the AFIP WSDL URLs, so a clean build may require network access. Integration and repository tests use Testcontainers with MySQL and therefore require Docker. Test configuration is in `src/test/resources/application.properties`, initializes `src/test/resources/data.sql`, and creates the schema for tests.

## Architecture

- `org.opencommercial.App` is the Spring Boot entry point and enables scheduling and async execution.
- HTTP endpoints are in `controller`; most routes are versioned under `/api/v1`. Controllers handle request mapping, authorization annotations, DTO/entity mapping, and orchestration, while business rules and transactions belong in `service/*ServiceImpl`.
- Persistence is in `repository`: Spring Data JPA repositories commonly also implement `QuerydslPredicateExecutor` for criteria searches. Complex totals and aggregates use `repository/custom` implementations backed by Querydsl and `JPAQueryFactory`; generated Querydsl sources are build artifacts.
- Domain entities are in `model`, with request/search DTOs in `model/dto` and `model/criteria`, and reusable value objects in `model/embeddable`. Many business entities use an `eliminado`/`eliminada` soft-delete flag; normal reads and queries must preserve the non-deleted invariant.
- Cross-cutting configuration is in `config`, including ModelMapper mappings, AFIP SOAP marshalling, CORS, caching, and JSON serialization for Spring `Page`. `AppWebMvc` registers `JwtInterceptor` for `/api/**` and explicitly excludes public/authentication and payment-notification routes.
- Authentication is JWT-based: `JwtInterceptor` validates the `Authorization` header, enabled-user status, and presence of roles. Methods requiring role authorization use `@AccesoRolesPermitidos`, enforced by `AuthAspect`; use the existing localized messages and exception types for failures.
- External integrations include AFIP SOAP, Cloudinary image uploads, Mercado Pago, Gmail/Resend email providers, reCAPTCHA, JasperReports/PDF, and Apache POI/XLSX. Configuration is supplied through environment variables consumed by `application.properties`; do not hard-code credentials or tokens.
- `src/main/resources/report/*.jrxml` contains Jasper report templates, `scripts/` contains SQL analytics/hotfix/data scripts, and `doc/ARCA_ARGENTINA` contains AFIP-related artifacts. `Dockerfile` packages the built JAR with the New Relic agent and exposes port 8080.

## Codebase conventions

- Keep the interface/implementation split for services (`FooService` and `FooServiceImpl`) and inject collaborators through constructors. Follow the existing Spring stereotypes and place transaction boundaries on service methods/classes, not controllers.
- Prefer repository-derived methods for straightforward lookups and `@Query` for explicit JPQL. Use Querydsl custom repositories when a criteria predicate or aggregate spans several joins; preserve soft-delete predicates in both derived and custom queries.
- Controllers commonly accept DTOs and resolve related entities through their services before calling the main service. Preserve existing `ModelMapper` mappings and explicit enrichment logic rather than exposing new persistence relationships directly.
- Business failures use `BusinessServiceException`, `ServiceException`, `UnauthorizedException`, `ForbiddenException`, or `EntityNotFoundException` as appropriate. User-facing messages are localized in `src/main/resources/messages.properties` and validation messages in `ValidationMessages.properties`; use `MessageSource` and `Locale.getDefault()` consistently.
- Financial and quantity calculations use `BigDecimal`; preserve explicit scale/rounding and existing domain calculation helpers. Do not replace monetary arithmetic with `double`.
- Search endpoints use criteria DTOs and Spring `Page`/`Pageable`; the application registers a custom stable JSON shape for pages (`totalElements`, `totalPages`, `number`, `numberOfElements`, `size`, `first`, `last`, `content`, and `sort`).
- Async work is opt-in via `@Async`, and scheduled jobs are enabled globally. Be careful when changing email, stock, cash, order, or statistics services because these jobs coordinate mutable inventory/accounting state.
- Tests are mostly focused Spring contexts with `@ContextConfiguration` and `@MockitoBean` collaborators, named after the class under test. Controller/service tests verify orchestration with Mockito; use `AppIntegrationTest` for end-to-end HTTP/database behavior and Testcontainers-backed repository coverage where persistence behavior matters.
- Keep API and domain naming consistent with the existing Spanish terminology (`Producto`, `Sucursal`, `Pedido`, `eliminado`, etc.), and use the existing endpoint versioning and plural resource paths when adding routes.
