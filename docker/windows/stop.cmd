@echo off
REM Stoppt Activiti 6 Container (Windows)
cd /d "%~dp0.."
docker-compose down
echo Activiti 6 gestoppt.
