CREATE TABLE usuario (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    roles VARCHAR(255) NOT NULL
);

-- Insere o usuário admin padrão
-- A senha 'admin123' encodada com BCrypt
INSERT INTO usuario (username, password, roles)
VALUES ('admin', '$2a$10$kinUEUwzKL/qX60a6nOpaOzVuYuIbyBJkpz8x1QhV3SdIQmzeuv8i', 'ROLE_ADMIN');
