# Convenções de Arquitetura e Código do Backend

**Projeto:** Bifrost  
**Status:** Vigente  
**Stack:** Java + Spring Boot + MySQL (ADR-006)

---

## 1. Objetivo

Registrar as convenções que devem orientar a implementação do backend Bifrost em `apps/backend`.

Padrão: **Clean Architecture**, portas e adaptadores, casos de uso explícitos, controllers finos, domínio protegido e infraestrutura isolada — em monólito modular Spring Boot.

---

## 2. Estilo Arquitetural

```text
src/main/java/com/bifrost/backend
|-- domain/
|-- application/
`-- infrastructure/
```

### 2.1 `domain`

Camada central do negócio. **Não** deve depender de Spring Web, JPA, REST clients ou detalhes de banco.

Responsabilidades:

- Entidades e agregados de negócio
- Value Objects
- Enums de domínio
- Exceções de domínio
- Interfaces de repositório
- Interfaces de query (leituras complexas)
- Portas de saída para integrações externas
- Serviços de domínio quando a regra não couber numa entidade

Estrutura sugerida:

```text
domain/
|-- dto/
|-- enums/
|-- exception/
|-- model/
|   |-- command/
|   `-- query/
|-- port/
|   `-- output/
|-- query/
|-- queryobject/
|-- repository/
|-- service/
`-- vo/
```

### 2.2 `application`

Orquestra fluxos de negócio: use cases, DTOs de aplicação, transações.

```text
application/
|-- dto/
|-- exception/
|-- facade/
|-- factory/
|-- service/
|-- storage/
`-- usecase/
    |-- auth/
    |-- user/
    |-- robotprofile/
    |-- parameter/
    |-- audit/
    |-- telemetry/
    `-- llm/
```

### 2.3 `infrastructure`

Adapters de entrada/saída, persistência, config, segurança, schedulers, integrações.

```text
infrastructure/
|-- adapter/
|   |-- input/
|   |   `-- controller/
|   |       `-- v1/
|   `-- output/
|       |-- http/
|       |-- query/
|       |-- repository/
|       `-- storage/
|-- config/
|-- exception/
|-- filter/
|-- interceptor/
|-- log/
|-- mapper/
|-- security/
`-- scheduler/
```

---

## 3. Convenções de Casos de Uso

Cada ação relevante vira um caso de uso explícito.

Exemplos Bifrost:

- `LoginUserUseCase`
- `GetSessionConfigUseCase`
- `CreateRobotProfileUseCase`
- `ListRobotProfilesUseCase`
- `UpsertParameterUseCase`
- `RecordAuditEventUseCase`
- `AskLlmUseCase`

Convenções:

- Pacote: `application/usecase/<module>`
- Classe anotada com `@Service`
- Método público principal: `execute`
- Escritas com `@Transactional(rollbackFor = Exception.class)`
- Use cases **não** dependem de controllers, JPA entities ou DTOs de provedores externos
- Validações de fluxo no use case; invariantes no domínio

```java
@Service
public class GetSessionConfigUseCase {
    private final UserRepository userRepository;
    private final RobotProfileRepository robotProfileRepository;

    public GetSessionConfigUseCase(
            UserRepository userRepository,
            RobotProfileRepository robotProfileRepository) {
        this.userRepository = userRepository;
        this.robotProfileRepository = robotProfileRepository;
    }

    @Transactional(readOnly = true)
    public SessionConfigDto execute(UUID userId) {
        // Orquestra o fluxo e delega regras ao domínio.
        return null;
    }
}
```

---

## 4. Controllers e Endpoints

```text
infrastructure/adapter/input/controller/v1
```

Convenções:

- `@RestController` + base `/api/v1/<resource>`
- Sem regra de negócio no controller
- Monta request, chama use case, traduz HTTP
- Autorização com `@PreAuthorize` quando específica por endpoint

```java
@RestController
@RequestMapping("/api/v1/robot-profiles")
public class RobotProfileController {
    private final CreateRobotProfileUseCase createRobotProfileUseCase;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Void> create(@RequestBody CreateRobotProfileRequest request) {
        createRobotProfileUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
```

Papéis Bifrost (ADR-004): `ADMIN`, `OPERATOR`, `VIEWER` (ajustar claim/role prefix conforme Spring Security).

---

## 5. DDD Tático

O domínio modela **comportamento**, não o reflexo direto das tabelas. JPA entities são só persistência.

### 5.1 Entidades de domínio

Ficam em `domain/model/command`.

- Classe `public`; campos `private`
- Sem anotações JPA no domínio
- Construtor público chama setters privados (mesmas validações)
- Setters genéricos `private`
- Mudanças de estado por métodos de negócio (`activate`, `deactivate`, `grantAccess`, …)
- Factories estáticas `create(...)` quando houver defaults

```java
public class RobotProfile {
    private UUID id;
    private String slug;
    private String project;
    private String prefix;
    private Boolean active;

    public RobotProfile(UUID id, String slug, String project, String prefix, Boolean active) {
        setId(id);
        setSlug(slug);
        setProject(project);
        setPrefix(prefix);
        setActive(active);
    }

    public static RobotProfile create(CreateRobotProfileDto dto) {
        return new RobotProfile(null, dto.slug(), dto.project(), dto.prefix(), true);
    }

    public void deactivate() {
        setActive(false);
    }

    private void setSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new InvalidInputException("Robot profile slug is required.");
        }
        this.slug = slug.trim();
    }

    private void setActive(Boolean active) {
        if (active == null) {
            throw new CodingErrorException("active flag cannot be null.");
        }
        this.active = active;
    }

    // demais getters/setters privados...
}
```

Exemplos de métodos de domínio esperados:

- `User.activate()` / `User.deactivate()`
- `RobotProfile.create(dto)` / `RobotProfile.deactivate()`
- `AuditEvent.create(type, payload)`

### 5.2 Value Objects

Imutáveis, validam no construtor. Exemplos Bifrost: `Email`, `TopicName`, `RosbridgeUrl`, `SpeedLimit`.

### 5.3 DTOs por camada

- `domain/dto` — comandos de criação/atualização de domínio
- `application/dto` — requests/results de use cases
- `infrastructure/.../controller/v1/dto` — payloads HTTP quando distintos
- `infrastructure/adapter/output/dto` — payloads de provedores externos (ex.: LLM)

DTOs externos **não** entram no domínio.

### 5.4 Modelos de leitura

`domain/model/query` — projeções de consulta, sem regras de escrita.

Leituras complexas:

```text
domain/query/RobotProfileQuery.java
infrastructure/adapter/output/query/robotprofile/JdbcTemplateRobotProfileQuery.java
domain/model/query/RobotProfileQueryResponse.java
```

---

## 6. Persistência JPA

```text
domain/repository/RobotProfileRepository.java
infrastructure/adapter/output/repository/robotprofile/RobotProfileRepositoryImpl.java
infrastructure/adapter/output/repository/robotprofile/RobotProfileJpaRepository.java
infrastructure/adapter/output/repository/robotprofile/RobotProfileJpaEntity.java
```

### 6.1 Repository de domínio (porta)

Retorna/recebe entidades de domínio; sem Spring Data na interface.

### 6.2 JpaRepository

Package-private; só opera `JpaEntity`; só o `RepositoryImpl` o conhece.

### 6.3 RepositoryImpl

`@Component`/`@Repository`; conversão `domain ↔ jpa`; sem regra de negócio.

### 6.4 JpaEntity

- Package-private (`@Entity` não `public`)
- Sem getters/setters públicos; sem regra de negócio
- `fromDomain` / `toDomain` estáticos
- Construtor vazio + completo

### 6.5 Nomenclatura

Todo código (classes, campos, métodos, enums) em **inglês**.

- IDs: `id`, relacionamentos `userId`, `robotProfileId`
- Timestamps: `createdAt`, `updatedAt`
- Flags: `active`, `confirmed` → `isActive()`
- Tabelas/colunas: snake_case inglês (`robot_profile`, `created_at`)
- Enums: `ADMIN`, `OPERATOR`, `VIEWER`, `SIM`, `PHYSICAL`

### 6.6 Queries de leitura

- Use `Repository` para carregar/alterar agregado
- Use `Query` + `JdbcTemplate` para listagens, joins, paginação, dashboards
- Filtros em `domain/queryobject`
- SQL só no adapter; bind parameters; whitelist de ordenação
- Use case de leitura chama a porta `Query`, nunca o `JdbcTemplate`

### 6.7 Migrações

- Flyway obrigatório para alteração estrutural
- Constraints/índices de negócio versionados

---

## 7. Portas e adaptadores externos

```text
domain/port/output/LlmPort.java
application/storage/FileStorage.java   # se necessário
```

Implementações:

```text
infrastructure/adapter/output/http/gemini/GeminiLlmAdapter.java
```

- Use case conhece a porta, não o provedor
- Anti-corruption layer quando o payload externo divergir
- Falhas externas → exceções de aplicação/domínio

---

## 8. Schedulers

`infrastructure/scheduler` — adapter de entrada fino; chama use case; feature flag / delay via config.

Exemplos Bifrost: purge de audit antigo, jobs de telemetria amostrada (evolução).

---

## 9. Segurança, auditoria e observabilidade

- JWT + Spring Security
- Roles: `ADMIN`, `OPERATOR`, `VIEWER`
- Endpoints públicos explícitos (login, health)
- `X-Correlation-Id` gerado/reutilizado por request
- Auditoria de ações críticas (login, profile switch, goal, parameter change)
- Actuator: health/métricas (proteger em produção)

---

## 10. Exceções

- `InvalidInputException`
- `ResourceNotFoundException`
- `OperationNotAllowedException`
- `UnauthorizedException`
- `ResourceUnavailableException`
- `DatabaseInconsistencyException`
- `CodingErrorException`

Handler global → payload HTTP padronizado `{ "error": { "code", "message" } }`.

---

## 11. Testes

- Domínio: entidades + invariantes sem Spring
- Use cases: JUnit 5 + Mockito
- Adapters JPA/HTTP: integração quando o mapeamento for risco
- Cobertura mínima MVP Bifrost: login/RBAC, session-config, CRUD/ACL de robot profile, auditoria básica, parâmetros

---

## 12. Decisões de pacote / tooling

| Item | Decisão |
|------|---------|
| Pacote base | `com.bifrost.backend` |
| Build | Maven (preferencial) ou Gradle — fixar no skeleton |
| Java | 21 LTS (recomendado) |
| Banco | MySQL 8.x |
| Migrations | Flyway |
| Auth | Spring Security + JWT (ADR-004 / ADR-006) |
