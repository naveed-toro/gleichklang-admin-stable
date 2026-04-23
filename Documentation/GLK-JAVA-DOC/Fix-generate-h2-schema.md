### Fix: generate-h2-schema.sql build failure

Problem:
Build fails in `generate-h2-schema.sql` with timeout / connection errors.

Root cause:
File `h2_schema_export.properties` is missing from classpath.

Fix:

1. Create file:

```
core/src/test/resources/h2_schema_export.properties
```

2. Add:

```properties
db.jdbc.url=jdbc:mysql://localhost:3306/gleichklang_h2_schema_export?useSSL=false&allowPublicKeyRetrieval=true&autoReconnect=true&connectTimeout=30000&socketTimeout=600000
db.database=gleichklang_h2_schema_export
db.user=gleichklang
db.password=gleichklang
```

3. Clean build:

```
mvn clean
```

4. Run:

```
mvn -pl core clean test
```

Reason:
Maven plugin loads `/h2_schema_export.properties` from classpath. If missing, wrong defaults are used, causing timeout failures.
