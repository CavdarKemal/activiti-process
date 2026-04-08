@echo off
REM Startet Activiti 6 mit frischer DB (Windows)
cd /d "%~dp0.."
echo Starte Activiti 6 (frische DB bei jedem Start)...
echo.
set ACTIVITI_PORT=9090
echo   Activiti App:  http://localhost:%ACTIVITI_PORT%/activiti-app  (kermit/kermit)
echo   REST API:      http://localhost:%ACTIVITI_PORT%/activiti-rest/service  (kermit/kermit)
echo   Port aendern:  set ACTIVITI_PORT=xxxx vor dem Start
echo.
docker-compose up
