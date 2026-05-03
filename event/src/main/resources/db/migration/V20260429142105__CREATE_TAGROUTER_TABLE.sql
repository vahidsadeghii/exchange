CREATE TABLE tag_router
(
    id          BIGSERIAL PRIMARY KEY,
    tag         VARCHAR(255),
    title_topic VARCHAR(255),
    create_date TIMESTAMP NULL
);