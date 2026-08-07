@echo off
setlocal

set "PROJECT_DIR=%~dp0.."
set "LOCAL_MAVEN=%PROJECT_DIR%\.tools\apache-maven-3.9.9\bin\mvn.cmd"

for /d %%D in ("%PROJECT_DIR%\.tools\jdk17c\*") do (
  if exist "%%~fD\bin\java.exe" set "JAVA_HOME=%%~fD"
)

if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"

if exist "%LOCAL_MAVEN%" (
  call "%LOCAL_MAVEN%" clean package
) else (
  call mvn clean package
)

exit /b %ERRORLEVEL%
