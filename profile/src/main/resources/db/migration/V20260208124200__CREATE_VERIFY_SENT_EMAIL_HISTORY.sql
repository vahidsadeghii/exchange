CREATE TABLE verify_sent_email_history
(
    id                 UUID PRIMARY KEY      DEFAULT gen_random_uuid(),

    user_id            VARCHAR(255),
    email              VARCHAR(255) NOT NULL,

    verification_code  VARCHAR(100),

    expired_date       TIMESTAMP,

    try_count          INT          NOT NULL DEFAULT 0,

    is_used            BOOLEAN      NOT NULL DEFAULT FALSE,

    status             VARCHAR(50)  NOT NULL,

    create_date        TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP             DEFAULT CURRENT_TIMESTAMP
);