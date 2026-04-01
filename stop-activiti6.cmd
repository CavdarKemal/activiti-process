@echo off
REM Activiti 6.0 / Tomcat stoppen

set JAVA_HOME=C:\Program Files\Java\jdk-21.0.10+7
set CATALINA_HOME=E:\Projekte\ClaudeCode\activiti-process-6\apache-tomcat-9.0.115

echo Stoppe Tomcat...
"%CATALINA_HOME%\bin\catalina.bat" stop

timeout /t 3 /nobreak >nul

REM Falls Tomcat nicht reagiert, Prozess beenden
wmic process where "commandline like '%%activiti-process-6%%apache-tomcat%%'" call terminate >nul 2>&1
echo Tomcat gestoppt.
