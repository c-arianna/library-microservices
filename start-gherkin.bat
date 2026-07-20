@echo off
cd /d %~dp0

call build.bat || exit /b %ERRORLEVEL%

cd docker || exit /b %ERRORLEVEL%

echo === START GHERKIN ENV ===

docker compose -p gherkin --profile gherkin down -v || exit /b %ERRORLEVEL%
docker compose -p gherkin --profile gherkin up -d || exit /b %ERRORLEVEL%

docker compose -p gherkin logs -f

echo === DONE ===
