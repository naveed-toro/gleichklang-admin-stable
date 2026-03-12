SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS affinity_question;
DROP TABLE IF EXISTS questions_mapping;
DROP TABLE IF EXISTS matching_matrix_value;
DROP TABLE IF EXISTS matching_matrix;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE IF NOT EXISTS matching_matrix
(
  id                     BIGINT(20)  NOT NULL AUTO_INCREMENT PRIMARY KEY,
  legacy_id              VARCHAR(255),
  change_date            DATETIME,
  create_date            DATETIME,
  name                   VARCHAR(31) NOT NULL,
  source_choice_group_id BIGINT(20)  NOT NULL,
  target_choice_group_id BIGINT(20)  NOT NULL,
  CONSTRAINT matching_matrix_ibfk_6 FOREIGN KEY (source_choice_group_id) REFERENCES choice_group (id),
  CONSTRAINT matching_matrix_ibfk_7 FOREIGN KEY (target_choice_group_id) REFERENCES choice_group (id)
);

CREATE INDEX source_choice_group_id ON matching_matrix (source_choice_group_id);
CREATE INDEX target_choice_group_id ON matching_matrix (target_choice_group_id);

CREATE TABLE matching_matrix_value
(
  id               BIGINT(20) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  legacy_id        VARCHAR(255),
  change_date      DATETIME,
  create_date      DATETIME,
  matrix_id        BIGINT(20)             NOT NULL,
  source_choice_id BIGINT(20)             NOT NULL,
  target_choice_id BIGINT(20)             NOT NULL,
  strictness       INT(11)                NOT NULL,
  CONSTRAINT matching_matrix_value_ibfk_1 FOREIGN KEY (matrix_id) REFERENCES matching_matrix (id),
  CONSTRAINT matching_matrix_value_ibfk_8 FOREIGN KEY (source_choice_id) REFERENCES choice (id),
  CONSTRAINT matching_matrix_value_ibfk_9 FOREIGN KEY (target_choice_id) REFERENCES choice (id)
);
CREATE INDEX source_choice_id ON matching_matrix_value (source_choice_id);
CREATE INDEX target_choice_id ON matching_matrix_value (target_choice_id);
CREATE UNIQUE INDEX unique_index ON matching_matrix_value (matrix_id, source_choice_id, target_choice_id);

CREATE TABLE questions_mapping
(
  id                  BIGINT(20) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  DTYPE               VARCHAR(31)            NOT NULL,
  legacy_id           VARCHAR(255),
  change_date         DATETIME,
  create_date         DATETIME,
  source_question_id  BIGINT(20),
  target_question_id  BIGINT(20),
  fact_question_id    BIGINT(20),
  max_question_id     BIGINT(20),
  min_question_id     BIGINT(20),
  max_distance        INT(11),
  matrix_id           BIGINT(20) NULL,
  max_age_question_id BIGINT(20),
  min_age_question_id BIGINT(20),
  avatar_question_id  BIGINT(20),
  true_choice_id      BIGINT(20),
  CONSTRAINT questions_mapping_ibfk_10 FOREIGN KEY (max_age_question_id) REFERENCES question (id),
  CONSTRAINT questions_mapping_ibfk_11 FOREIGN KEY (min_age_question_id) REFERENCES question (id),
  CONSTRAINT questions_mapping_ibfk_12 FOREIGN KEY (avatar_question_id) REFERENCES question (id),
  CONSTRAINT questions_mapping_ibfk_13 FOREIGN KEY (true_choice_id) REFERENCES choice (id),
  CONSTRAINT questions_mapping_ibfk_4 FOREIGN KEY (fact_question_id) REFERENCES question (id),
  CONSTRAINT questions_mapping_ibfk_5 FOREIGN KEY (max_question_id) REFERENCES question (id),
  CONSTRAINT questions_mapping_ibfk_6 FOREIGN KEY (min_question_id) REFERENCES question (id),
  CONSTRAINT questions_mapping_ibfk_7 FOREIGN KEY (matrix_id) REFERENCES matching_matrix (id),
  CONSTRAINT questions_mapping_ibfk_8 FOREIGN KEY (source_question_id) REFERENCES question (id),
  CONSTRAINT questions_mapping_ibfk_9 FOREIGN KEY (target_question_id) REFERENCES question (id)
);
CREATE INDEX avatar_question_id ON questions_mapping (avatar_question_id);
CREATE INDEX fact_question_id ON questions_mapping (fact_question_id);
CREATE INDEX matrix_id ON questions_mapping (matrix_id);
CREATE INDEX max_age_question_id ON questions_mapping (max_age_question_id);
CREATE INDEX max_question_id ON questions_mapping (max_question_id);
CREATE INDEX min_age_question_id ON questions_mapping (min_age_question_id);
CREATE INDEX min_question_id ON questions_mapping (min_question_id);
CREATE INDEX source_question_id ON questions_mapping (source_question_id);
CREATE INDEX target_question_id ON questions_mapping (target_question_id);
CREATE INDEX true_choice_id ON questions_mapping (true_choice_id);


CREATE TABLE IF NOT EXISTS affinity_question
(
  questions_mapping_id BIGINT NOT NULL,
  question_id          BIGINT NOT NULL,
  PRIMARY KEY (questions_mapping_id, question_id),
  FOREIGN KEY (questions_mapping_id) REFERENCES questions_mapping (id),
  FOREIGN KEY (question_id) REFERENCES question (id)
);
