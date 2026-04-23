# ROOT Project GlkMain Folder

###### Path file of DB user & password
gleichklang-admin-java\GlkMain\core\src\main\resources\h2_schema_export.properties

#### Install Package
cd GlkMain &  mvn clean install -DskipTests


# H2 schema export database setup
### H2 schema export database setup

For the `core` module, the project uses `h2_schema_export.properties` during the `generate-h2-schema` Maven profile.

Use this database for schema export:

```properties
db.jdbc.url=jdbc:mysql://localhost:3306/gleichklang_h2_schema_export?useSSL=false&allowPublicKeyRetrieval=true&autoReconnect=true&connectTimeout=10000&socketTimeout=30000
db.database=gleichklang_h2_schema_export
db.user=gleichklang
db.password=gleichklang
```

Required MySQL setup:

```sql
CREATE DATABASE IF NOT EXISTS gleichklang_h2_schema_export;
GRANT ALL PRIVILEGES ON gleichklang_h2_schema_export.* TO 'gleichklang'@'localhost';
FLUSH PRIVILEGES;
```

Environment used:

* Java: 1.8.0_482
* Maven: 3.9.14
* MySQL: 8.4.7

Verification commands:

```bash
java -version
mvn -version
```

```sql
SHOW DATABASES;
USE gleichklang_h2_schema_export;
SHOW GRANTS FOR 'gleichklang'@'localhost';
```
