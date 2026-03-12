DROP TABLE IF EXISTS cache;

CREATE TABLE IF NOT EXISTS cache
(
  id                BIGINT NOT NULL AUTO_INCREMENT,
  change_date       DATETIME,
  create_date       DATETIME,
  legacy_id         VARCHAR(255) DEFAULT NULL,
  DTYPE             VARCHAR(255) DEFAULT NULL,
  cache_key         VARCHAR(255) NOT NULL,
  cache_group       VARCHAR(255) DEFAULT NULL,
  number_value      DOUBLE DEFAULT NULL,
  text_value        VARCHAR(255) DEFAULT NULL,

  PRIMARY KEY (id),
  UNIQUE KEY cache_key_group(cache_key, cache_group)
);


-- Insert start values for affinity 'gesellschaft'

INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'mean_desint', 'gesellschaft', 10.465);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'mean_fuersor', 'gesellschaft', 12.731);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'mean_milde', 'gesellschaft', 9.368);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'mean_traditio', 'gesellschaft', 9.367);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'mean_umwelt', 'gesellschaft', 10.352);

INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'stddev_desint', 'gesellschaft', 2.2666);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'stddev_fuersor', 'gesellschaft', 1.9442);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'stddev_milde', 'gesellschaft', 1.6487);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'stddev_traditio', 'gesellschaft', 1.6431);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'stddev_umwelt', 'gesellschaft', 1.1730);


-- Insert start values for affinity 'partnerschaft'

INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'mean_agape', 'partnerschaft', 18.720);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'mean_anspruch', 'partnerschaft', 15.423);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'mean_kinder', 'partnerschaft', 10.275);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'mean_konflikt', 'partnerschaft', 15.467);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'mean_ungebundenheit', 'partnerschaft', 10.998);

INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'stddev_agape', 'partnerschaft', 3.3541);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'stddev_anspruch', 'partnerschaft', 3.3154);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'stddev_kinder', 'partnerschaft', 1.8868);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'stddev_konflikt', 'partnerschaft', 4.1558);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'stddev_ungebundenheit', 'partnerschaft', 3.4511);


-- Insert start values for affinity 'freundschaft'

INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'mean_anspruch$1', 'freundschaft', 11.360);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'mean_authentizitaet', 'freundschaft', 26.975);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'mean_geselligkeit', 'freundschaft', 20.838);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'mean_konflikt$1', 'freundschaft', 11.210);

INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'stddev_anspruch$1', 'freundschaft', 3.2870);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'stddev_authentizitaet', 'freundschaft', 3.0949);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'stddev_geselligkeit', 'freundschaft', 2.8555);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'stddev_konflikt$1', 'freundschaft', 2.3078);


-- Insert start values for affinity 'persoenlichkeit'

INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'mean_extraversion', 'persoenlichkeit', 17.694);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'mean_gewissenhaftigkeit', 'persoenlichkeit', 16.571);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'mean_neurotizismus', 'persoenlichkeit', 12.738);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'mean_offenheit', 'persoenlichkeit', 20.552);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'mean_vertraeglichkeit', 'persoenlichkeit', 14.381);

INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'stddev_extraversion', 'persoenlichkeit', 3.1009);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'stddev_gewissenhaftigkeit', 'persoenlichkeit', 2.8077);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'stddev_neurotizismus', 'persoenlichkeit', 3.6465);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'stddev_offenheit', 'persoenlichkeit', 2.5531);
INSERT INTO cache(DTYPE, cache_key, cache_group, number_value) VALUE('NumberCachedValue', 'stddev_vertraeglichkeit', 'persoenlichkeit', 2.1445);