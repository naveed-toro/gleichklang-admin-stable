DELIMITER //

DROP PROCEDURE IF EXISTS `CREATE_INDEX` //

-- Creates an index for a table column in the current database if it doesn't exist.
--
-- index_ : the name of the index
-- table_ : the name of the table
-- column_: the name of the column
CREATE PROCEDURE `CREATE_INDEX`
  (
    index_  VARCHAR(255),
    table_  VARCHAR(255),
    column_ VARCHAR(255)
  )
  BEGIN
    DECLARE IndexExists INTEGER;

    SELECT COUNT(1)
    INTO IndexExists
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE table_schema = DATABASE()
          AND table_name =
              CAST(table_ AS CHAR CHARACTER SET utf8) COLLATE utf8_general_ci
          AND index_name =
              CAST(index_ AS CHAR CHARACTER SET utf8) COLLATE utf8_general_ci;

    IF IndexExists = 0
    THEN
      SET @sqlstmt = CONCAT('CREATE INDEX `', index_, '` ON ', '`', table_,
                            '` (', column_, ')');

      PREPARE st FROM @sqlstmt;
      EXECUTE st;
      DEALLOCATE PREPARE st;
    ELSE
      SELECT
        CONCAT('Index ', index_, ' already exists on Table ', DATABASE(), '.',
               table_) CreateIndexWarningMessage;
    END IF;

  END //

DROP PROCEDURE IF EXISTS `CREATE_UNIQUE_INDEX` //

-- Creates an unique index for a table column in the current database if it doesn't exist.
--
-- index_ : the name of the index
-- table_ : the name of the table
-- column_: the name of the column
CREATE PROCEDURE `CREATE_UNIQUE_INDEX`
  (
    index_  VARCHAR(255),
    table_  VARCHAR(255),
    column_ VARCHAR(255)
  )
  BEGIN
    DECLARE IndexExists INTEGER;

    SELECT COUNT(1)
    INTO IndexExists
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE table_schema = DATABASE()
          AND table_name =
              CAST(table_ AS CHAR CHARACTER SET utf8) COLLATE utf8_general_ci
          AND index_name =
              CAST(index_ AS CHAR CHARACTER SET utf8) COLLATE utf8_general_ci;

    IF IndexExists = 0
    THEN
      SET @sqlstmt = CONCAT('CREATE UNIQUE INDEX `', index_, '` ON ', '`',
                            table_, '` (', column_, ')');

      PREPARE st FROM @sqlstmt;
      EXECUTE st;
      DEALLOCATE PREPARE st;
    ELSE
      SELECT
        CONCAT('Index ', index_, ' already exists on Table ', DATABASE(), '.',
               table_) CreateIndexWarningMessage;
    END IF;

  END //


DROP PROCEDURE IF EXISTS `DROP_INDEX` //

-- Drops an index for a table column in the current database if it doesn't exist.
--
-- index_ : the name of the index
-- table_ : the name of the table
CREATE PROCEDURE `DROP_INDEX`
  (
    index_ VARCHAR(255),
    table_ VARCHAR(255)
  )
  BEGIN
    DECLARE IndexExists INTEGER;

    SELECT COUNT(1)
    INTO IndexExists
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE table_schema = DATABASE()
          AND table_name =
              CAST(table_ AS CHAR CHARACTER SET utf8) COLLATE utf8_general_ci
          AND index_name =
              CAST(index_ AS CHAR CHARACTER SET utf8) COLLATE utf8_general_ci;

    IF IndexExists = 1
    THEN
      SET @sqlstmt = CONCAT('DROP INDEX `', index_, '` ON ', '`', table_, '`;');

      PREPARE st FROM @sqlstmt;
      EXECUTE st;
      DEALLOCATE PREPARE st;
    ELSE
      SELECT
        CONCAT('Index ', index_, ' already dropped on Table ', DATABASE(), '.',
               table_) DropIndexWarningMessage;
    END IF;

  END //

DROP PROCEDURE IF EXISTS `ADD_FOREIGN_KEY` //

-- Adds a foreign key for tables in the current database if it doesn't exist.
--
-- table_     : the name of the table to add the foreign key
-- column_    : the column for the foreign key
-- ref_table  : the referenced table name
-- ref_column : the referenced column name
CREATE PROCEDURE `ADD_FOREIGN_KEY`
  (
    table_     VARCHAR(255),
    column_    VARCHAR(255),
    ref_table  VARCHAR(255),
    ref_column VARCHAR(255)
  )
  BEGIN
    DECLARE ForeignKeyExists INTEGER;

    SELECT COUNT(1)
    INTO ForeignKeyExists
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND CONSTRAINT_NAME =
              CAST(column_ AS CHAR CHARACTER SET utf8) COLLATE utf8_general_ci
          AND TABLE_NAME =
              CAST(table_ AS CHAR CHARACTER SET utf8) COLLATE utf8_general_ci
          AND CONSTRAINT_TYPE =
              CAST('FOREIGN KEY' AS CHAR CHARACTER SET utf8) COLLATE
              utf8_general_ci;

    IF ForeignKeyExists = 0
    THEN
      SET @sqlstmt = CONCAT('ALTER TABLE `', table_, '`',
                            ' ADD FOREIGN KEY (`', column_, '`) REFERENCES `',
                            ref_table, '` (`', ref_column, '`);');

      PREPARE st FROM @sqlstmt;
      EXECUTE st;
      DEALLOCATE PREPARE st;
    ELSE
      SELECT
        CONCAT('Foreign key ', column_, ' already exists on Table ', DATABASE(),
               '.', table_) AddForeignKeyWarningMessage;
    END IF;

  END //


DROP PROCEDURE IF EXISTS `DROP_FOREIGN_KEY` //

-- Drops a foreign key for tables in the current database if it doesn't exist.
--
-- table_     : the name of the table to add the foreign key
-- fkname     : the name of the foreign key
-- ref_table  : the referenced table name
-- ref_column : the referenced column name
CREATE PROCEDURE `DROP_FOREIGN_KEY`
  (
    table_  VARCHAR(255),
    fk_name VARCHAR(255)
  )
  BEGIN
    DECLARE ForeignKeyExists INTEGER;

    SELECT COUNT(1)
    INTO ForeignKeyExists
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND CONSTRAINT_NAME =
              CAST(fk_name AS CHAR CHARACTER SET utf8) COLLATE utf8_general_ci
          AND TABLE_NAME =
              CAST(table_ AS CHAR CHARACTER SET utf8) COLLATE utf8_general_ci
          AND CONSTRAINT_TYPE =
              CAST('FOREIGN KEY' AS CHAR CHARACTER SET utf8) COLLATE
              utf8_general_ci;

    IF ForeignKeyExists = 1
    THEN
      SET @sqlstmt = CONCAT('ALTER TABLE `', table_, '`',
                            ' DROP FOREIGN KEY `', fk_name, '`;');

      PREPARE st FROM @sqlstmt;
      EXECUTE st;
      DEALLOCATE PREPARE st;
    ELSE
      SELECT
        CONCAT('Foreign key ', fk_name, ' doesnt exists on Table ', DATABASE(),
               '.', table_) DropForeignKeyWarningMessage;
    END IF;

  END //

DROP PROCEDURE IF EXISTS `ADD_COLUMN` //

-- Adds a column for tables in the current database if it doesn't exist.
--
-- table_      : the name of the table to add the column
-- column_     : the name of  the column
-- column_def  : the column definition
CREATE PROCEDURE `ADD_COLUMN`
  (
    table_     VARCHAR(255),
    column_    VARCHAR(255),
    column_def VARCHAR(255)
  )
  BEGIN
    DECLARE ColumnExists INTEGER;

    SELECT COUNT(1)
    INTO ColumnExists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME =
              CAST(table_ AS CHAR CHARACTER SET utf8) COLLATE utf8_general_ci
          AND COLUMN_NAME =
              CAST(column_ AS CHAR CHARACTER SET utf8) COLLATE utf8_general_ci;

    IF ColumnExists = 0
    THEN
      SET @sqlstmt = CONCAT('ALTER TABLE `', table_, '`',
                            ' ADD COLUMN `', column_, '` ', column_def, ';');

      PREPARE st FROM @sqlstmt;
      EXECUTE st;
      DEALLOCATE PREPARE st;
    ELSE
      SELECT
        CONCAT('Column ', column_, ' already exists on Table ', DATABASE(), '.',
               table_) AddColumnWarningMessage;
    END IF;

  END //


DROP PROCEDURE IF EXISTS `DROP_COLUMN` //

-- Drops a column for tables in the current database if it doesn't exist.
--
-- table_      : the name of the table to add the column
-- column_     : the name of  the column
CREATE PROCEDURE `DROP_COLUMN`
  (
    table_  VARCHAR(255),
    column_ VARCHAR(255)
  )
  BEGIN
    DECLARE ColumnExists INTEGER;

    SELECT COUNT(1)
    INTO ColumnExists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME =
              CAST(table_ AS CHAR CHARACTER SET utf8) COLLATE utf8_general_ci
          AND COLUMN_NAME =
              CAST(column_ AS CHAR CHARACTER SET utf8) COLLATE utf8_general_ci;

    IF ColumnExists = 1
    THEN
      SET @sqlstmt = CONCAT('ALTER TABLE `', table_, '`',
                            ' DROP COLUMN `', column_, '`;');

      PREPARE st FROM @sqlstmt;
      EXECUTE st;
      DEALLOCATE PREPARE st;
    ELSE
      SELECT
        CONCAT('Column ', column_, ' doesnt exists on Table ', DATABASE(), '.',
               table_) DropColumnWarningMessage;
    END IF;
  END//

DROP PROCEDURE IF EXISTS `RENAME_TABLE` //

-- Rename a table in the current database if it exist.
--
-- table_      : the name of the table to rename
-- new_table_     : the name of  the new table
CREATE PROCEDURE `RENAME_TABLE`
  (
    table_     VARCHAR(255),
    new_table_ VARCHAR(255)
  )
  BEGIN
    DECLARE TableExists INTEGER;

    SELECT COUNT(1)
    INTO TableExists
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_TYPE = 'BASE TABLE'
          AND TABLE_NAME =
              CAST(table_ AS CHAR CHARACTER SET utf8) COLLATE utf8_general_ci;

    IF TableExists > 0
    THEN
      SET @sqlstmt = CONCAT('RENAME TABLE `', table_, '`',
                            ' TO `', new_table_, '`;');

      PREPARE st FROM @sqlstmt;
      EXECUTE st;
      DEALLOCATE PREPARE st;
    ELSE
      SELECT CONCAT('Table ', table_, ' not exists on ',
                    DATABASE()) RenameTableWarningMessage;
    END IF;

  END //

DROP PROCEDURE IF EXISTS `CHANGE_COLUMN` //

-- Drops a column for tables in the current database if it doesn't exist.
--
-- table_      : the name of the table to add the column
-- column_     : the name of  the column
CREATE PROCEDURE `CHANGE_COLUMN`
  (
    table_             VARCHAR(255),
    column_            VARCHAR(255),
    new_column_        VARCHAR(255),
    column_definition_ VARCHAR(255)
  )
  BEGIN
    DECLARE ColumnExists INTEGER;

    SELECT COUNT(1)
    INTO ColumnExists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME =
              CAST(table_ AS CHAR CHARACTER SET utf8) COLLATE utf8_general_ci
          AND COLUMN_NAME =
              CAST(column_ AS CHAR CHARACTER SET utf8) COLLATE utf8_general_ci;

    IF ColumnExists = 1
    THEN
      SET @sqlstmt = CONCAT('ALTER TABLE `', table_, '`',
                            ' CHANGE COLUMN `', column_, '` `', new_column_,
                            '` ', column_definition_, ';');

      PREPARE st FROM @sqlstmt;
      EXECUTE st;
      DEALLOCATE PREPARE st;
    ELSE
      SELECT
        CONCAT('Column ', column_, ' doesnt exists on Table ', DATABASE(), '.',
               table_) ChangeColumnWarningMessage;
    END IF;

  END //

DROP PROCEDURE IF EXISTS `TRACK_QUERY` //

-- Inserts an entry to query_performance table
--
-- version_     : version of migration
-- message_     : message of log entry
-- start_       : when the tracking starts
CREATE PROCEDURE `TRACK_QUERY`
  (
    version_ VARCHAR(255),
    message_ VARCHAR(255),
    start_   DATETIME
  )
  BEGIN
    INSERT INTO query_performance (version, message, change_date, time_diff)
      VALUE (version_, message_, NOW(), NULL);
  END//


--DROP PROCEDURE IF EXISTS `TRACK_ANSWER_QUERY` //
--
---- Inserts an entry to query_performance table
--
--CREATE PROCEDURE `TRACK_ANSWER_QUERY`
--  (
--    version_ VARCHAR(255),
--    type_ VARCHAR(255),
--    rows_ INT,
--    message_ VARCHAR(255),
--    time_diff_ INT,
--    object_id_ INT
--
--  )
--  BEGIN
--    INSERT INTO query_performance (version, type, rows, message, change_date, time_diff, object_id)
--      VALUE (version_, type_, rows_, message_, NOW(), time_diff_, object_id_);
--  END//

DELIMITER ;

