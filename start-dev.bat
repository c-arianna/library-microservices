@echo off

cd /d %~dp0

call build.bat || exit /b %ERRORLEVEL%

cd docker | exit /b %ERRORLEVEL%

docker compose -p dev --profile dev up -d || exit /b %ERRORLEVEL%

docker compose -p dev logs -f

echo === DONE ===