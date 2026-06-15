@echo off

cd /d %~dp0

echo === BUILD MAVEN ===
call mvn  -Dmaven.test.skip clean install

echo === BUILD DOCKER IMAGES ===
docker build -t api-gateway:dev ./api-gateway
docker build -t book-service:dev ./book-service
docker build -t loan-service:dev ./loan-service
docker build -t user-service:dev ./user-service

echo === DONE BUILD ===