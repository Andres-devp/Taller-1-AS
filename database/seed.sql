CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(100) PRIMARY KEY,
    password_hash VARCHAR(100) NOT NULL
);

-- Academic account for demonstrations: student / laboratory-password
MERGE INTO users KEY(username) VALUES
    ('student', '$2a$10$nTjCZ6hIb5qmI9WLkwAvBukm4tiA/pbHVtyd08dzOlEkY9QJS.w5S');
