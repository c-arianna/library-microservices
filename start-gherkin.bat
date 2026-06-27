@echo off
cd /d %~dp0

call build.bat

cd docker

echo === START GHERKIN ENV ===

docker compose --profile gherkin down -v
docker compose --profile gherkin up

echo === DONE ===
