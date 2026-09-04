============================================================
SHREE ANNAPURNA BILLING SYSTEM
Installer Build Guide — Phase 5.4
============================================================

OBJECTIVE
---------
Produce: ShreeAnnapurnaBillingSetup.exe

This directory contains all installer build assets.
The final .exe is placed in: deployment\output\

============================================================
DIRECTORY STRUCTURE
============================================================

deployment\
├── ShreeAnnapurnaInstaller.iss    <- Inno Setup compiler script
├── build-installer.bat            <- One-click build script
├── app-config.properties          <- Deployed next to the .exe (port 33066)
├── wizard-side.bmp                <- Installer left panel branding
├── wizard-top.bmp                 <- Installer header logo
├── mysql\                         <- MySQL 8.0 ZIP contents (MANUAL STEP)
│   └── bin\mysqld.exe             <- Must exist before build
├── mysql-scripts\
│   └── set-password.sql           <- Sets root123 on first install
└── output\
    └── ShreeAnnapurnaBillingSetup.exe   <- FINAL OUTPUT

============================================================
TWO MANUAL PREREQUISITES (before running build)
============================================================

STEP A: Install Inno Setup 6
  URL: https://jrsoftware.org/isdl.php
  - Download "Inno Setup 6" (stable, not QuickStart Pack)
  - Run the installer, accept all defaults
  - Verify after install:
    C:\Program Files (x86)\Inno Setup 6\iscc.exe  <-- must exist

STEP B: Download MySQL 8.0 Community Server ZIP Archive
  URL: https://dev.mysql.com/downloads/mysql/
  - Select OS: Microsoft Windows
  - Select: Windows (x86, 64-bit), ZIP Archive
  - Size: approximately 250 MB
  - File name example: mysql-8.0.36-winx64.zip
  - Click: "No thanks, just start my download"

  After download:
  - Extract the ZIP
  - Open the extracted folder (e.g. mysql-8.0.36-winx64\)
  - Copy ALL contents into:
      capstone\deployment\mysql\
  - Verify this path exists after copy:
      capstone\deployment\mysql\bin\mysqld.exe

============================================================
BUILD COMMAND (after prerequisites are done)
============================================================

Open Command Prompt as Administrator.

cd "c:\Users\RK NISHANTH\OneDrive\Desktop\capstone 123\capstone\deployment"

build-installer.bat

Output will appear at:
  deployment\output\ShreeAnnapurnaBillingSetup.exe

============================================================
INSTALLER BEHAVIOR SUMMARY
============================================================

First clean installation (no MySQL):
  - Extracts application image
  - Extracts MySQL engine binaries
  - Runs mysqld --initialize-insecure (creates fresh data dir)
  - Sets root password to root123
  - Registers ShreeAnnapurnaDB Windows service (port 33066)
  - Starts service
  - Verifies port 33066 is listening
  - Creates Desktop + Start Menu shortcuts

Reinstall / Upgrade (data directory already exists):
  - Extracts updated application image
  - Skips MySQL initialization
  - Reuses existing data directory
  - Restarts service
  - ALL business data preserved

Uninstall:
  - Stops ShreeAnnapurnaDB service
  - Removes ShreeAnnapurnaDB service registration
  - Removes application binaries from Program Files
  - Removes MySQL engine binaries from Program Files
  - Removes shortcuts
  - PRESERVES: C:\ProgramData\Shree Annapurna\Database\
    (Business data is NOT automatically deleted)

============================================================
SAFETY GUARANTEES
============================================================

  Never touches MySQL80 (port 3306) or any other MySQL
  Never changes existing MySQL passwords
  Never deletes any existing MySQL database
  Never drops tables or business data
  Stops installation if port 33066 is already in use
  Business data survives uninstall
  Existing data directory is never overwritten on reinstall

============================================================
DATABASE CONNECTION (post-install)
============================================================

  Host:     localhost
  Port:     33066
  Database: smart_billing_db
  User:     root
  Password: root123
  Service:  ShreeAnnapurnaDB (Automatic, starts at Windows boot)
