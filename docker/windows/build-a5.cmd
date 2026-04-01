@echo off
REM Baut das Activiti 5.22 Docker-Image (Windows)
cd /d "%~dp0.."
echo Baue Activiti 5.19 Docker-Image...
set DOCKER_BUILDKIT=0
docker-compose -f docker-compose-a5.yml build
echo.
echo Fertig! Starten mit: windows\start-a5.cmd
