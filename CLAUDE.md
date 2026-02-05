# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **BDD (Behavior Driven Development) + SDD (Skills Driven Development)** fusion demo project for Company R&D team. The core workflow is:

```
Business Requirements → BDD Scenarios (Gherkin) → AI Skill Analysis → Auto-Generated Code + Tests → Validation
```

## Key Architectural Concepts

### 1. BDD-First Development Flow

- **Business scenarios** are written in `behaviors/` using Gherkin syntax (supports Chinese)
- Each `.feature` file defines business behaviors that drive code generation
- BDD scenarios are the **source of truth** - code and tests are derived from them

### 2. Skill-Based Code Generation

- **Skills** in `skills/` define how BDD scenarios map to code structures
- Each skill (e.g., `menu-pricing`) contains:
  - `skill-definition.md`: Generation rules and architecture patterns
  - Mapping rules: Feature → Service class, Scenario → Method, Given/When/Then → Test steps
  - Output specifications for Domain/Service/Controller/Repository layers

### 3. Layered Architecture (DDD-Based)

Generated code follows Domain-Driven Design with strict layering:

```
src/main/
├── domain/          # Rich domain models with business logic
│                    # - Business rules encoded as methods (e.g., validatePriceDecrease())
│                    # - State machines (e.g., approve(), startExecution(), completeExecution())
│                    # - Self-validating entities
├── service/         # Business orchestration layer
│                    # - Each method corresponds to a BDD scenario
│                    # - Javadoc references the source BDD scenario
├── controller/      # RESTful API layer (OpenAPI annotated)
└── repository/      # Data access (Spring Data JPA)

src/test/
├── bdd/             # Cucumber step definitions (supports Chinese: @假如, @当, @那么)
└── api/             # Karate API contract tests
```

### 4. External Dependencies Handling

When BDD scenarios involve external services (APIs, databases, message queues):

- **Integration interfaces** defined in `src/main/java/com/company/menu/integration/`
- **Mock in tests** using `@MockBean` in step definitions
- **Test isolation** - each scenario gets clean mocks via `@Before` hooks
- **See**: `docs/bdd-external-dependencies-solutions.md` for complete guide

Example pattern:
```java
@SpringBootTest
public class OrderFlowSteps {
    @MockBean
    private InventoryService inventoryService;  // External dependency

    @假如("库存系统显示 {string} 库存充足")
    public void mockInventory(String productCode) {
        when(inventoryService.checkInventory(eq(productCode), anyInt()))
            .thenReturn(InventoryCheckResult.sufficient());
    }
}
```

### 5. BDD → Code Traceability

**Critical**: All generated code includes Javadoc comments that reference the originating BDD scenario:

```java
/**
 * 对应 BDD 场景: behaviors/menu/price_update.feature
 * - 场景: 单一区域价格上调
 */
```

This bidirectional traceability ensures scenarios and code stay synchronized.

## Development Commands

### Testing

```bash
# Run Cucumber BDD tests (executes all Gherkin scenarios)
mvn test -Dtest=BddTestRunner

# Run Karate API tests (contract testing)
mvn test -Dtest=KarateRunner

# Run specific API test scenarios by tag
mvn test -Dkarate.options="--tags @pricing"

# Run all tests with coverage
mvn clean verify
```

### Code Generation (Conceptual)

This project demonstrates AI-driven generation. In practice:

```bash
# Generate code from BDD scenario using Claude Code
claude-code --skill menu-pricing \
  --input behaviors/menu/price_update.feature \
  --output src/

# Generate only specific layers
claude-code --skill menu-pricing \
  --input behaviors/menu/price_update.feature \
  --generate domain,service
```

### Viewing Components

```bash
# View BDD scenario
cat behaviors/menu/price_update.feature

# View generated domain model
cat src/main/domain/PriceChangeOrder.java

# View corresponding test steps
cat src/test/bdd/PriceUpdateSteps.java
```

## Critical Code Patterns

### 1. Domain Model Pattern

Domain models are **Rich Domain Models** (not anemic):
- Business rules live in domain objects, not services
- State transitions are encapsulated (e.g., `order.approve(approver)`)
- Validation is self-contained (e.g., `validatePriceDecrease()`)

### 2. Service Method Naming

Service methods mirror BDD scenario names and include detailed Javadoc:

```java
/**
 * 创建价格变更单
 *
 * 对应 BDD 场景: "单一区域价格上调"
 * When: 提交价格调整请求
 * Then: 系统应生成价格变更单
 */
public PriceChangeOrder createPriceChangeOrder(...)
```

### 3. Test Step Definitions

Cucumber steps use **Chinese annotations** matching Gherkin keywords:

```java
@当("提交以下价格调整请求:")
public void 提交以下价格调整请求(DataTable dataTable) {
    // Implementation that calls the service layer
}
```

### 4. Exception Handling

Services include rollback mechanisms referenced in BDD scenarios:
- `RollbackService` handles automatic rollback on failure
- Exception types map to BDD failure scenarios (e.g., `PriceUpdateException`)

## System Integration Points

This demo project integrates with three Company systems (conceptual):

- **OMS** (Order Management System): Order creation/cancellation flows
- **VIA** (Fulfillment System): Store assignment and delivery tracking
- **MOD** (Customization System): Modifier validation logic

Each system has its own:
- BDD scenario directory in `behaviors/`
- Skill definition in `skills/`

## Data Flow

When working with this codebase:

1. **Start with BDD scenarios** in `behaviors/` to understand business requirements
2. **Check skill definitions** in `skills/` to understand generation rules
3. **Review domain models** in `src/main/domain/` to see business rules
4. **Examine services** to understand orchestration logic
5. **Verify tests** in `src/test/` match scenarios 1:1

## Important Constraints

### BDD Scenario Quality

- Scenarios use **business language**, not technical implementation details
- Each scenario tests **one business behavior**
- Data tables improve readability for complex inputs
- Scenarios are **independent** - no cross-scenario dependencies

### Code Generation Rules

When generating or modifying code:

- **Always** include Javadoc referencing the source BDD scenario
- Follow the skill's architecture pattern (defined in `skills/*/skill-definition.md`)
- Maintain 80%+ test coverage target
- Use Lombok to reduce boilerplate (`@Data`, `@Builder`, etc.)
- Follow validation-first pattern (validate in domain, not controllers)

### Test Philosophy

- **BDD tests** validate business behaviors (black-box)
- **API tests** validate contracts (request/response schemas)
- **Unit tests** validate edge cases (white-box)
- Test reports generate to `target/cucumber-reports/` and `target/karate-reports/`

## File Organization Logic

```
behaviors/{domain}/         # BDD scenarios grouped by business domain
skills/{domain}/            # Code generation rules per domain
src/main/{layer}/           # Generated code by architectural layer
src/test/{test-type}/       # Tests organized by testing approach
docs/                       # Architecture and integration guides
```

## Technology Stack

- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **BDD**: Cucumber with Chinese language support
- **API Testing**: Karate
- **Build**: Maven
- **Database**: PostgreSQL + Redis (referenced, not implemented)
- **Messaging**: Kafka (referenced, not implemented)

## Context for AI Assistance

When Claude Code assists with this repository:

1. **Understand the BDD → Code flow**: Changes start with BDD scenarios, not code
2. **Preserve traceability**: Always maintain Javadoc links to BDD scenarios
3. **Follow skill definitions**: Use `skills/*/skill-definition.md` as the rulebook
4. **Maintain Chinese support**: Test steps use Chinese annotations (`@假如`, `@当`, `@那么`)
5. **Think domain-first**: Business logic belongs in domain models, not services
6. **Validate bidirectionally**: Tests must match scenarios exactly, scenarios must have test coverage

## Demo Purpose

This is a **demonstration project** showing:
- How BDD scenarios drive development
- How AI can generate code from business requirements
- How to maintain requirements-code-test synchronization
- Integration patterns for Company' OMS/VIA/MOD systems

For full context, see:
- `DEMO.md` - Complete demonstration script
- `docs/architecture.md` - Architectural design
- `docs/integration-guide.md` - BDD execution mechanism
- `PROJECT_SUMMARY.md` - Project completion summary
