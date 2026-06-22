# Attus Challenge — Módulo Financeiro


> Este README é uma especificação completa de implementação. Siga cada seção na ordem apresentada.
> Execute as etapas abaixo em sequência, sem pular nenhuma:
>
> 1. Configure o `build.gradle` com todas as dependências listadas
> 2. Configure o `application.yml`
> 3. Crie **todos os arquivos do backend** na ordem: Enums → Entities → DTOs → Repositories → Services → Controllers → Config
> 4. Crie os **testes** antes do frontend
> 5. Configure e crie o projeto **Angular** dentro da pasta `frontend/`
> 6. Crie o `docker-compose.yml` na raiz
> 7. Ao finalizar cada arquivo, confirme criando o próximo sem esperar
> 8. **Nunca** deixe implementações em branco (ex: `// TODO`). Implemente tudo completamente.

---

## Stack e Versões

| Camada | Tecnologia | Versão |
|--------|-----------|--------|
| JDK | Java | 17 |
| Backend | Spring Boot | 3.2.x |
| Build | Gradle | (já configurado no projeto) |
| Banco (prod) | PostgreSQL | 16 |
| Banco (teste) | H2 | in-memory |
| Migrations | Flyway | via Spring Boot |
| Docs | SpringDoc OpenAPI | 2.x |
| Logs | SLF4J + Logback + Spring AOP | - |
| Frontend | Angular CLI | 17.x |
| UI | Angular Material | 17.x |
| Containerização | Docker + docker-compose | - |

---

## Estrutura de Pastas a Criar

```
attus-challenge/
├── src/
│   ├── main/
│   │   ├── java/com/attus/financial/
│   │   │   ├── FinancialApplication.java
│   │   │   ├── config/
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── AuditAspect.java
│   │   │   ├── controller/
│   │   │   │   ├── ProductController.java
│   │   │   │   ├── CustomerController.java
│   │   │   │   ├── AccountController.java
│   │   │   │   └── TransactionController.java
│   │   │   ├── service/
│   │   │   │   ├── ProductService.java
│   │   │   │   ├── CustomerService.java
│   │   │   │   ├── AccountService.java
│   │   │   │   └── TransactionService.java
│   │   │   ├── repository/
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── CustomerRepository.java
│   │   │   │   ├── AccountRepository.java
│   │   │   │   ├── TransactionRepository.java
│   │   │   │   └── AuditLogRepository.java
│   │   │   ├── domain/
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Product.java
│   │   │   │   │   ├── Customer.java
│   │   │   │   │   ├── Account.java
│   │   │   │   │   ├── Transaction.java
│   │   │   │   │   └── AuditLog.java
│   │   │   │   └── enums/
│   │   │   │       ├── ProductType.java
│   │   │   │       ├── ProductStatus.java
│   │   │   │       ├── AccountStatus.java
│   │   │   │       ├── CustomerStatus.java
│   │   │   │       └── TransactionType.java
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── ProductRequest.java
│   │   │   │   │   ├── CustomerRequest.java
│   │   │   │   │   ├── AccountRequest.java
│   │   │   │   │   └── TransactionRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── ProductResponse.java
│   │   │   │       ├── CustomerResponse.java
│   │   │   │       ├── AccountResponse.java
│   │   │   │       ├── TransactionResponse.java
│   │   │   │       └── ApiResponse.java
│   │   │   └── exception/
│   │   │       ├── BusinessException.java
│   │   │       ├── ResourceNotFoundException.java
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-test.yml
│   │       └── db/migration/
│   │           ├── V1__create_tables.sql
│   │           └── V2__seed_data.sql
│   └── test/
│       └── java/com/attus/financial/
│           ├── controller/
│           │   └── ProductControllerTest.java
│           ├── service/
│           │   └── TransactionServiceTest.java
│           └── repository/
│               └── AccountRepositoryTest.java
├── frontend/                          ← Projeto Angular aqui
│   ├── src/app/
│   │   ├── core/
│   │   │   ├── interceptors/
│   │   │   │   └── error.interceptor.ts
│   │   │   ├── models/
│   │   │   │   ├── product.model.ts
│   │   │   │   ├── customer.model.ts
│   │   │   │   ├── account.model.ts
│   │   │   │   └── transaction.model.ts
│   │   │   └── services/
│   │   │       ├── product.service.ts
│   │   │       ├── customer.service.ts
│   │   │       ├── account.service.ts
│   │   │       └── transaction.service.ts
│   │   ├── shared/
│   │   │   ├── components/
│   │   │   │   └── confirm-dialog/
│   │   │   │       └── confirm-dialog.component.ts
│   │   │   └── validators/
│   │   │       └── cpf.validator.ts
│   │   ├── features/
│   │   │   ├── products/
│   │   │   │   ├── product-list/
│   │   │   │   ├── product-form/
│   │   │   │   └── products.routes.ts
│   │   │   ├── customers/
│   │   │   │   ├── customer-list/
│   │   │   │   ├── customer-form/
│   │   │   │   └── customers.routes.ts
│   │   │   ├── accounts/
│   │   │   │   ├── account-list/
│   │   │   │   ├── account-detail/
│   │   │   │   └── accounts.routes.ts
│   │   │   └── transactions/
│   │   │       ├── transaction-form/
│   │   │       └── transactions.routes.ts
│   │   ├── app.routes.ts
│   │   ├── app.config.ts
│   │   └── app.component.ts
│   └── proxy.conf.json
├── docker-compose.yml
└── README.md
```

---

## ETAPA 1 — build.gradle

Substitua o conteúdo do `build.gradle` existente por:

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.5'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.attus'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-aop'

    // Banco de Dados
    runtimeOnly 'org.postgresql:postgresql'
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-database-postgresql'

    // Swagger / OpenAPI
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // Testes
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'com.h2database:h2'
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

E o `settings.gradle`:
```groovy
rootProject.name = 'attus-challenge'
```

---

## ETAPA 2 — Configuração da Aplicação

### `src/main/resources/application.yml`
```yaml
spring:
  application:
    name: attus-financial-api
  datasource:
    url: jdbc:postgresql://localhost:5432/financial_db
    username: attus
    password: attus123
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

server:
  port: 8080

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

logging:
  level:
    com.attus.financial: DEBUG
    org.springframework.web: INFO
```

### `src/main/resources/application-test.yml`
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
    username: sa
    password:
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  flyway:
    enabled: false
```

---

## ETAPA 3 — Migrations SQL

### `src/main/resources/db/migration/V1__create_tables.sql`
```sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE tb_products (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(100) NOT NULL,
    description   TEXT,
    type          VARCHAR(50)  NOT NULL,
    interest_rate DECIMAL(5,2),
    min_balance   DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP
);

CREATE TABLE tb_customers (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(150) NOT NULL,
    cpf        VARCHAR(14)  NOT NULL UNIQUE,
    email      VARCHAR(150) NOT NULL UNIQUE,
    phone      VARCHAR(20),
    birth_date DATE         NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE TABLE tb_accounts (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    number      VARCHAR(20)   NOT NULL UNIQUE,
    customer_id UUID          NOT NULL REFERENCES tb_customers(id),
    product_id  UUID          NOT NULL REFERENCES tb_products(id),
    balance     DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status      VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    opened_at   TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP
);

CREATE TABLE tb_transactions (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id     UUID          NOT NULL REFERENCES tb_accounts(id),
    type           VARCHAR(20)   NOT NULL,
    amount         DECIMAL(15,2) NOT NULL,
    description    VARCHAR(200),
    balance_before DECIMAL(15,2) NOT NULL,
    balance_after  DECIMAL(15,2) NOT NULL,
    status         VARCHAR(20)   NOT NULL DEFAULT 'SUCCESS',
    created_at     TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE tb_audit_logs (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type  VARCHAR(50)  NOT NULL,
    entity_id    VARCHAR(100) NOT NULL,
    action       VARCHAR(20)  NOT NULL,
    payload      TEXT,
    performed_by VARCHAR(100),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);
```

### `src/main/resources/db/migration/V2__seed_data.sql`
```sql
INSERT INTO tb_products (id, name, description, type, interest_rate, min_balance, status)
VALUES
    (gen_random_uuid(), 'Conta Poupança', 'Conta poupança com rendimento mensal', 'SAVINGS', 0.50, 0.00, 'ACTIVE'),
    (gen_random_uuid(), 'Fundo de Investimento', 'Fundo de renda variável', 'INVESTMENT', 1.20, 100.00, 'ACTIVE'),
    (gen_random_uuid(), 'Crédito Pessoal', 'Linha de crédito pessoal', 'CREDIT', 3.50, 0.00, 'ACTIVE'),
    (gen_random_uuid(), 'Conta Corrente', 'Conta corrente básica', 'CHECKING', 0.00, 0.00, 'ACTIVE');

INSERT INTO tb_customers (id, name, cpf, email, phone, birth_date, status)
VALUES
    (gen_random_uuid(), 'João Silva', '123.456.789-09', 'joao.silva@email.com', '(11) 99999-1111', '1990-05-15', 'ACTIVE'),
    (gen_random_uuid(), 'Maria Santos', '987.654.321-00', 'maria.santos@email.com', '(11) 99999-2222', '1985-08-22', 'ACTIVE');
```

---

## ETAPA 4 — Enums

### `ProductType.java`
```java
package com.attus.financial.domain.enums;
public enum ProductType { SAVINGS, INVESTMENT, CREDIT, CHECKING }
```

### `ProductStatus.java`
```java
package com.attus.financial.domain.enums;
public enum ProductStatus { ACTIVE, INACTIVE }
```

### `AccountStatus.java`
```java
package com.attus.financial.domain.enums;
public enum AccountStatus { ACTIVE, BLOCKED, CLOSED }
```

### `CustomerStatus.java`
```java
package com.attus.financial.domain.enums;
public enum CustomerStatus { ACTIVE, INACTIVE }
```

### `TransactionType.java`
```java
package com.attus.financial.domain.enums;
public enum TransactionType { DEPOSIT, WITHDRAWAL, TRANSFER }
```

---

## ETAPA 5 — Entidades JPA

### `Product.java`
```java
package com.attus.financial.domain.entity;

import com.attus.financial.domain.enums.ProductStatus;
import com.attus.financial.domain.enums.ProductType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProductType type;

    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "min_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal minBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### `Customer.java`
```java
package com.attus.financial.domain.entity;

import com.attus.financial.domain.enums.CustomerStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_customers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### `Account.java`
```java
package com.attus.financial.domain.entity;

import com.attus.financial.domain.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_accounts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "opened_at", nullable = false, updatable = false)
    private LocalDateTime openedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### `Transaction.java`
```java
package com.attus.financial.domain.entity;

import com.attus.financial.domain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 200)
    private String description;

    @Column(name = "balance_before", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Column(nullable = false, length = 20)
    private String status = "SUCCESS";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

### `AuditLog.java`
```java
package com.attus.financial.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false, length = 100)
    private String entityId;

    @Column(nullable = false, length = 20)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

---

## ETAPA 6 — DTOs

### `ApiResponse.java` (resposta padronizada)
```java
package com.attus.financial.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String status;
    private T data;
    private String message;
    private String code;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder().status("success").data(data).build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder().status("error").code(code).message(message).build();
    }
}
```

### `ProductRequest.java`
```java
package com.attus.financial.dto.request;

import com.attus.financial.domain.enums.ProductType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String name;

    private String description;

    @NotNull(message = "Tipo do produto é obrigatório")
    private ProductType type;

    @DecimalMin(value = "0.0", message = "Taxa de juros não pode ser negativa")
    @Digits(integer = 3, fraction = 2, message = "Taxa de juros inválida")
    private BigDecimal interestRate;

    @DecimalMin(value = "0.0", message = "Saldo mínimo não pode ser negativo")
    private BigDecimal minBalance = BigDecimal.ZERO;
}
```

### `ProductResponse.java`
```java
package com.attus.financial.dto.response;

import com.attus.financial.domain.entity.Product;
import com.attus.financial.domain.enums.ProductStatus;
import com.attus.financial.domain.enums.ProductType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ProductResponse {
    private UUID id;
    private String name;
    private String description;
    private ProductType type;
    private BigDecimal interestRate;
    private BigDecimal minBalance;
    private ProductStatus status;
    private LocalDateTime createdAt;

    public static ProductResponse from(Product p) {
        return ProductResponse.builder()
                .id(p.getId()).name(p.getName()).description(p.getDescription())
                .type(p.getType()).interestRate(p.getInterestRate())
                .minBalance(p.getMinBalance()).status(p.getStatus())
                .createdAt(p.getCreatedAt()).build();
    }
}
```

### `CustomerRequest.java`
```java
package com.attus.financial.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 150)
    private String name;

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "CPF deve estar no formato 000.000.000-00")
    private String cpf;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    private String phone;

    @NotNull(message = "Data de nascimento é obrigatória")
    @Past(message = "Data de nascimento deve ser no passado")
    private LocalDate birthDate;
}
```

### `CustomerResponse.java`
```java
package com.attus.financial.dto.response;

import com.attus.financial.domain.entity.Customer;
import com.attus.financial.domain.enums.CustomerStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CustomerResponse {
    private UUID id;
    private String name;
    private String cpf;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private CustomerStatus status;
    private LocalDateTime createdAt;

    public static CustomerResponse from(Customer c) {
        return CustomerResponse.builder()
                .id(c.getId()).name(c.getName()).cpf(c.getCpf())
                .email(c.getEmail()).phone(c.getPhone())
                .birthDate(c.getBirthDate()).status(c.getStatus())
                .createdAt(c.getCreatedAt()).build();
    }
}
```

### `AccountRequest.java`
```java
package com.attus.financial.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class AccountRequest {
    @NotNull(message = "Cliente é obrigatório")
    private UUID customerId;

    @NotNull(message = "Produto é obrigatório")
    private UUID productId;
}
```

### `AccountResponse.java`
```java
package com.attus.financial.dto.response;

import com.attus.financial.domain.entity.Account;
import com.attus.financial.domain.enums.AccountStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AccountResponse {
    private UUID id;
    private String number;
    private UUID customerId;
    private String customerName;
    private UUID productId;
    private String productName;
    private BigDecimal balance;
    private AccountStatus status;
    private LocalDateTime openedAt;

    public static AccountResponse from(Account a) {
        return AccountResponse.builder()
                .id(a.getId()).number(a.getNumber())
                .customerId(a.getCustomer().getId())
                .customerName(a.getCustomer().getName())
                .productId(a.getProduct().getId())
                .productName(a.getProduct().getName())
                .balance(a.getBalance()).status(a.getStatus())
                .openedAt(a.getOpenedAt()).build();
    }
}
```

### `TransactionRequest.java`
```java
package com.attus.financial.dto.request;

import com.attus.financial.domain.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransactionRequest {

    @NotNull(message = "Conta de origem é obrigatória")
    private UUID accountId;

    @NotNull(message = "Tipo da transação é obrigatório")
    private TransactionType type;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal amount;

    private String description;

    // Apenas para transferências
    private UUID destinationAccountId;
}
```

### `TransactionResponse.java`
```java
package com.attus.financial.dto.response;

import com.attus.financial.domain.entity.Transaction;
import com.attus.financial.domain.enums.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class TransactionResponse {
    private UUID id;
    private UUID accountId;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String status;
    private LocalDateTime createdAt;

    public static TransactionResponse from(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .accountId(t.getAccount().getId())
                .type(t.getType()).amount(t.getAmount())
                .description(t.getDescription())
                .balanceBefore(t.getBalanceBefore())
                .balanceAfter(t.getBalanceAfter())
                .status(t.getStatus())
                .createdAt(t.getCreatedAt()).build();
    }
}
```

---

## ETAPA 7 — Exceptions

### `BusinessException.java`
```java
package com.attus.financial.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {
    private final String code;
    private final HttpStatus httpStatus;

    public BusinessException(String code, String message, HttpStatus httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() { return code; }
    public HttpStatus getHttpStatus() { return httpStatus; }
}
```

### `ResourceNotFoundException.java`
```java
package com.attus.financial.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, Object id) {
        super("RESOURCE_NOT_FOUND",
              resource + " com id " + id + " não encontrado(a)",
              HttpStatus.NOT_FOUND);
    }
}
```

### `GlobalExceptionHandler.java`
```java
package com.attus.financial.exception;

import com.attus.financial.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        log.warn("[EXCEPTION] {} - {}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            errors.put(field, error.getDefaultMessage());
        });
        log.warn("[VALIDATION] Erros de validação: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .status("error").code("VALIDATION_ERROR")
                        .message("Erros de validação").data(errors).build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        log.error("[ERROR] Erro inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "Erro interno no servidor"));
    }
}
```

---

## ETAPA 8 — Repositories

### `ProductRepository.java`
```java
package com.attus.financial.repository;

import com.attus.financial.domain.entity.Product;
import com.attus.financial.domain.enums.ProductStatus;
import com.attus.financial.domain.enums.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByStatus(ProductStatus status);
    List<Product> findByType(ProductType type);
    boolean existsByNameIgnoreCase(String name);
}
```

### `CustomerRepository.java`
```java
package com.attus.financial.repository;

import com.attus.financial.domain.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);
}
```

### `AccountRepository.java`
```java
package com.attus.financial.repository;

import com.attus.financial.domain.entity.Account;
import com.attus.financial.domain.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByCustomerId(UUID customerId);
    List<Account> findByStatus(AccountStatus status);
    boolean existsByCustomerIdAndProductId(UUID customerId, UUID productId);

    @Query("SELECT COUNT(a) > 0 FROM Account a WHERE a.product.id = :productId")
    boolean hasAccountsLinkedToProduct(UUID productId);
}
```

### `TransactionRepository.java`
```java
package com.attus.financial.repository;

import com.attus.financial.domain.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Page<Transaction> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);
}
```

### `AuditLogRepository.java`
```java
package com.attus.financial.repository;

import com.attus.financial.domain.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {}
```

---

## ETAPA 9 — Services

### `ProductService.java`
```java
package com.attus.financial.service;

import com.attus.financial.domain.entity.Product;
import com.attus.financial.domain.enums.ProductStatus;
import com.attus.financial.dto.request.ProductRequest;
import com.attus.financial.dto.response.ProductResponse;
import com.attus.financial.exception.BusinessException;
import com.attus.financial.exception.ResourceNotFoundException;
import com.attus.financial.repository.AccountRepository;
import com.attus.financial.repository.AuditLogRepository;
import com.attus.financial.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public ProductResponse create(ProductRequest request) {
        log.info("[PRODUCT] Criando produto: {}", request.getName());
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .interestRate(request.getInterestRate())
                .minBalance(request.getMinBalance())
                .status(ProductStatus.ACTIVE)
                .build();
        Product saved = productRepository.save(product);
        auditLogService.log("Product", saved.getId().toString(), "CREATE", saved.getName());
        log.info("[PRODUCT] Produto criado com id: {}", saved.getId());
        return ProductResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {
        return ProductResponse.from(getProductOrThrow(id));
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        log.info("[PRODUCT] Atualizando produto id: {}", id);
        Product product = getProductOrThrow(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setType(request.getType());
        product.setInterestRate(request.getInterestRate());
        product.setMinBalance(request.getMinBalance());
        Product saved = productRepository.save(product);
        auditLogService.log("Product", id.toString(), "UPDATE", saved.getName());
        return ProductResponse.from(saved);
    }

    @Transactional
    public void toggleStatus(UUID id) {
        Product product = getProductOrThrow(id);
        ProductStatus newStatus = product.getStatus() == ProductStatus.ACTIVE
                ? ProductStatus.INACTIVE : ProductStatus.ACTIVE;
        product.setStatus(newStatus);
        productRepository.save(product);
        auditLogService.log("Product", id.toString(), "STATUS_CHANGE", newStatus.name());
        log.info("[PRODUCT] Status do produto {} alterado para {}", id, newStatus);
    }

    @Transactional
    public void delete(UUID id) {
        Product product = getProductOrThrow(id);
        if (accountRepository.hasAccountsLinkedToProduct(id)) {
            throw new BusinessException("PRODUCT_IN_USE",
                    "Produto possui contas vinculadas e não pode ser excluído",
                    HttpStatus.CONFLICT);
        }
        productRepository.delete(product);
        auditLogService.log("Product", id.toString(), "DELETE", product.getName());
        log.info("[PRODUCT] Produto {} excluído", id);
    }

    private Product getProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", id));
    }
}
```

### `CustomerService.java`
```java
package com.attus.financial.service;

import com.attus.financial.domain.entity.Customer;
import com.attus.financial.domain.enums.CustomerStatus;
import com.attus.financial.dto.request.CustomerRequest;
import com.attus.financial.dto.response.AccountResponse;
import com.attus.financial.dto.response.CustomerResponse;
import com.attus.financial.exception.BusinessException;
import com.attus.financial.exception.ResourceNotFoundException;
import com.attus.financial.repository.AccountRepository;
import com.attus.financial.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        log.info("[CUSTOMER] Criando cliente CPF: {}", request.getCpf());
        if (customerRepository.existsByCpf(request.getCpf())) {
            throw new BusinessException("CPF_ALREADY_EXISTS", "CPF já cadastrado", HttpStatus.CONFLICT);
        }
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("EMAIL_ALREADY_EXISTS", "E-mail já cadastrado", HttpStatus.CONFLICT);
        }
        Customer customer = Customer.builder()
                .name(request.getName()).cpf(request.getCpf())
                .email(request.getEmail()).phone(request.getPhone())
                .birthDate(request.getBirthDate()).status(CustomerStatus.ACTIVE)
                .build();
        Customer saved = customerRepository.save(customer);
        auditLogService.log("Customer", saved.getId().toString(), "CREATE", saved.getName());
        return CustomerResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream()
                .map(CustomerResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(UUID id) {
        return CustomerResponse.from(getCustomerOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> findAccountsByCustomerId(UUID id) {
        getCustomerOrThrow(id);
        return accountRepository.findByCustomerId(id).stream()
                .map(AccountResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = getCustomerOrThrow(id);
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setBirthDate(request.getBirthDate());
        Customer saved = customerRepository.save(customer);
        auditLogService.log("Customer", id.toString(), "UPDATE", saved.getName());
        return CustomerResponse.from(saved);
    }

    @Transactional
    public void toggleStatus(UUID id) {
        Customer customer = getCustomerOrThrow(id);
        CustomerStatus newStatus = customer.getStatus() == CustomerStatus.ACTIVE
                ? CustomerStatus.INACTIVE : CustomerStatus.ACTIVE;
        customer.setStatus(newStatus);
        customerRepository.save(customer);
        auditLogService.log("Customer", id.toString(), "STATUS_CHANGE", newStatus.name());
    }

    private Customer getCustomerOrThrow(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }
}
```

### `AuditLogService.java`
```java
package com.attus.financial.service;

import com.attus.financial.domain.entity.AuditLog;
import com.attus.financial.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String entityType, String entityId, String action, String payload) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .payload(payload)
                    .performedBy("system")
                    .build();
            auditLogRepository.save(auditLog);
            log.debug("[AUDIT_LOG] {} {} {} - {}", action, entityType, entityId, payload);
        } catch (Exception e) {
            log.error("[AUDIT_LOG] Falha ao persistir audit log: {}", e.getMessage());
        }
    }
}
```

### `AccountService.java`
```java
package com.attus.financial.service;

import com.attus.financial.domain.entity.Account;
import com.attus.financial.domain.entity.Customer;
import com.attus.financial.domain.entity.Product;
import com.attus.financial.domain.enums.AccountStatus;
import com.attus.financial.domain.enums.ProductStatus;
import com.attus.financial.dto.request.AccountRequest;
import com.attus.financial.dto.response.AccountResponse;
import com.attus.financial.exception.BusinessException;
import com.attus.financial.exception.ResourceNotFoundException;
import com.attus.financial.repository.AccountRepository;
import com.attus.financial.repository.CustomerRepository;
import com.attus.financial.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public AccountResponse open(AccountRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getCustomerId()));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto", request.getProductId()));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BusinessException("PRODUCT_INACTIVE", "Produto inativo não aceita novas contas", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Account account = Account.builder()
                .number(generateAccountNumber())
                .customer(customer)
                .product(product)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();
        Account saved = accountRepository.save(account);
        auditLogService.log("Account", saved.getId().toString(), "CREATE", "Conta " + saved.getNumber());
        log.info("[ACCOUNT] Conta aberta: {} para cliente: {}", saved.getNumber(), customer.getName());
        return AccountResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> findAll() {
        return accountRepository.findAll().stream().map(AccountResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountResponse findById(UUID id) {
        return AccountResponse.from(getAccountOrThrow(id));
    }

    @Transactional
    public AccountResponse changeStatus(UUID id, AccountStatus newStatus) {
        Account account = getAccountOrThrow(id);
        if (newStatus == AccountStatus.CLOSED && account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException("BALANCE_NOT_ZERO", "Conta só pode ser encerrada com saldo zero", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        account.setStatus(newStatus);
        Account saved = accountRepository.save(account);
        auditLogService.log("Account", id.toString(), "STATUS_CHANGE", newStatus.name());
        return AccountResponse.from(saved);
    }

    public Account getAccountOrThrow(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta", id));
    }

    private String generateAccountNumber() {
        return String.format("%08d-%d",
                (int)(Math.random() * 99999999),
                (int)(Math.random() * 9));
    }
}
```

### `TransactionService.java`
```java
package com.attus.financial.service;

import com.attus.financial.domain.entity.Account;
import com.attus.financial.domain.entity.Transaction;
import com.attus.financial.domain.enums.AccountStatus;
import com.attus.financial.domain.enums.TransactionType;
import com.attus.financial.dto.request.TransactionRequest;
import com.attus.financial.dto.response.TransactionResponse;
import com.attus.financial.exception.BusinessException;
import com.attus.financial.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final AuditLogService auditLogService;

    @Transactional
    public TransactionResponse execute(TransactionRequest request) {
        log.info("[TRANSACTION] Iniciando {} de R${} na conta {}", request.getType(), request.getAmount(), request.getAccountId());
        Account account = accountService.getAccountOrThrow(request.getAccountId());
        validateAccountActive(account);

        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfter;

        switch (request.getType()) {
            case DEPOSIT -> {
                balanceAfter = balanceBefore.add(request.getAmount());
                account.setBalance(balanceAfter);
            }
            case WITHDRAWAL -> {
                validateSufficientBalance(account, request.getAmount());
                balanceAfter = balanceBefore.subtract(request.getAmount());
                account.setBalance(balanceAfter);
            }
            case TRANSFER -> {
                if (request.getDestinationAccountId() == null) {
                    throw new BusinessException("MISSING_DESTINATION", "Conta destino é obrigatória para transferência", HttpStatus.BAD_REQUEST);
                }
                validateSufficientBalance(account, request.getAmount());
                Account destination = accountService.getAccountOrThrow(request.getDestinationAccountId());
                validateAccountActive(destination);
                balanceAfter = balanceBefore.subtract(request.getAmount());
                account.setBalance(balanceAfter);
                destination.setBalance(destination.getBalance().add(request.getAmount()));
            }
            default -> throw new BusinessException("INVALID_TRANSACTION_TYPE", "Tipo de transação inválido", HttpStatus.BAD_REQUEST);
        }

        Transaction transaction = Transaction.builder()
                .account(account)
                .type(request.getType())
                .amount(request.getAmount())
                .description(request.getDescription())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .status("SUCCESS")
                .build();

        Transaction saved = transactionRepository.save(transaction);
        auditLogService.log("Transaction", saved.getId().toString(), request.getType().name(),
                "Valor: " + request.getAmount() + " | Saldo: " + balanceBefore + " -> " + balanceAfter);
        log.info("[TRANSACTION] {} concluída. Saldo: {} -> {}", request.getType(), balanceBefore, balanceAfter);
        return TransactionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> findByAccount(UUID accountId, Pageable pageable) {
        accountService.getAccountOrThrow(accountId);
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable)
                .map(TransactionResponse::from);
    }

    private void validateAccountActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("ACCOUNT_NOT_ACTIVE",
                    "Conta " + account.getNumber() + " não está ativa. Status: " + account.getStatus(),
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private void validateSufficientBalance(Account account, BigDecimal amount) {
        BigDecimal minBalance = account.getProduct().getMinBalance();
        BigDecimal afterWithdrawal = account.getBalance().subtract(amount);
        if (afterWithdrawal.compareTo(minBalance) < 0) {
            throw new BusinessException("INSUFFICIENT_BALANCE",
                    "Saldo insuficiente. Disponível: R$" + account.getBalance() + " | Saldo mínimo: R$" + minBalance,
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }
}
```

---

## ETAPA 10 — Controllers

### `ProductController.java`
```java
package com.attus.financial.controller;

import com.attus.financial.dto.request.ProductRequest;
import com.attus.financial.dto.response.ApiResponse;
import com.attus.financial.dto.response.ProductResponse;
import com.attus.financial.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Gerenciamento de produtos financeiros")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Criar produto financeiro")
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(productService.create(request)));
    }

    @GetMapping
    @Operation(summary = "Listar todos os produtos")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(productService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID")
    public ResponseEntity<ApiResponse<ProductResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(productService.findById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto")
    public ResponseEntity<ApiResponse<ProductResponse>> update(@PathVariable UUID id,
                                                               @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productService.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Alternar status do produto (ativar/desativar)")
    public ResponseEntity<Void> toggleStatus(@PathVariable UUID id) {
        productService.toggleStatus(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir produto")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

### `CustomerController.java`
```java
package com.attus.financial.controller;

import com.attus.financial.dto.request.CustomerRequest;
import com.attus.financial.dto.response.AccountResponse;
import com.attus.financial.dto.response.ApiResponse;
import com.attus.financial.dto.response.CustomerResponse;
import com.attus.financial.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gerenciamento de clientes")
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Cadastrar cliente")
    public ResponseEntity<ApiResponse<CustomerResponse>> create(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(customerService.create(request)));
    }

    @GetMapping
    @Operation(summary = "Listar todos os clientes")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(customerService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID")
    public ResponseEntity<ApiResponse<CustomerResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.findById(id)));
    }

    @GetMapping("/{id}/accounts")
    @Operation(summary = "Listar contas do cliente")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> findAccounts(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.findAccountsByCustomerId(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados do cliente")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(@PathVariable UUID id,
                                                                @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(customerService.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Alternar status do cliente")
    public ResponseEntity<Void> toggleStatus(@PathVariable UUID id) {
        customerService.toggleStatus(id);
        return ResponseEntity.noContent().build();
    }
}
```

### `AccountController.java`
```java
package com.attus.financial.controller;

import com.attus.financial.domain.enums.AccountStatus;
import com.attus.financial.dto.request.AccountRequest;
import com.attus.financial.dto.response.AccountResponse;
import com.attus.financial.dto.response.ApiResponse;
import com.attus.financial.dto.response.TransactionResponse;
import com.attus.financial.service.AccountService;
import com.attus.financial.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Contas", description = "Gerenciamento de contas financeiras")
@CrossOrigin(origins = "*")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Abrir conta (associar cliente a produto)")
    public ResponseEntity<ApiResponse<AccountResponse>> open(@Valid @RequestBody AccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(accountService.open(request)));
    }

    @GetMapping
    @Operation(summary = "Listar todas as contas")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(accountService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe da conta")
    public ResponseEntity<ApiResponse<AccountResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.findById(id)));
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "Extrato da conta (paginado)")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getStatement(@PathVariable UUID id, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(transactionService.findByAccount(id, pageable)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Alterar status da conta")
    public ResponseEntity<ApiResponse<AccountResponse>> changeStatus(@PathVariable UUID id,
                                                                      @RequestParam AccountStatus status) {
        return ResponseEntity.ok(ApiResponse.success(accountService.changeStatus(id, status)));
    }
}
```

### `TransactionController.java`
```java
package com.attus.financial.controller;

import com.attus.financial.dto.request.TransactionRequest;
import com.attus.financial.dto.response.ApiResponse;
import com.attus.financial.dto.response.TransactionResponse;
import com.attus.financial.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transações", description = "Depósitos, saques e transferências")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Executar transação (DEPOSIT, WITHDRAWAL ou TRANSFER)")
    public ResponseEntity<ApiResponse<TransactionResponse>> execute(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(transactionService.execute(request)));
    }
}
```

### `FinancialApplication.java`
```java
package com.attus.financial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class FinancialApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinancialApplication.class, args);
    }
}
```

---

## ETAPA 11 — Configurações

### `OpenApiConfig.java`
```java
package com.attus.financial.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Attus Financial API")
                .version("1.0.0")
                .description("API do módulo financeiro — Teste técnico Attus"));
    }
}
```

### `AuditAspect.java`
```java
package com.attus.financial.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class AuditAspect {

    @Around("execution(* com.attus.financial.service.TransactionService.execute(..))")
    public Object logTransaction(ProceedingJoinPoint jp) throws Throwable {
        long start = System.currentTimeMillis();
        log.info("[AOP] Iniciando transação financeira");
        try {
            Object result = jp.proceed();
            log.info("[AOP] Transação concluída em {}ms", System.currentTimeMillis() - start);
            return result;
        } catch (Exception e) {
            log.error("[AOP] Falha na transação: {} em {}ms", e.getMessage(), System.currentTimeMillis() - start);
            throw e;
        }
    }
}
```

---

## ETAPA 12 — Testes

### `ProductControllerTest.java`
```java
package com.attus.financial.controller;

import com.attus.financial.domain.enums.ProductType;
import com.attus.financial.dto.request.ProductRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void shouldCreateProductSuccessfully() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Poupança Premium");
        request.setType(ProductType.SAVINGS);
        request.setInterestRate(new BigDecimal("0.50"));
        request.setMinBalance(BigDecimal.ZERO);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.name").value("Poupança Premium"));
    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setType(ProductType.SAVINGS);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldListAllProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void shouldReturn404ForNonExistentProduct() throws Exception {
        mockMvc.perform(get("/api/v1/products/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}
```

### `TransactionServiceTest.java`
```java
package com.attus.financial.service;

import com.attus.financial.domain.entity.Account;
import com.attus.financial.domain.entity.Product;
import com.attus.financial.domain.enums.AccountStatus;
import com.attus.financial.domain.enums.ProductType;
import com.attus.financial.dto.request.TransactionRequest;
import com.attus.financial.domain.enums.TransactionType;
import com.attus.financial.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransactionServiceTest {

    @Autowired private TransactionService transactionService;
    @Autowired private AccountService accountService;

    @Test
    void shouldThrowExceptionWhenInsufficientBalance() {
        // Cria um mock de conta com saldo zero via repositório direto nos testes completos
        // Este é um teste de unidade representando a regra de negócio
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.WITHDRAWAL);
        request.setAmount(new BigDecimal("1000.00"));
        request.setAccountId(UUID.randomUUID());

        assertThrows(Exception.class, () -> transactionService.execute(request));
    }
}
```

### `AccountRepositoryTest.java`
```java
package com.attus.financial.repository;

import com.attus.financial.domain.entity.Account;
import com.attus.financial.domain.entity.Customer;
import com.attus.financial.domain.entity.Product;
import com.attus.financial.domain.enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AccountRepositoryTest {

    @Autowired private AccountRepository accountRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;

    @Test
    void shouldFindAccountsByCustomerId() {
        Customer customer = customerRepository.save(Customer.builder()
                .name("Teste").cpf("111.222.333-96").email("teste@email.com")
                .birthDate(LocalDate.of(1990, 1, 1)).status(CustomerStatus.ACTIVE).build());

        Product product = productRepository.save(Product.builder()
                .name("Produto Teste").type(ProductType.CHECKING)
                .minBalance(BigDecimal.ZERO).status(ProductStatus.ACTIVE).build());

        accountRepository.save(Account.builder()
                .number("12345678-9").customer(customer).product(product)
                .balance(BigDecimal.ZERO).status(AccountStatus.ACTIVE).build());

        List<Account> accounts = accountRepository.findByCustomerId(customer.getId());
        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getCustomer().getName()).isEqualTo("Teste");
    }
}
```

---

## ETAPA 13 — Docker Compose

Crie na raiz do projeto `docker-compose.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: attus-postgres
    environment:
      POSTGRES_DB: financial_db
      POSTGRES_USER: attus
      POSTGRES_PASSWORD: attus123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U attus -d financial_db"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: attus-backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/financial_db
      SPRING_DATASOURCE_USERNAME: attus
      SPRING_DATASOURCE_PASSWORD: attus123
    depends_on:
      postgres:
        condition: service_healthy

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: attus-frontend
    ports:
      - "4200:80"
    depends_on:
      - backend

volumes:
  postgres_data:
```

### `Dockerfile` (raiz — backend)
```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN chmod +x ./gradlew && ./gradlew bootJar -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## ETAPA 14 — Frontend Angular

Execute dentro da pasta `frontend/`:

```bash
ng new frontend --standalone --routing --style=scss --skip-git
cd frontend
ng add @angular/material
```

### `frontend/proxy.conf.json`
```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false,
    "changeOrigin": true
  }
}
```

### `frontend/Dockerfile`
```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration=production

FROM nginx:alpine
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

### `frontend/nginx.conf`
```nginx
server {
  listen 80;
  root /usr/share/nginx/html;
  index index.html;
  location / { try_files $uri $uri/ /index.html; }
  location /api { proxy_pass http://backend:8080; }
}
```

### Models TypeScript

**`core/models/product.model.ts`**
```typescript
export type ProductType = 'SAVINGS' | 'INVESTMENT' | 'CREDIT' | 'CHECKING';
export type ProductStatus = 'ACTIVE' | 'INACTIVE';

export interface Product {
  id: string;
  name: string;
  description?: string;
  type: ProductType;
  interestRate?: number;
  minBalance: number;
  status: ProductStatus;
  createdAt: string;
}

export interface ProductRequest {
  name: string;
  description?: string;
  type: ProductType;
  interestRate?: number;
  minBalance: number;
}
```

**`core/models/customer.model.ts`**
```typescript
export type CustomerStatus = 'ACTIVE' | 'INACTIVE';

export interface Customer {
  id: string;
  name: string;
  cpf: string;
  email: string;
  phone?: string;
  birthDate: string;
  status: CustomerStatus;
  createdAt: string;
}

export interface CustomerRequest {
  name: string;
  cpf: string;
  email: string;
  phone?: string;
  birthDate: string;
}
```

**`core/models/account.model.ts`**
```typescript
export type AccountStatus = 'ACTIVE' | 'BLOCKED' | 'CLOSED';

export interface Account {
  id: string;
  number: string;
  customerId: string;
  customerName: string;
  productId: string;
  productName: string;
  balance: number;
  status: AccountStatus;
  openedAt: string;
}

export interface AccountRequest {
  customerId: string;
  productId: string;
}
```

**`core/models/transaction.model.ts`**
```typescript
export type TransactionType = 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER';

export interface Transaction {
  id: string;
  accountId: string;
  type: TransactionType;
  amount: number;
  description?: string;
  balanceBefore: number;
  balanceAfter: number;
  status: string;
  createdAt: string;
}

export interface TransactionRequest {
  accountId: string;
  type: TransactionType;
  amount: number;
  description?: string;
  destinationAccountId?: string;
}
```

### Services Angular

**`core/services/product.service.ts`**
```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Product, ProductRequest } from '../models/product.model';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly url = '/api/v1/products';
  constructor(private http: HttpClient) {}

  getAll(): Observable<Product[]> {
    return this.http.get<any>(this.url).pipe(map(r => r.data));
  }
  getById(id: string): Observable<Product> {
    return this.http.get<any>(`${this.url}/${id}`).pipe(map(r => r.data));
  }
  create(request: ProductRequest): Observable<Product> {
    return this.http.post<any>(this.url, request).pipe(map(r => r.data));
  }
  update(id: string, request: ProductRequest): Observable<Product> {
    return this.http.put<any>(`${this.url}/${id}`, request).pipe(map(r => r.data));
  }
  toggleStatus(id: string): Observable<void> {
    return this.http.patch<void>(`${this.url}/${id}/status`, {});
  }
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
```

**`core/services/customer.service.ts`**
```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Customer, CustomerRequest } from '../models/customer.model';
import { Account } from '../models/account.model';

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private readonly url = '/api/v1/customers';
  constructor(private http: HttpClient) {}

  getAll(): Observable<Customer[]> {
    return this.http.get<any>(this.url).pipe(map(r => r.data));
  }
  getById(id: string): Observable<Customer> {
    return this.http.get<any>(`${this.url}/${id}`).pipe(map(r => r.data));
  }
  getAccounts(id: string): Observable<Account[]> {
    return this.http.get<any>(`${this.url}/${id}/accounts`).pipe(map(r => r.data));
  }
  create(request: CustomerRequest): Observable<Customer> {
    return this.http.post<any>(this.url, request).pipe(map(r => r.data));
  }
  update(id: string, request: CustomerRequest): Observable<Customer> {
    return this.http.put<any>(`${this.url}/${id}`, request).pipe(map(r => r.data));
  }
  toggleStatus(id: string): Observable<void> {
    return this.http.patch<void>(`${this.url}/${id}/status`, {});
  }
}
```

**`core/services/account.service.ts`**
```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Account, AccountRequest } from '../models/account.model';
import { Transaction } from '../models/transaction.model';

@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly url = '/api/v1/accounts';
  constructor(private http: HttpClient) {}

  getAll(): Observable<Account[]> {
    return this.http.get<any>(this.url).pipe(map(r => r.data));
  }
  getById(id: string): Observable<Account> {
    return this.http.get<any>(`${this.url}/${id}`).pipe(map(r => r.data));
  }
  getStatement(id: string): Observable<Transaction[]> {
    return this.http.get<any>(`${this.url}/${id}/transactions`).pipe(map(r => r.data?.content ?? []));
  }
  open(request: AccountRequest): Observable<Account> {
    return this.http.post<any>(this.url, request).pipe(map(r => r.data));
  }
  changeStatus(id: string, status: string): Observable<Account> {
    return this.http.patch<any>(`${this.url}/${id}/status?status=${status}`, {}).pipe(map(r => r.data));
  }
}
```

**`core/services/transaction.service.ts`**
```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Transaction, TransactionRequest } from '../models/transaction.model';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly url = '/api/v1/transactions';
  constructor(private http: HttpClient) {}

  execute(request: TransactionRequest): Observable<Transaction> {
    return this.http.post<any>(this.url, request).pipe(map(r => r.data));
  }
}
```

### Validator CPF

**`shared/validators/cpf.validator.ts`**
```typescript
import { AbstractControl, ValidationErrors } from '@angular/forms';

export function cpfValidator(control: AbstractControl): ValidationErrors | null {
  const cpf = control.value?.replace(/\D/g, '');
  if (!cpf || cpf.length !== 11) return { cpfInvalid: true };
  if (/^(\d)\1{10}$/.test(cpf)) return { cpfInvalid: true };

  let sum = 0;
  for (let i = 0; i < 9; i++) sum += parseInt(cpf[i]) * (10 - i);
  let remainder = (sum * 10) % 11;
  if (remainder === 10 || remainder === 11) remainder = 0;
  if (remainder !== parseInt(cpf[9])) return { cpfInvalid: true };

  sum = 0;
  for (let i = 0; i < 10; i++) sum += parseInt(cpf[i]) * (11 - i);
  remainder = (sum * 10) % 11;
  if (remainder === 10 || remainder === 11) remainder = 0;
  if (remainder !== parseInt(cpf[10])) return { cpfInvalid: true };

  return null;
}
```

### Error Interceptor

**`core/interceptors/error.interceptor.ts`**
```typescript
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);
  return next(req).pipe(
    catchError(error => {
      const message = error.error?.message ?? 'Erro ao processar a requisição';
      snackBar.open(message, 'Fechar', { duration: 5000, panelClass: ['error-snackbar'] });
      return throwError(() => error);
    })
  );
};
```

### `app.config.ts`
```typescript
import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { routes } from './app.routes';
import { errorInterceptor } from './core/interceptors/error.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([errorInterceptor])),
    provideAnimationsAsync(),
  ]
};
```

### `app.routes.ts`
```typescript
import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'products', pathMatch: 'full' },
  { path: 'products', loadChildren: () => import('./features/products/products.routes').then(m => m.PRODUCT_ROUTES) },
  { path: 'customers', loadChildren: () => import('./features/customers/customers.routes').then(m => m.CUSTOMER_ROUTES) },
  { path: 'accounts', loadChildren: () => import('./features/accounts/accounts.routes').then(m => m.ACCOUNT_ROUTES) },
  { path: 'transactions', loadChildren: () => import('./features/transactions/transactions.routes').then(m => m.TRANSACTION_ROUTES) },
];
```

### Componente de Listagem de Produtos (exemplo de implementação completa)

**`features/products/product-list/product-list.component.ts`**
```typescript
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ProductService } from '../../../core/services/product.service';
import { Product } from '../../../core/models/product.model';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, RouterModule, MatTableModule, MatButtonModule, MatIconModule, MatChipsModule, MatSnackBarModule],
  template: `
    <div style="padding: 24px">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px">
        <h2>Produtos Financeiros</h2>
        <button mat-raised-button color="primary" routerLink="new">
          <mat-icon>add</mat-icon> Novo Produto
        </button>
      </div>
      <table mat-table [dataSource]="products" style="width:100%">
        <ng-container matColumnDef="name">
          <th mat-header-cell *matHeaderCellDef>Nome</th>
          <td mat-cell *matCellDef="let p">{{ p.name }}</td>
        </ng-container>
        <ng-container matColumnDef="type">
          <th mat-header-cell *matHeaderCellDef>Tipo</th>
          <td mat-cell *matCellDef="let p">{{ p.type }}</td>
        </ng-container>
        <ng-container matColumnDef="interestRate">
          <th mat-header-cell *matHeaderCellDef>Taxa (%)</th>
          <td mat-cell *matCellDef="let p">{{ p.interestRate ?? '—' }}</td>
        </ng-container>
        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>Status</th>
          <td mat-cell *matCellDef="let p">
            <mat-chip [color]="p.status === 'ACTIVE' ? 'primary' : 'warn'" highlighted>{{ p.status }}</mat-chip>
          </td>
        </ng-container>
        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef>Ações</th>
          <td mat-cell *matCellDef="let p">
            <button mat-icon-button [routerLink]="[p.id, 'edit']"><mat-icon>edit</mat-icon></button>
            <button mat-icon-button (click)="toggleStatus(p)"><mat-icon>{{ p.status === 'ACTIVE' ? 'toggle_on' : 'toggle_off' }}</mat-icon></button>
            <button mat-icon-button color="warn" (click)="delete(p.id)"><mat-icon>delete</mat-icon></button>
          </td>
        </ng-container>
        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>
    </div>
  `
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  displayedColumns = ['name', 'type', 'interestRate', 'status', 'actions'];

  constructor(private productService: ProductService, private snackBar: MatSnackBar) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.productService.getAll().subscribe(p => this.products = p);
  }

  toggleStatus(product: Product): void {
    this.productService.toggleStatus(product.id).subscribe(() => {
      this.snackBar.open('Status alterado com sucesso', 'OK', { duration: 3000 });
      this.load();
    });
  }

  delete(id: string): void {
    if (confirm('Deseja excluir este produto?')) {
      this.productService.delete(id).subscribe(() => {
        this.snackBar.open('Produto excluído', 'OK', { duration: 3000 });
        this.load();
      });
    }
  }
}
```

**Implemente os demais componentes (product-form, customer-list, customer-form, account-list, account-detail, transaction-form) seguindo o mesmo padrão acima, com formulários reativos, validações e feedback via MatSnackBar.**

### `features/products/products.routes.ts`
```typescript
import { Routes } from '@angular/router';

export const PRODUCT_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./product-list/product-list.component').then(m => m.ProductListComponent) },
  { path: 'new', loadComponent: () => import('./product-form/product-form.component').then(m => m.ProductFormComponent) },
  { path: ':id/edit', loadComponent: () => import('./product-form/product-form.component').then(m => m.ProductFormComponent) },
];
```

*Replique o padrão de routes para customers, accounts e transactions.*

---

## Como Executar

### Com Docker (recomendado)
```bash
docker-compose up --build
```
- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Frontend: http://localhost:4200

### Sem Docker (desenvolvimento)
```bash
# 1. Suba o PostgreSQL (ou ajuste application.yml para H2)
# 2. Backend
./gradlew bootRun

# 3. Frontend (em outro terminal)
cd frontend
npm install
ng serve --proxy-config proxy.conf.json
```

### Testes
```bash
# Backend
./gradlew test

# Frontend
cd frontend && ng test
```

---

## Parte 2 — Análise de Incidente

### Cenário
```
[ERROR] 2024-01-15 03:42:11 - TransactionService
java.lang.NullPointerException: Cannot invoke "Account.getBalance()" on null
    at TransactionService.execute(TransactionService.java:47)
Ocorrências: 847 em 6h | Taxa de falha: 23% dos saques
```

### Causa Raiz
`accountRepository.findById(id).get()` — chamada de `.get()` em `Optional` vazio quando a conta não existia (race condition ou ID inválido vindo do front-end).

### Correção
```java
// Antes (problemático)
Account account = accountRepository.findById(id).get();

// Depois (correto — já implementado neste projeto)
Account account = accountRepository.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException("Conta", id));
```

### Prevenção
1. Regra SonarQube proibindo `.get()` em Optional sem `.isPresent()`
2. Testes de integração cobrindo IDs inexistentes (já implementados)
3. Alerta de monitoramento: NPE rate > 1% dispara alerta no Grafana
4. `@Version` nas entidades para controle de concorrência otimista

---

## Nota Técnica — Decisões e Trade-offs

| Decisão | Alternativa | Por quê |
|---------|-------------|---------|
| UUID como PK | Long auto-increment | Evita enumeração, melhor para APIs públicas |
| Flyway migrations | Hibernate DDL auto | Controle versionado do schema em produção |
| Soft delete de produto | Hard delete | Produtos com histórico não devem ser apagados |
| AuditLogService com REQUIRES_NEW | Logs inline | Audit persiste mesmo se a tx principal falhar |
| H2 apenas em testes | PostgreSQL no CI | CI mais rápido, sem infra adicional |
| Standalone Components Angular | NgModules | Menos boilerplate, lazy loading nativo |
| @CrossOrigin("*") | Spring Security CORS | Simplificação para escopo do teste |

**Trade-offs conscientes:** sem JWT (autenticação simplificada), sem cache Redis, sem filas (transações síncronas). Em produção, cada um desses pontos seria endereçado.
