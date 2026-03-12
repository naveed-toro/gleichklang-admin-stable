CREATE TABLE IF NOT EXISTS filter
(
    id                       BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    DTYPE                    VARCHAR(31) NOT NULL,
    legacy_id                VARCHAR(255),
    change_date              DATETIME,
    create_date              DATETIME,

    left_filter_id           BIGINT,
    right_filter_id          BIGINT,
    binary_operator          VARCHAR(255),

    single_filter_id         BIGINT,
    unary_operator           VARCHAR(255),

    template_filter_id       BIGINT,
    template_name            VARCHAR(255),

    enum_value               VARCHAR(255),
    choice_question_id       BIGINT,
    choice_id                BIGINT,
    radius                   INT,
    zip_id                   BIGINT,
    locatable_id             BIGINT,

    FOREIGN KEY (left_filter_id)   REFERENCES filter (id),
    FOREIGN KEY (right_filter_id)   REFERENCES filter (id),
    FOREIGN KEY (single_filter_id)   REFERENCES filter (id),
    FOREIGN KEY (template_filter_id)   REFERENCES filter (id),
    FOREIGN KEY (choice_question_id)   REFERENCES question (id),
    FOREIGN KEY (choice_id)   REFERENCES choice (id),
    FOREIGN KEY (zip_id)   REFERENCES locatable (id),
    FOREIGN KEY (locatable_id)   REFERENCES locatable (id)
);