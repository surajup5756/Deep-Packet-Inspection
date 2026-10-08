@echo off
setlocal
cd /d "%~dp0"
if not exist bin mkdir bin
where javac >nul 2>nul
if errorlevel 1 (
 echo Java JDK not found. Install JDK 17+ and make sure javac is in PATH.
 pause
 exit /b 1
)
javac -encoding UTF-8 -d bin src\*.java
if errorlevel 1 (
 echo.
 echo Compilation failed.
 pause
 exit /b 1
)
if exist test_dpi.pcap (
 echo Running DPI on test_dpi.pcap...
 java -cp bin dpi.MainDpi test_dpi.pcap output_java.pcap
) else (
 echo No test_dpi.pcap found.
 echo Usage: java -cp bin dpi.MainDpi input.pcap output.pcap
)
pause
