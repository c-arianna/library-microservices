@echo off
cd /d %~dp0

echo === BUILD MAVEN ===
call mvn clean install

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo BUILD FAILED - Maven error
    exit /b %ERRORLEVEL%
)

echo.
echo === BUILD DOCKER IMAGES ===

echo Building api-gateway...
docker build -t api-gateway:dev ./api-gateway
IF %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

echo Building book-service...
docker build -t book-service:dev ./book-service
IF %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

echo Building loan-service...
docker build -t loan-service:dev ./loan-service
IF %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

echo Building user-service...
docker build -t user-service:dev ./user-service
IF %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

echo Building notification-service...
docker build -t notification-service:dev ./notification-service
IF %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

echo BUILD COMPLETED SUCCESSFULLY