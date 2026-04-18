@echo off
REM Baut das Activiti 6 Docker-Image (Windows)
REM Verwendet DOCKER_BUILDKIT=0 fuer Linux-Kompatibilitaet
cd /d "%~dp0.."
echo Baue Activiti 6 Docker-Image...
set DOCKER_BUILDKIT=0
docker compose -f docker-compose-a6.yml build
echo.
echo Fertig! Starten mit: windows\start.cmd
