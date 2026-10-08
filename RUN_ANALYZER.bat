@echo off
setlocal
cd /d "%~dp0"
if not exist bin mkdir bin
javac -encoding UTF-8 -d bin src\*.java
if errorlevel 1 (pause & exit /b 1)
set "PCAP=%~1"
if "%PCAP%"=="" set "PCAP=test_dpi.pcap"
java -cp bin dpi.Main "%PCAP%"
pause
