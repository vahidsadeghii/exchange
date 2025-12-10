CREATE TABLE user_profile
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT,
    first_name   VARCHAR(255),
    last_name    VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    email        VARCHAR(255),
    gender_type  VARCHAR(50),
    address      VARCHAR(500) NOT NULL,
    avatar_id    VARCHAR(255),
    avatar_link  VARCHAR(500),
    file_name    VARCHAR(255),
    birthday     DATE         NOT NULL,
    create_date  TIMESTAMP,
    update_date  TIMESTAMP
);
