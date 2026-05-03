CREATE TABLE event_info
(
    id           BIGSERIAL PRIMARY KEY,
    tag          VARCHAR(255),
    title        VARCHAR(255),
    service_name VARCHAR(255),
    event        VARCHAR(1224),
    create_date  TIMESTAMP NULL
);