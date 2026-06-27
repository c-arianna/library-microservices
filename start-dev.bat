@echo off
cd /d %~dp0

call build.bat

cd docker

echo === START DEV ENV ===

docker compose --profile dev up

echo === DONE ===
