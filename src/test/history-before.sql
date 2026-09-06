INSERT INTO users (name, email, password) VALUES ('Teste A', 'history-a@example.test', 'unused'), ('Teste B', 'history-b@example.test', 'unused');
INSERT INTO customers (user_id, name) VALUES (1, 'Cliente anterior');
INSERT INTO products (user_id, name, stock) VALUES (1, 'Produto anterior', 0);
INSERT INTO sales (user_id, customer_id, total_amount, status) VALUES (1, 1, 10, 'CANCELLED');
INSERT INTO bills (user_id, name, amount, due_date, status, paid_at) VALUES (1, 'Conta anterior', 10, CURRENT_DATE, 'PAID', CURRENT_TIMESTAMP);
