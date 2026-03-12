UPDATE envelope dest INNER JOIN envelope src ON src.id = dest.sender_id SET dest.message_id = src.message_id;

ALTER TABLE envelope
 DROP FOREIGN KEY envelope_ibfk_2,
	DROP COLUMN sender_id;

ALTER TABLE envelope
	CHANGE id legacy_id VARCHAR(255) NULL,
	DROP PRIMARY KEY,
	ADD id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	CHANGE changeDate change_date DATETIME,
	CHANGE createDate create_date DATETIME;

ALTER TABLE envelope
	DROP FOREIGN KEY envelope_ibfk_1,
	CHANGE message_id message_legacy_id VARCHAR(255) NULL,
	ADD message_id BIGINT;
	
ALTER TABLE message
	CHANGE id legacy_id VARCHAR(255) NULL,
	DROP PRIMARY KEY,
	ADD id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	CHANGE changeDate change_date DATETIME,
	CHANGE createDate create_date DATETIME;
	
UPDATE envelope dest INNER JOIN message src ON src.legacy_id = dest.message_legacy_id SET dest.message_id = src.id;

ALTER TABLE envelope
	ADD FOREIGN KEY (message_id) REFERENCES message (id),
	DROP COLUMN message_legacy_id,
	MODIFY message_id BIGINT NOT NULL;

