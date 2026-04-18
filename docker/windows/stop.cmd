@echo off
REM Stoppt Activiti 6 Container (Windows)
cd /d "%~dp0.."
docker compose -f docker-compose-a6.yml down
echo Activiti 6 gestoppt.
