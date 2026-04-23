# 📘 Gleichklang Admin Java – Setup Guide
## 1. 🧱 Environment Setup
### Required Software
- Java JDK 8
- Apache Maven 3.9+
- MySQL 8.x
- Apache Tomcat 9
- (Optional) WampServer / XAMPP

### Verify Installation
```
java -version
mvn -version
```
Expected:
Java 1.8.x
Maven 3.9.x

# 2. 🗄️ MySQL Setup
## 2.1 Create Database
```
CREATE DATABASE gleichklang CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
###### Optional (for H2 export):
```
CREATE DATABASE gleichklang_h2_schema_export CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 2.2 Create User
```
CREATE USER 'gleichklang'@'localhost' IDENTIFIED BY 'gleichklang';
GRANT ALL PRIVILEGES ON gleichklang.* TO 'gleichklang'@'localhost';
GRANT ALL PRIVILEGES ON gleichklang_h2_schema_export.* TO 'gleichklang'@'localhost';
FLUSH PRIVILEGES;
```

## 2.3 Verify
```
SHOW DATABASES;
```

# 3. ⚙️ Application Configuration
#### 📍 Important Folder
GlkMain/core/src/main/resources

## 3.1 Update ALL DB configs to local
Make sure ALL these files use:

```
db.jdbc.url=jdbc:mysql://localhost:3306/gleichklang?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8
db.user=gleichklang
db.password=gleichklang
```
![alt text](https://github.com/sydenhong/GLK-JAVA-DOC/blob/main/db.png)

## 3.2 Files to update
- application.properties ✅ (MOST IMPORTANT)
- application-dev.properties
- application-staging.properties
- application-prod.properties
- application-n3.properties
- application-qa.properties
- application-bugfix.properties
- application-integration.properties

#### ⚠️ Important Note
Remove or replace ALL of these if found:
```
127.0.0.1:3308 ❌
mysql0.core.binaere-bauten.de ❌
192.168.xx.xx ❌
```
###### Only use:
localhost:3306 ✅

# 4. 🛠️ Build Project

From root folder:
```
mvn clean install -DskipTests
```

# 5. 🚀 Deploy to Tomcat
## 5.1 Copy WAR
gkadm.war → C:\tomcat\webapps\

## 5.2 Clean Old Deployment

###### Delete:
```
C:\tomcat\webapps\gkadm
C:\tomcat\webapps\gkadm.war
```

## 5.3 Start Tomcat
```
C:\tomcat\bin\startup.bat
```

## 5.4 Open Browser
```
http://localhost:8080/gkadm
```

# 6. 🔍 Troubleshooting
##### ❌ Error: HTTP 404 /gkadm

Cause:
- WAR not deployed
- Tomcat not restarted

Fix:
- Delete folder + WAR
- Restart Tomcat

##### ❌ Error: Connection refused

Cause:
- Wrong DB port (3308 ❌ instead of 3306)
- MySQL not running

Fix:
- Check MySQL
- Fix db.jdbc.url

##### ❌ Error: Communications link failure

Cause:
- Wrong DB host or port
- 
##### ❌ Check Logs
- C:\tomcat\logs\catalina*.log
- C:\tomcat\logs\localhost*.log

# 7. 🧪 Optional: H2 Schema Export
File:
```
h2_schema_export.properties
```

Use:
```
db.jdbc.url=jdbc:mysql://localhost:3306/gleichklang_h2_schema_export
```

# 8. ✅ Final Checklist

Before run:
 - MySQL running
 - DB exists
 - User created
 - All configs use localhost:3306
 - Maven build success
 - WAR deployed
 - Tomcat restarted


# DATABASE Preproduction
Please ask Mr.Syden or Mr.Sascha
