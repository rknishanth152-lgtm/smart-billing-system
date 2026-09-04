@echo off
title Smart Billing System
cd /d "%~dp0"

where java >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java ^(java^) is not installed or not added to PATH.
    echo Please install Java ^(JDK 17 or higher^) and ensure 'java' is available in your system PATH.
    pause
    exit /b 1
)

set "JAR_FILE=target\smart-billing-system-1.0-SNAPSHOT.jar"

if not exist "%JAR_FILE%" (
    echo Application JAR not found at %JAR_FILE%.
    echo Attempting to build the application with Maven...
    
    where mvn >nul 2>&1
    if %errorlevel% neq 0 (
        echo ERROR: Maven ^(mvn^) is required to build the application, but was not found in PATH.
        echo Please install Apache Maven or run compile_and_run.bat once Maven is available.
        pause
        exit /b 1
    )
    
    call mvn clean package
    if %errorlevel% neq 0 (
        echo Build failed!
        pause
        exit /b %errorlevel%
    )
)

if not exist "%JAR_FILE%" (
    echo ERROR: Output JAR file not found at %JAR_FILE%.
    pause
    exit /b 1
)

echo Starting Smart Billing System...
java -jar "%JAR_FILE%"

