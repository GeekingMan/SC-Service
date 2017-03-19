DROP TABLE IF EXISTS event_data_sync;
CREATE TABLE event_data_sync (
  event_id            VARCHAR(36) NOT NULL,
  event_type          VARCHAR(50),
  user_id             VARCHAR(36),
  source_service      VARCHAR(50),
  destination_service VARCHAR(50),
  feature_key         VARCHAR(500),
  content             VARCHAR(100),
  accept_status       VARCHAR(50),
  process_status      VARCHAR(50),
  create_time         TIMESTAMP,
  update_time         TIMESTAMP,
  PRIMARY KEY (event_id)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;