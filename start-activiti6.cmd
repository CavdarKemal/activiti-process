@echo off
REM Activiti 6.0 auf Tomcat 9 starten (Port 9090)
REM Benutzt JDK 11 (Activiti 6.0 ist nicht Java 17+ kompatibel fuer activiti-app)
REM Voraussetzung: PostgreSQL Docker-Container "activiti-db" auf Port 5433

set JAVA_HOME=E:\Projekte\ClaudeCode\activiti-process-6\jdk-11.0.25+9
set CATALINA_HOME=E:\Projekte\ClaudeCode\activiti-process-6\apache-tomcat-9.0.115

echo Pruefe PostgreSQL Docker-Container (activiti-db)...
docker ps --format "{{.Names}}" | findstr "activiti-db" >nul 2>&1
if errorlevel 1 (
    echo Starte PostgreSQL Container...
    docker start activiti-db
    timeout /t 3 /nobreak >nul
) else (
    echo PostgreSQL Container laeuft bereits.
)

echo.
echo Starte Activiti 6.0 auf Tomcat 9 (JDK 11)...
echo   Activiti REST:      http://localhost:9090/activiti-rest/service
echo   Activiti App:       http://localhost:9090/activiti-app
echo   Tomcat Manager:     http://localhost:9090/manager/html (admin/admin)
echo   Activiti App Login: kermit / kermit
echo   REST API Login:     kermit / kermit
echo   PostgreSQL:         localhost:5433/activiti6 (postgres/postgres)
echo.

cd /d "%CATALINA_HOME%"
"%CATALINA_HOME%\bin\catalina.bat" run
