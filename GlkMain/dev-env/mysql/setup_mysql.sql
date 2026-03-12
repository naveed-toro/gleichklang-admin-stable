CREATE USER 'gleichklang'@'%' IDENTIFIED BY 'gleichklang';
CREATE USER 'gleichklang'@'localhost' IDENTIFIED BY 'gleichklang';

GRANT ALL ON *.* TO 'gleichklang'@'localhost';
GRANT ALL ON *.* TO 'gleichklang'@'%';

CREATE DATABASE gleichklang;

GRANT ALL PRIVILEGES ON `gleichklang`.* TO 'gleichklang'@'%';
GRANT ALL PRIVILEGES ON `gleichklang_h2_schema_export`.* TO 'gleichklang'@'%';
