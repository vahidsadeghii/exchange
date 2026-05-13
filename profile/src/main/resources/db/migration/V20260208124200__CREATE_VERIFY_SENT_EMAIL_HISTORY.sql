CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE verify_sent_email_history
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    verification_code VARCHAR(255) NOT NULL,

    expired_date TIMESTAMP NOT NULL,

    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    try_count INT NOT NULL DEFAULT 0,

    sent_date DATE NOT NULL,

    status VARCHAR(50) NOT NULL,

    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);