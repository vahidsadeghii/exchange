CREATE TABLE user_profile
(
    id               BIGSERIAL PRIMARY KEY,

    keycloak_user_id VARCHAR(255) UNIQUE,

    first_name       VARCHAR(255),
    last_name        VARCHAR(255),
    phone_number     VARCHAR(50),

    email            VARCHAR(255) UNIQUE,

    gender_type      VARCHAR(50),
    user_status      VARCHAR(50),

    address          VARCHAR(500),

    avatar_id        VARCHAR(255),
    avatar_link      VARCHAR(500),
    file_name        VARCHAR(255),

    birthday         DATE,

    password         VARCHAR(255),

    username         VARCHAR(255) UNIQUE,

    create_date      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_date      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
