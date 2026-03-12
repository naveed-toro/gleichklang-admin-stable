DELIMITER //
DROP PROCEDURE IF EXISTS `DROP_INDEX`//

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
          AND table_name = CAST(table_ AS CHAR
    CHARACTER SET utf8) COLLATE utf8_general_ci
          AND index_name = CAST(index_ AS CHAR
    CHARACTER SET utf8) COLLATE utf8_general_ci;

    IF IndexExists > 0
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

  END
//
DELIMITER ;


