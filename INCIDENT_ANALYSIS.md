# Relatório de Análise de Incidente — INS-2024-0115

**Classificação:** SEV-1 — Crítico  
**Status:** Resolvido  
**Equipe responsável:** Engenharia de Plataforma Financeira  
**Data de abertura:** 15/01/2024 03:42 UTC  
**Data de encerramento:** 15/01/2024 09:17 UTC  
**Duração total:** 5h35min  

---

## 1. Cenário do Incidente

### 1.1 Descrição

A partir das 03:42 UTC do dia 15/01/2024, o serviço `TransactionService` começou a lançar `NullPointerException` de forma recorrente durante operações de saque (`WITHDRAWAL`). O erro afetou 23% de todas as requisições de saque na janela de 6 horas, gerando 847 falhas registradas nos logs de produção antes da contenção.

### 1.2 Logs de Produção

```
[ERROR] 2024-01-15 03:42:11.847 - [attus-backend] [http-nio-8080-exec-7]
TransactionService - Falha na transação
java.lang.NullPointerException: Cannot invoke "com.attus.financial.domain.entity.Account.getBalance()" because "account" is null
    at com.attus.financial.service.TransactionService.execute(TransactionService.java:47)
    at com.attus.financial.controller.TransactionController.execute(TransactionController.java:32)
    at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:885)
    at javax.servlet.http.HttpServlet.service(HttpServlet.java:764)
    ...

[ERROR] 2024-01-15 03:42:34.102 - [attus-backend] [http-nio-8080-exec-3]
TransactionService - Falha na transação
java.lang.NullPointerException: Cannot invoke "com.attus.financial.domain.entity.Account.getBalance()" because "account" is null
    at com.attus.financial.service.TransactionService.execute(TransactionService.java:47)
    ...

[ERROR] 2024-01-15 03:43:01.559 - [attus-backend] [http-nio-8080-exec-12]
TransactionService - Falha na transação
java.lang.NullPointerException: Cannot invoke "com.attus.financial.domain.entity.Account.getBalance()" because "account" is null
    at com.attus.financial.service.TransactionService.execute(TransactionService.java:47)
    ...

[WARN]  2024-01-15 04:01:17.003 - [attus-backend] GlobalExceptionHandler - Erro não tratado capturado
java.lang.NullPointerException: Cannot invoke "com.attus.financial.domain.entity.Account.getBalance()" because "account" is null
    at com.attus.financial.service.TransactionService.execute(TransactionService.java:47)
    ...

[ERROR] 2024-01-15 07:11:44.228 - [attus-backend] [http-nio-8080-exec-19]
TransactionService - Falha na transação
java.lang.NullPointerException: Cannot invoke "com.attus.financial.domain.entity.Account.getBalance()" because "account" is null
    at com.attus.financial.service.TransactionService.execute(TransactionService.java:47)
    ...
```

### 1.3 Métricas do Incidente

| Métrica | Valor |
|---|---|
| Primeira ocorrência registrada | 15/01/2024 03:42:11 UTC |
| Total de erros em 6 horas | 847 ocorrências |
| Taxa de falha em saques | 23% |
| Pico de erros por minuto | 6,2 (às 07:30 UTC) |
| Contas afetadas únicas | 312 |
| Código HTTP retornado | 500 Internal Server Error |
| Alertas disparados no Grafana | 3 (NPE rate, error rate, p99 latency) |

---

## 2. Análise de Causa Raiz

### 2.1 O Problema

O método `execute` da classe `TransactionService` utilizava `Optional.get()` diretamente sobre o resultado de `accountRepository.findById(id)`, sem verificar previamente se o valor estava presente.

Quando um `accountId` inválido (conta inexistente, deletada ou pertencente a outro tenant) era enviado na requisição, o `Optional` retornava vazio. A chamada a `.get()` em um `Optional.empty()` lança `NoSuchElementException`, que era capturada e suprimida por um bloco `catch (Exception e)` genérico no código original, fazendo com que a variável `account` permanecesse `null`. A linha subsequente `account.getBalance()` então lançava `NullPointerException`.

### 2.2 Trecho de Código com o Defeito

```java
// TransactionService.java — versão com defeito (antes da correção)

@Transactional
public TransactionResponse execute(TransactionRequest request) {
    Account account = null;
    try {
        // PROBLEMA: .get() em Optional vazio lança NoSuchElementException
        account = accountRepository.findById(request.getAccountId()).get();
    } catch (Exception e) {
        // PROBLEMA: exceção suprimida, account permanece null
        log.warn("Erro ao buscar conta: {}", e.getMessage());
    }

    BigDecimal balanceBefore = account.getBalance(); // NPE aqui (linha 47) quando account == null
    // ...
}
```

### 2.3 Cadeia de Causas

```
Requisição com accountId inválido
        │
        ▼
accountRepository.findById(id) → Optional.empty()
        │
        ▼
Optional.get() → lança NoSuchElementException
        │
        ▼
catch (Exception e) → exceção suprimida, log apenas warn
        │
        ▼
account == null
        │
        ▼
account.getBalance() → NullPointerException (linha 47)
        │
        ▼
GlobalExceptionHandler captura com HTTP 500
        │
        ▼
Cliente recebe "Erro interno no servidor" sem detalhe útil
```

### 2.4 Por Que o Erro Surgiu Agora

Uma análise do histórico de deploys revelou que a versão `v1.4.2`, publicada em 14/01/2024 às 23:10 UTC, introduziu uma migração de dados que encerrou (`CLOSED`) aproximadamente 1.400 contas inativas. O front-end mobile, no entanto, continuava exibindo essas contas em cache local por até 8 horas, permitindo que usuários tentassem realizar saques em contas que já não constavam no banco como elegíveis — ou cujos IDs foram alterados durante a migração.

---

## 3. Correção Aplicada

### 3.1 Antes (código defeituoso)

```java
@Transactional
public TransactionResponse execute(TransactionRequest request) {
    Account account = null;
    try {
        account = accountRepository.findById(request.getAccountId()).get();
    } catch (Exception e) {
        log.warn("Erro ao buscar conta: {}", e.getMessage());
    }

    BigDecimal balanceBefore = account.getBalance(); // NPE quando account == null
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
        // ...
    }
    // ...
}
```

### 3.2 Depois (código corrigido)

```java
@Transactional
public TransactionResponse execute(TransactionRequest request) {
    log.info("[TRANSACTION] Iniciando {} de R${} na conta {}",
            request.getType(), request.getAmount(), request.getAccountId());

    // CORREÇÃO: uso de orElseThrow() — lança ResourceNotFoundException (HTTP 404) de forma controlada
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
                throw new BusinessException("MISSING_DESTINATION",
                        "Conta destino é obrigatória para transferência",
                        HttpStatus.BAD_REQUEST);
            }
            validateSufficientBalance(account, request.getAmount());
            Account destination = accountService.getAccountOrThrow(
                    request.getDestinationAccountId());
            validateAccountActive(destination);
            balanceAfter = balanceBefore.subtract(request.getAmount());
            account.setBalance(balanceAfter);
            destination.setBalance(destination.getBalance().add(request.getAmount()));
        }
        default -> throw new BusinessException("INVALID_TRANSACTION_TYPE",
                "Tipo de transação inválido", HttpStatus.BAD_REQUEST);
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
    auditLogService.log("Transaction", saved.getId().toString(),
            request.getType().name(),
            "Valor: " + request.getAmount() + " | Saldo: " + balanceBefore + " -> " + balanceAfter);

    log.info("[TRANSACTION] {} concluída. Saldo: {} -> {}",
            request.getType(), balanceBefore, balanceAfter);
    return TransactionResponse.from(saved);
}
```

### 3.3 Método auxiliar utilizado na correção

```java
// AccountService.java
public Account getAccountOrThrow(UUID id) {
    return accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Conta", id));
}
```

### 3.4 Comparação dos Comportamentos

| Comportamento | Antes (defeituoso) | Depois (corrigido) |
|---|---|---|
| Conta não encontrada | `NullPointerException` → HTTP 500 | `ResourceNotFoundException` → HTTP 404 |
| Mensagem de erro para o cliente | "Erro interno no servidor" | "Conta com id X não encontrada" |
| Log gerado | `ERROR` com stack trace longo | `WARN` com mensagem clara e controlada |
| Transação registrada no banco | Não (erro antes do save) | Não (exceção lançada antes do save) |
| Auditoria do evento | Nenhuma | Log de tentativa inválida registrado |

---

## 4. Medidas Preventivas

### 4.1 Regra SonarQube — Proibição de `Optional.get()` sem verificação

Adicionar ao perfil de qualidade do projeto a regra `java:S3655` do SonarQube, que bloqueia o uso de `Optional.get()` sem chamada prévia a `isPresent()` ou `isEmpty()`. O pipeline de CI deve ser configurado para falhar o build caso essa regra seja violada.

```xml
<!-- sonar-project.properties -->
sonar.issue.ignore.multicriteria=e1
sonar.java.checkstyle.reportPaths=build/reports/checkstyle/main.xml

<!-- quality-gate: bloquear merge se houver blocker/critical issues -->
sonar.qualitygate.wait=true
```

A regra preferida no projeto é substituir qualquer `Optional.get()` por `.orElseThrow()` com uma exceção de domínio significativa, conforme já padronizado no método `getAccountOrThrow`.

### 4.2 Testes de Integração para IDs Inexistentes

Criar e manter testes de integração que cubram explicitamente o cenário de recursos não encontrados em todos os endpoints críticos:

```java
// TransactionControllerTest.java
@Test
void shouldReturn404WhenAccountNotFound() throws Exception {
    TransactionRequest request = new TransactionRequest();
    request.setAccountId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    request.setType(TransactionType.WITHDRAWAL);
    request.setAmount(new BigDecimal("100.00"));

    mockMvc.perform(post("/api/v1/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
            .andExpect(jsonPath("$.status").value("error"));
}

@Test
void shouldReturn422WhenAccountIsClosed() throws Exception {
    // Cria conta e a encerra antes de tentar transacionar
    UUID accountId = createAndCloseAccount();

    TransactionRequest request = new TransactionRequest();
    request.setAccountId(accountId);
    request.setType(TransactionType.WITHDRAWAL);
    request.setAmount(new BigDecimal("50.00"));

    mockMvc.perform(post("/api/v1/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_ACTIVE"));
}
```

Esses testes devem ser executados em cada PR e integrados ao pipeline de CI como critério de aceite obrigatório.

### 4.3 Alerta de Monitoramento no Grafana

Configurar alertas no Grafana com base nas métricas expostas pelo Actuator do Spring Boot e coletadas pelo Prometheus:

**Alerta 1 — Taxa de NullPointerException:**
```yaml
# grafana-alert-npe.yml
alert: HighNPERate
expr: |
  rate(http_server_requests_seconds_count{
    exception="NullPointerException", status="500"
  }[5m]) > 0.01
for: 2m
labels:
  severity: critical
  team: platform-engineering
annotations:
  summary: "Taxa de NPE acima de 1% em TransactionService"
  runbook: "https://wiki.attus.internal/runbooks/transaction-npe"
```

**Alerta 2 — Taxa de erros HTTP 500 em transações:**
```yaml
alert: TransactionErrorRateHigh
expr: |
  rate(http_server_requests_seconds_count{
    uri="/api/v1/transactions", status=~"5.."
  }[5m])
  /
  rate(http_server_requests_seconds_count{
    uri="/api/v1/transactions"
  }[5m]) > 0.05
for: 3m
labels:
  severity: high
annotations:
  summary: "Mais de 5% das transações estão falhando com 5xx"
```

O alerta deve notificar o canal `#on-call-fintech` no Slack e abrir um ticket automático no PagerDuty com severidade P1.

### 4.4 Controle de Concorrência Otimista com `@Version`

Adicionar a anotação `@Version` às entidades `Account` e `Transaction` para evitar condições de corrida em operações concorrentes sobre o mesmo registro:

```java
// Account.java
@Entity
@Table(name = "tb_accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version; // Hibernate lança OptimisticLockException em conflito

    // ... demais campos
}
```

```java
// GlobalExceptionHandler.java — tratar a exceção de lock
@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(
        ObjectOptimisticLockingFailureException ex) {
    log.warn("[CONCURRENCY] Conflito de versão detectado: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error("CONCURRENT_MODIFICATION",
                    "A conta foi modificada por outra operação. Tente novamente."));
}
```

Com `@Version`, duas transações simultâneas sobre a mesma conta resultarão em uma `OptimisticLockException` controlada (HTTP 409) ao invés de race conditions silenciosas que corrompem o saldo.

### 4.5 Validação de Entrada no Nível do Controller

Adicionar uma camada de validação prévia no `TransactionController` que rejeite requisições com `accountId` semanticamente inválidos antes de chegarem à camada de serviço:

```java
// TransactionController.java
@PostMapping
public ResponseEntity<ApiResponse<TransactionResponse>> execute(
        @Valid @RequestBody TransactionRequest request) {

    // Validação de regra de negócio antes de delegar ao service
    if (request.getType() == TransactionType.TRANSFER
            && request.getDestinationAccountId() == null) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("MISSING_DESTINATION",
                        "Conta destino é obrigatória para transferências"));
    }

    if (request.getType() == TransactionType.TRANSFER
            && request.getAccountId().equals(request.getDestinationAccountId())) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("SAME_ACCOUNT_TRANSFER",
                        "Conta de origem e destino não podem ser iguais"));
    }

    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(transactionService.execute(request)));
}
```

Adicionalmente, o `TransactionRequest` deve ser reforçado com validações de formato:

```java
// TransactionRequest.java
@Data
public class TransactionRequest {

    @NotNull(message = "Conta de origem é obrigatória")
    private UUID accountId;

    @NotNull(message = "Tipo da transação é obrigatório")
    private TransactionType type;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Digits(integer = 13, fraction = 2, message = "Valor monetário inválido")
    private BigDecimal amount;

    @Size(max = 200, message = "Descrição deve ter no máximo 200 caracteres")
    private String description;

    private UUID destinationAccountId;
}
```

---

## 5. Linha do Tempo do Incidente (Postmortem)

| Horário (UTC) | Evento |
|---|---|
| 14/01/2024 23:10 | Deploy da versão `v1.4.2` concluído com sucesso. Migração encerra 1.400 contas inativas no banco. |
| 15/01/2024 03:42 | Primeira `NullPointerException` registrada. Usuário tenta saque em conta migrada para `CLOSED`. |
| 15/01/2024 03:50 | Grafana dispara alerta de taxa de NPE acima de 1% — notificação enviada ao canal `#on-call-fintech`. |
| 15/01/2024 04:05 | Engenheiro de plantão (João Mendes) assume o incidente. Inicia triagem dos logs. |
| 15/01/2024 04:22 | Incidente classificado como SEV-1. Time de engenharia de plataforma convocado. |
| 15/01/2024 04:45 | Causa raiz identificada: `Optional.get()` sem verificação na linha 47 de `TransactionService`. |
| 15/01/2024 05:10 | Medida de contenção imediata: feature flag desativa temporariamente saques no app mobile. |
| 15/01/2024 05:30 | Correção implementada em branch `hotfix/INS-2024-0115`. |
| 15/01/2024 06:15 | Testes de integração executados localmente: 100% passando. |
| 15/01/2024 06:40 | Pull request aprovado após revisão de dois engenheiros sêniores. |
| 15/01/2024 07:00 | Pipeline de CI/CD iniciado. Build e testes automatizados concluídos em 12 minutos. |
| 15/01/2024 07:20 | Deploy da versão `v1.4.3` concluído em produção (zero downtime via rolling update). |
| 15/01/2024 07:35 | Feature flag de saques reativada. Monitoramento em modo de observação intensiva. |
| 15/01/2024 08:00 | Taxa de erro normalizada. Zero ocorrências de NPE nos últimos 25 minutos. |
| 15/01/2024 09:17 | Incidente encerrado formalmente. Postmortem agendado para 16/01/2024. |
| 16/01/2024 14:00 | Reunião de postmortem realizada. Ações de prevenção aprovadas pela equipe. |

---

## 6. Avaliação de Impacto

### 6.1 Usuários Afetados

| Grupo | Quantidade | Tipo de Impacto |
|---|---|---|
| Usuários que tentaram saque | 312 | Falha na operação, mensagem de erro genérica |
| Usuários com conta migrada | 1.400 | Contas exibidas no app por até 8h após encerramento |
| Usuários com conta ativa | 0 | Nenhum impacto (contas ativas não foram afetadas) |

### 6.2 Transações com Falha

| Métrica | Valor |
|---|---|
| Total de tentativas de saque no período | 3.683 |
| Saques com falha por NPE | 847 |
| Taxa de falha | 23% |
| Valor total das transações que falharam | R$ 412.350,00 |
| Transações com débito incorreto no saldo | 0 (nenhuma) |
| Transações duplicadas criadas | 0 (nenhuma) |

Importante: **nenhuma transação foi parcialmente processada**. O NPE ocorria antes da operação de débito/crédito, garantindo que nenhum saldo foi alterado de forma incorreta. Não houve impacto financeiro real nas contas dos clientes.

### 6.3 Impacto Financeiro

| Item | Estimativa |
|---|---|
| Perda de receita por transações não processadas | R$ 1.230,00 (tarifas não cobradas) |
| Custo de horas de engenharia no incidente | R$ 8.400,00 (14 horas × 3 engenheiros) |
| Potencial reembolso de tarifas por SLA | R$ 0,00 (não aplicável — clientes não foram cobrados) |
| Impacto estimado em NPS / satisfação | Monitoramento indica 47 reclamações no suporte |
| **Total estimado** | **R$ 9.630,00** |

### 6.4 Integridade dos Dados

Após auditoria completa realizada em 15/01/2024 às 10:00 UTC:

- **Saldos:** Nenhuma inconsistência encontrada. Todas as 847 transações falharam antes de qualquer alteração de saldo.
- **Registros de auditoria:** `AuditLog` não registrou entradas para as transações com falha (esperado, pois o log é criado após a persistência).
- **Tabela `tb_transactions`:** Nenhum registro órfão ou incompleto encontrado.
- **Integridade referencial:** Verificada e íntegra em todas as tabelas.

### 6.5 Violações de SLA

| SLA | Contratado | Realizado |
|---|---|---|
| Disponibilidade mensal | 99,9% (43min downtime/mês) | 99,87% (funcionalidade parcialmente degradada por 5h35min) |
| Tempo de resposta p99 | < 500ms | 1.840ms (pico às 07:30 UTC) |
| Taxa de erro | < 0,1% | 23% (apenas para saques, no pico) |

O incidente não resultou em indisponibilidade total do sistema. A degradação foi localizada no endpoint de saques. Os demais endpoints (depósitos, consultas, transferências entre contas ativas) operaram normalmente durante todo o período.

---

## 7. Ações de Acompanhamento

| Ação | Responsável | Prazo | Status |
|---|---|---|---|
| Implementar regra SonarQube `java:S3655` no perfil de qualidade | Equipe DevOps | 22/01/2024 | Pendente |
| Adicionar testes de integração para todos os endpoints com ID inexistente | Equipe Backend | 19/01/2024 | Pendente |
| Configurar alerta Grafana para NPE rate > 1% | Equipe SRE | 17/01/2024 | Pendente |
| Adicionar `@Version` nas entidades `Account` e `Transaction` | Equipe Backend | 26/01/2024 | Pendente |
| Implementar invalidação de cache no app mobile após mudança de status de conta | Equipe Mobile | 31/01/2024 | Pendente |
| Revisar todos os `Optional.get()` no codebase atual | Equipe Backend | 19/01/2024 | Pendente |
| Adicionar smoke tests pós-deploy no pipeline | Equipe DevOps | 26/01/2024 | Pendente |
| Documentar runbook para incidentes de NPE em TransactionService | Equipe Platform | 22/01/2024 | Pendente |

---

*Documento preparado pela Equipe de Engenharia de Plataforma Financeira — Attus.*  
*Revisado por: João Mendes (SRE), Maria Costa (Tech Lead), Carlos Oliveira (Arquiteto de Soluções).*  
*Versão: 1.0 | Data de publicação: 16/01/2024*
