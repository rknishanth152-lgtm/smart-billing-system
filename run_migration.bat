@echo off
title Smart Billing System - Migration
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
    echo Compiled application JAR not found at %JAR_FILE%.
    echo Building the application with Maven before running migration...
    
    where mvn >nul 2>&1
    if %errorlevel% neq 0 (
        echo ERROR: Maven ^(mvn^) is required to build the application, but was not found in PATH.
        echo Please install Apache Maven and ensure 'mvn' is available in your system PATH.
        pause
        exit /b 1
    )
    
    call mvn clean package
    if %errorlevel% neq 0 (
        echo Build failed! Migration cancelled.
        pause
        exit /b %errorlevel%
    )
)

if not exist "%JAR_FILE%" (
    echo ERROR: Output JAR file not found at %JAR_FILE%.
    pause
    exit /b 1
)

echo Running database migration ^(DataConverter^)...
java -cp "%JAR_FILE%" com.smartbilling.DataConverter
if %errorlevel% neq 0 (
    echo Migration failed with error code %errorlevel%!
    pause
    exit /b %errorlevel%
)

echo Migration completed successfully.

