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
