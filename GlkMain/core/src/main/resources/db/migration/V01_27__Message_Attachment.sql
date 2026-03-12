CREATE TABLE IF NOT EXISTS message_attachment
(
	id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
	legacy_id      VARCHAR(255) NULL,
	changeDate     DATETIME,
	createDate     DATETIME,
	file_id        BIGINT NOT NULL,
	message_id     BIGINT NOT NULL,
	FOREIGN KEY (file_id) REFERENCES file (id),
	FOREIGN KEY (message_id) REFERENCES message (id)
);
 
ALTER TABLE envelope CHANGE `read` read_ BIT;

ALTER TABLE message	MODIFY body LONGTEXT NOT NULL;