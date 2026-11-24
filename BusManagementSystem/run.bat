@echo off
cd /d "%~dp0"
if not exist out mkdir out
javac -d out src\bms\*.java
if errorlevel 1 pause & exit /b
echo Starting server... open http://localhost:8080
java -cp out bms.Main
pause
