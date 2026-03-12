CREATE TABLE IF NOT EXISTS service_offer_category (
  id                       BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  legacy_id                VARCHAR(255),
  change_date              DATETIME,
  create_date              DATETIME,

  service_offer_id    BIGINT NOT NULL,
  category                 VARCHAR(255) NOT NULL,

  FOREIGN KEY (service_offer_id) REFERENCES product (id),
  UNIQUE KEY service_offer_category_service_offer_id_category (service_offer_id, category)
);
