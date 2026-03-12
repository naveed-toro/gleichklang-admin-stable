CREATE TABLE IF NOT EXISTS template_context
(
--    id                      BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
--    legacy_id               VARCHAR(255),
--    change_date             DATETIME,
--    create_date             DATETIME,
    template_context        VARCHAR(255) NOT NULL,
    template_filter_id      BIGINT NOT NULL,
    FOREIGN KEY (template_filter_id) REFERENCES filter (id),
    PRIMARY KEY (template_context, template_filter_id)
);