@echo off
title Smart Billing System - Build and Run
cd /d "%~dp0"

echo Checking prerequisites...

where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven ^(mvn^) is not installed or not added to PATH.
    echo Please install Apache Maven and ensure 'mvn' is available in your system PATH.
    pause
    exit /b 1
)

where java >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java ^(java^) is not installed or not added to PATH.
    echo Please install Java ^(JDK 17 or higher^) and ensure 'java' is available in your system PATH.
    pause
    exit /b 1
)

echo Building Smart Billing System with Maven...
call mvn clean package
if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b %errorlevel%
)

set "JAR_FILE=target\smart-billing-system-1.0-SNAPSHOT.jar"
if not exist "%JAR_FILE%" (
    echo ERROR: Expected output JAR file not found at %JAR_FILE%
    pause
    exit /b 1
)

echo Compilation successful! Starting the application...
java -jar "%JAR_FILE%"

