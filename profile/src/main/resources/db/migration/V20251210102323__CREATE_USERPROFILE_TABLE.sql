CREATE TABLE user_profile
(
    id              BIGSERIAL PRIMARY KEY,
    first_name      VARCHAR(255),
    last_name       VARCHAR(255),
    phone_number    VARCHAR(50),
    email           VARCHAR(255),
    gender_type     VARCHAR(50),
    address         VARCHAR(500),
    avatar_id       VARCHAR(255),
    avatar_link     VARCHAR(500),
    file_name       VARCHAR(255),
    birthday        DATE,
    password        VARCHAR(255),
    username        VARCHAR(255),
    keycloak_user_id VARCHAR(255),
    create_date     TIMESTAMP,
    update_date     TIMESTAMP
);
