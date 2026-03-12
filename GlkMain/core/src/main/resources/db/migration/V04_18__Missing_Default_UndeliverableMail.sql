ALTER TABLE undeliverable_mail MODIFY incidents INT NOT NULL DEFAULT 1;
UPDATE undeliverable_mail SET incidents = 1 WHERE incidents = 0;