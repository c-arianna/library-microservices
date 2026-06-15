@echo off
cd /d %~dp0

call build.bat

cd docker
docker compose up
