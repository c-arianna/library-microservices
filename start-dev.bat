@echo off
cd /d %~dp0

call build.bat

cd docker

echo === START DEV ENV ===

docker compose -p dev --profile dev up

echo === DONE ===
