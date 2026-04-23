# 🚀 How to open the Admin Page
### 1. Find your built WAR file
From your log:
```
admin-web\target\gkadm.war
```
👉 This is your admin application.

# 2. You need a server (Tomcat)
This project is Spring (non-Spring Boot) → it does NOT run with java -jar.

You must deploy it to a server like:

👉 Apache Tomcat: https://tomcat.apache.org/download-90.cgi


# Best way to capture the real error
cd C:\tomcat\bin
catalina.bat run
