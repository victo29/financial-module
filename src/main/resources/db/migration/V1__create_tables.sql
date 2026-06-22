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
