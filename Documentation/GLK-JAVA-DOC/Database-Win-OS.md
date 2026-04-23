# 1. Make sure MySQL is running

Open WampServer and confirm MySQL is green/running.

# 2. Open Command Prompt as Administrator

Press Start, type: cmd

# 3. Go to MySQL bin folder
cd /d C:\wamp64\bin\mysql\mysql8.4.7\bin

# 4. Login as root
mysql -u root

# 5. Create database and user
Run these SQL commands inside MySQL:
```
CREATE DATABASE gleichklang CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'gleichklang'@'localhost' IDENTIFIED BY 'gleichklang';

GRANT ALL PRIVILEGES ON gleichklang.* TO 'gleichklang'@'localhost';

FLUSH PRIVILEGES;
```
###### If it says the user already exists, run this instead:
```
ALTER USER 'gleichklang'@'localhost' IDENTIFIED BY 'gleichklang';
GRANT ALL PRIVILEGES ON gleichklang.* TO 'gleichklang'@'localhost';
FLUSH PRIVILEGES;
```

# 6. Import the 25 GB SQL file
Run this in Command Prompt:
```
mysql -u gleichklang -p gleichklang < "D:\E-KHMER\Germany\database\gleichklang_dump.sql"
```
# Better for very large import
```
mysql --max_allowed_packet=1G -u gleichklang -p gleichklang < "D:\E-KHMER\Germany\database\gleichklang_dump.sql"
```

# Run these in MySQL:
###### Grants user gleichklang with Database gleichklang_h2_schema_export
```
USE gleichklang_h2_schema_export;
SHOW TABLES;
SHOW GRANTS FOR 'gleichklang'@'localhost';
```
###### If grants are missing, run:
```
GRANT ALL PRIVILEGES ON gleichklang_h2_schema_export.* TO 'gleichklang'@'localhost';
FLUSH PRIVILEGES;
```



