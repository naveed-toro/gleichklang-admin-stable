CREATE TABLE IF NOT EXISTS activator
(
    id                       BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    DTYPE                    VARCHAR(31) NOT NULL,
    legacy_id                VARCHAR(255),
    change_date              DATETIME,
    create_date              DATETIME,
    activating_question_id   BIGINT NOT NULL,
    enables_questionnaire_id BIGINT,
    enables_question_id      BIGINT,
    FOREIGN KEY (activating_question_id)   REFERENCES question (id),
    FOREIGN KEY (enables_questionnaire_id) REFERENCES questionnaire (id),
    FOREIGN KEY (enables_question_id)      REFERENCES question (id)
);

CREATE TABLE IF NOT EXISTS activator_choice
(
    activator_id    BIGINT NOT NULL,
    choice_id BIGINT NOT NULL,
    FOREIGN KEY (activator_id)    REFERENCES activator(id),
    FOREIGN KEY (choice_id) REFERENCES choice(id)
);