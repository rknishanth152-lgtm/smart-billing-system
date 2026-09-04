@echo off
REM ============================================================
REM build-installer.bat
REM
REM PURPOSE:
REM   Build the final ShreeAnnapurnaBillingSetup.exe
REM
REM PREREQUISITES THAT MUST BE DONE MANUALLY BEFORE RUNNING:
REM   1. Download Inno Setup 6 from https://jrsoftware.org/isdl.php
REM      Install it (accept defaults).
REM      Verify: C:\Program Files (x86)\Inno Setup 6\iscc.exe exists
REM
REM   2. Download MySQL 8.0 Community Server ZIP Archive from
REM      https://dev.mysql.com/downloads/mysql/
REM      (Windows x86_64 ZIP Archive, ~250 MB)
REM      Extract the ZIP contents so that:
REM      deployment\mysql\bin\mysqld.exe  -- exists
REM
REM   3. Run Maven clean package first:
REM      cd ..\  (project root)
REM      "C:\Program Files\Java\jdk-21\bin\mvn.cmd" clean package
REM
REM   4. Run jpackage to regenerate app-image:
REM      (this script will do it automatically below)
REM
REM OUTPUT:
REM   deployment\output\ShreeAnnapurnaBillingSetup.exe
REM ============================================================

setlocal
set JAVA_HOME=C:\Program Files\Java\jdk-21
set JPACKAGE=%JAVA_HOME%\bin\jpackage.exe
set MAVEN=C:\Users\RK NISHANTH\.antigravity-ide\extensions\oracle.oracle-java-26.0.2-universal\nbcode\java\maven\bin\mvn.cmd
set ISCC=C:\Program Files (x86)\Inno Setup 6\iscc.exe
set PROJECT_DIR=%~dp0..

echo.
echo ============================================================
echo   SHREE ANNAPURNA BILLING SYSTEM -- INSTALLER BUILD
echo ============================================================

REM ---- Step 1: Verify prerequisites ----
echo.
echo [1/5] Verifying prerequisites...

if not exist "%JPACKAGE%" (
    echo ERROR: JDK 21 jpackage not found at: %JPACKAGE%
    exit /b 1
)
echo   [OK] jpackage: %JPACKAGE%

if not exist "%ISCC%" (
    echo.
    echo ERROR: Inno Setup 6 is NOT installed.
    echo.
    echo Please install Inno Setup 6 from:
    echo   https://jrsoftware.org/isdl.php
    echo.
    echo Then re-run this build script.
    exit /b 1
)
echo   [OK] Inno Setup: %ISCC%

if not exist "%~dp0mysql\bin\mysqld.exe" (
    echo.
    echo ERROR: MySQL binaries not found at: %~dp0mysql\bin\mysqld.exe
    echo.
    echo Please download MySQL 8.0 Community Server ZIP from:
    echo   https://dev.mysql.com/downloads/mysql/
    echo.
    echo Extract so that:
    echo   %~dp0mysql\bin\mysqld.exe  -- exists
    echo.
    exit /b 1
)
echo   [OK] MySQL binaries found.

REM ---- Step 2: Maven clean package ----
echo.
echo [2/5] Building application JAR...
pushd "%PROJECT_DIR%"
call "%MAVEN%" clean package -q
if errorlevel 1 (
    echo ERROR: Maven build failed.
    popd
    exit /b 1
)
echo   [OK] Maven build successful.
popd

REM ---- Step 3: Rebuild jpackage app-image ----
echo.
echo [3/5] Building application image (jpackage)...
pushd "%PROJECT_DIR%"

REM Clean previous app-image
if exist "target\deployment\Shree Annapurna Billing System" (
    rmdir /s /q "target\deployment\Shree Annapurna Billing System"
)
mkdir "target\jpackage-input" 2>nul
copy /y "target\smart-billing-system-1.0-SNAPSHOT.jar" "target\jpackage-input\" >nul

"%JPACKAGE%" ^
  --type app-image ^
  --name "Shree Annapurna Billing System" ^
  --input "target\jpackage-input" ^
  --main-jar "smart-billing-system-1.0-SNAPSHOT.jar" ^
  --main-class com.smartbilling.Main ^
  --dest "target\deployment"

if errorlevel 1 (
    echo ERROR: jpackage failed.
    popd
    exit /b 1
)
echo   [OK] App-image created.
popd

REM ---- Step 4: Compile Inno Setup installer ----
echo.
echo [4/5] Compiling installer...
"%ISCC%" "%~dp0ShreeAnnapurnaInstaller.iss"
if errorlevel 1 (
    echo ERROR: Inno Setup compilation failed.
    exit /b 1
)

REM ---- Step 5: Verify output ----
echo.
echo [5/5] Verifying output...
if not exist "%~dp0output\ShreeAnnapurnaBillingSetup.exe" (
    echo ERROR: Output installer not found. Build may have failed.
    exit /b 1
)

for %%F in ("%~dp0output\ShreeAnnapurnaBillingSetup.exe") do (
    echo   [OK] ShreeAnnapurnaBillingSetup.exe  (Size: %%~zF bytes)
)

echo.
echo ============================================================
echo   BUILD COMPLETE
echo   Output: %~dp0output\ShreeAnnapurnaBillingSetup.exe
echo ============================================================
endlocal
