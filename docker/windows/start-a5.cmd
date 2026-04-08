@echo off
REM Startet Activiti 5.22 mit frischer DB (Windows)
cd /d "%~dp0.."
echo Starte Activiti 5.19 (frische DB bei jedem Start)...
echo.
set ACTIVITI5_PORT=9091
echo   REST API:  http://localhost:%ACTIVITI5_PORT%/activiti-rest/service  (kermit/kermit)
echo   Port aendern:  set ACTIVITI5_PORT=xxxx vor dem Start
echo.
docker-compose -f docker-compose-a5.yml down
docker-compose -f docker-compose-a5.yml up
