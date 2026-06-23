# Attus Financial — Módulo Financeiro

API REST para gestão de um módulo financeiro completo com produtos, clientes, contas e transações. Desenvolvida com Spring Boot 3.2, Angular 17 e PostgreSQL como parte do teste técnico Attus.

---

## Stack

| Camada | Tecnologia | Versão |
|---|---|---|
| Linguagem | Java | 17 |
| Backend | Spring Boot | 3.2.5 |
| Banco de dados (produção) | PostgreSQL | 16 |
| Banco de dados (testes) | H2 In-Memory | — |
| Migrations | Flyway | via Spring Boot |
| Documentação | SpringDoc OpenAPI | 2.5.0 |
| Logs / AOP | SLF4J + Logback + Spring AOP | — |
| Frontend | Angular | 17 |
| UI | Angular Material | 17 |

---

## Como Executar

### Com Docker (recomendado)

Pré-requisito: Docker Desktop em execução.

```bash
docker-compose up --build
```

| Serviço | URL |
|---|---|
| API REST | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |
| Frontend | http://localhost:4200 |

Para parar e remover os volumes:

```bash
docker-compose down -v
```

---

## Endpoints da API

A documentação interativa completa está disponível no Swagger UI em `http://localhost:8080/swagger-ui.html`. As tabelas abaixo cobrem todos os endpoints implementados.

### Produtos — `/api/v1/products`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/v1/products` | Criar produto financeiro |
| `GET` | `/api/v1/products` | Listar todos os produtos |
| `GET` | `/api/v1/products/{id}` | Buscar produto por ID |
| `PUT` | `/api/v1/products/{id}` | Atualizar produto |
| `PATCH` | `/api/v1/products/{id}/status` | Alternar status (ACTIVE ↔ INACTIVE) |
| `DELETE` | `/api/v1/products/{id}` | Excluir produto (bloqueado se houver contas vinculadas) |

**Exemplo — criar produto**

```json
// POST /api/v1/products
{
  "name": "Conta Poupança Plus",
  "description": "Poupança com rendimento mensal",
  "type": "SAVINGS",
  "interestRate": 0.50,
  "minBalance": 0.00
}
```

```json
// 201 Created
{
  "status": "success",
  "data": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "name": "Conta Poupança Plus",
    "type": "SAVINGS",
    "interestRate": 0.50,
    "minBalance": 0.00,
    "status": "ACTIVE",
    "createdAt": "2024-06-23T10:00:00"
  }
}
```

Tipos de produto aceitos: `SAVINGS`, `INVESTMENT`, `CREDIT`, `CHECKING`.

---

### Clientes — `/api/v1/customers`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/v1/customers` | Cadastrar cliente |
| `GET` | `/api/v1/customers` | Listar todos os clientes |
| `GET` | `/api/v1/customers/{id}` | Buscar cliente por ID |
| `GET` | `/api/v1/customers/{id}/accounts` | Listar contas do cliente |
| `PUT` | `/api/v1/customers/{id}` | Atualizar dados do cliente |
| `PATCH` | `/api/v1/customers/{id}/status` | Alternar status (ACTIVE ↔ INACTIVE) |

**Exemplo — cadastrar cliente**

```json
// POST /api/v1/customers
{
  "name": "João Silva",
  "cpf": "123.456.789-09",
  "email": "joao.silva@email.com",
  "phone": "(11) 99999-1111",
  "birthDate": "1990-05-15"
}
```

```json
// 201 Created
{
  "status": "success",
  "data": {
    "id": "a1b2c3d4-...",
    "name": "João Silva",
    "cpf": "123.456.789-09",
    "email": "joao.silva@email.com",
    "status": "ACTIVE",
    "createdAt": "2024-06-23T10:05:00"
  }
}
```

---

### Contas — `/api/v1/accounts`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/v1/accounts` | Abrir conta (associar cliente a produto) |
| `GET` | `/api/v1/accounts` | Listar todas as contas |
| `GET` | `/api/v1/accounts/{id}` | Detalhe da conta |
| `GET` | `/api/v1/accounts/{id}/transactions` | Extrato paginado |
| `PATCH` | `/api/v1/accounts/{id}/status?status=BLOCKED` | Alterar status da conta |

**Status aceitos:** `ACTIVE`, `BLOCKED`, `CLOSED` (encerramento exige saldo zero).

**Exemplo — abrir conta**

```json
// POST /api/v1/accounts
{
  "customerId": "a1b2c3d4-...",
  "productId": "3fa85f64-..."
}
```

```json
// 201 Created
{
  "status": "success",
  "data": {
    "id": "f7e8d9c0-...",
    "number": "04521876-3",
    "customerName": "João Silva",
    "productName": "Conta Poupança Plus",
    "balance": 0.00,
    "status": "ACTIVE",
    "openedAt": "2024-06-23T10:10:00"
  }
}
```

---

### Transações — `/api/v1/transactions`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/v1/transactions` | Executar transação (depósito, saque ou transferência) |

**Exemplo — depósito**

```json
// POST /api/v1/transactions
{
  "accountId": "f7e8d9c0-...",
  "type": "DEPOSIT",
  "amount": 1500.00,
  "description": "Depósito inicial"
}
```

**Exemplo — transferência**

```json
// POST /api/v1/transactions
{
  "accountId": "f7e8d9c0-...",
  "type": "TRANSFER",
  "amount": 500.00,
  "destinationAccountId": "b2c3d4e5-...",
  "description": "Transferência para conta corrente"
}
```

```json
// 201 Created
{
  "status": "success",
  "data": {
    "id": "c1d2e3f4-...",
    "type": "DEPOSIT",
    "amount": 1500.00,
    "balanceBefore": 0.00,
    "balanceAfter": 1500.00,
    "status": "SUCCESS",
    "createdAt": "2024-06-23T10:15:00"
  }
}
```

Tipos aceitos: `DEPOSIT`, `WITHDRAWAL`, `TRANSFER`. Transferências exigem `destinationAccountId`.

---

### Formato de erros

Todos os erros seguem o mesmo envelope:

```json
{
  "status": "error",
  "code": "INSUFFICIENT_BALANCE",
  "message": "Saldo insuficiente. Disponível: R$100.00 | Saldo mínimo: R$0.00",
  "timestamp": "2024-06-23T10:20:00"
}
```

| Código | HTTP | Situação |
|---|---|---|
| `VALIDATION_ERROR` | 400 | Campos obrigatórios ausentes ou inválidos |
| `RESOURCE_NOT_FOUND` | 404 | Entidade não encontrada pelo ID |
| `CPF_ALREADY_EXISTS` | 409 | CPF já cadastrado |
| `EMAIL_ALREADY_EXISTS` | 409 | E-mail já cadastrado |
| `PRODUCT_IN_USE` | 409 | Produto possui contas vinculadas |
| `PRODUCT_INACTIVE` | 422 | Produto inativo não aceita novas contas |
| `ACCOUNT_NOT_ACTIVE` | 422 | Conta bloqueada ou encerrada |
| `INSUFFICIENT_BALANCE` | 422 | Saldo insuficiente ou violação do saldo mínimo |
| `BALANCE_NOT_ZERO` | 422 | Encerramento exige saldo zero |
| `MISSING_DESTINATION` | 400 | Transferência sem conta destino |

---

## Frontend

### Funcionalidades por módulo

**Produtos** (`/products`)
- Listagem em tabela com nome, tipo, taxa de juros e status
- Formulário reativo de criação e edição com validação inline
- Toggle de status ACTIVE/INACTIVE via botão dedicado
- Exclusão com confirmação via dialog
- Feedback de todas as ações via MatSnackBar

**Clientes** (`/customers`)
- Listagem com CPF, e-mail e status
- Formulário com validação de CPF por algoritmo (dígitos verificadores)
- Validação de unicidade de CPF e e-mail (erro retornado pela API)
- Toggle de status ACTIVE/INACTIVE

**Contas** (`/accounts`)
- Listagem de todas as contas com cliente, produto, saldo e status
- Abertura de conta associando cliente a produto (somente produtos ACTIVE)
- Tela de detalhe com extrato paginado de transações
- Alteração de status: ACTIVE → BLOCKED → CLOSED

**Transações** (`/transactions`)
- Formulário único para depósito, saque e transferência
- Campo de conta destino exibido dinamicamente apenas para TRANSFER
- Bloqueio de operações em contas não ativas
- Exibição do saldo antes e depois na resposta

### Validações implementadas

| Regra | Camada |
|---|---|
| CPF — formato `000.000.000-00` | Backend (Bean Validation) e Frontend (regex) |
| CPF — dígitos verificadores | Frontend (`cpf.validator.ts`) |
| Campos obrigatórios | Backend (Bean Validation) + formulários reativos Angular |
| Valor de transação > 0 | Backend (`@DecimalMin("0.01")`) |
| Saldo mínimo do produto | Backend (`TransactionService.validateSufficientBalance`) |
| Conta deve estar ACTIVE para transações | Backend (`TransactionService.validateAccountActive`) |
| Encerramento de conta exige saldo zero | Backend (`AccountService.changeStatus`) |
| Produto inativo não aceita novas contas | Backend (`AccountService.open`) |
| Produto com contas vinculadas não pode ser excluído | Backend (`ProductService.delete`) |
| Erros HTTP exibidos ao usuário | Frontend (`error.interceptor.ts` + MatSnackBar) |

