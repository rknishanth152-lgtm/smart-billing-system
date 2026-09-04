; ============================================================
; Shree Annapurna Billing System
; Inno Setup 6 Installer Script
;
; Architecture:
;   Application: C:\Program Files\Shree Annapurna\Billing System
;   MySQL Engine: C:\Program Files\Shree Annapurna\mysql
;   MySQL Data:   C:\ProgramData\Shree Annapurna\Database\data
;   MySQL Logs:   C:\ProgramData\Shree Annapurna\Database\logs
;   MySQL Service: ShreeAnnapurnaDB (port 33066)
;
; Safety Rules:
;   - NEVER touches existing MySQL80 or any other MySQL
;   - NEVER deletes ProgramData database on uninstall
;   - NEVER initializes an existing data directory
;   - NEVER drops tables or existing business data
;   - STOPS installation if port 33066 is already occupied
; ============================================================

#define MyAppName      "Shree Annapurna Billing System"
#define MyAppVersion   "1.0"
#define MyAppPublisher "Shree Annapurna Edible Oil Company"
#define MyAppExeName   "Shree Annapurna Billing System.exe"
#define MySQLSvcName   "ShreeAnnapurnaDB"
#define MySQLSvcDisp   "Shree Annapurna Billing System Database Service"
#define MySQLPort      "33066"

[Setup]
AppId={{A1B2C3D4-E5F6-7890-ABCD-EF1234567890}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\Shree Annapurna\Billing System
DefaultGroupName=Shree Annapurna
OutputDir=output
OutputBaseFilename=ShreeAnnapurnaBillingSetup
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
PrivilegesRequired=admin
MinVersion=10.0
ArchitecturesInstallIn64BitMode=x64compatible
ArchitecturesAllowed=x64compatible
UninstallDisplayName={#MyAppName}
UninstallDisplayIcon={app}\{#MyAppExeName}
WizardImageFile=wizard-side.bmp
WizardSmallImageFile=wizard-top.bmp
DisableDirPage=no
DisableProgramGroupPage=no

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Dirs]
Name: "{app}"
Name: "{autopf}\Shree Annapurna\mysql"
Name: "{commonappdata}\Shree Annapurna\Database\data"
Name: "{commonappdata}\Shree Annapurna\Database\logs"

[Files]
; Application image (from Phase 5.3 jpackage output)
Source: "..\target\deployment\Shree Annapurna Billing System\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

; config.properties pointing application to port 33066
Source: "app-config.properties"; DestDir: "{app}"; DestName: "config.properties"; Flags: ignoreversion

; Dedicated MySQL engine binaries
Source: "mysql\*"; DestDir: "{autopf}\Shree Annapurna\mysql"; Flags: ignoreversion recursesubdirs createallsubdirs

; Password init SQL (temp, deleted after install)
Source: "mysql-scripts\set-password.sql"; DestDir: "{tmp}"; Flags: deleteafterinstall

[Icons]
Name: "{userdesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"; Comment: "Shree Annapurna Edible Oil Company - Smart Billing System"
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"; Comment: "Shree Annapurna Edible Oil Company - Smart Billing System"
Name: "{group}\Uninstall {#MyAppName}"; Filename: "{uninstallexe}"

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "Launch Shree Annapurna Billing System"; Flags: nowait postinstall skipifsilent; WorkingDir: "{app}"

[UninstallRun]
Filename: "net.exe"; Parameters: "stop {#MySQLSvcName}"; Flags: runhidden; RunOnceId: "StopDB"
Filename: "{autopf}\Shree Annapurna\mysql\bin\mysqld.exe"; Parameters: "--remove {#MySQLSvcName}"; Flags: runhidden; RunOnceId: "RemoveDB"

[Code]
var
  IsFirstInstall: Boolean;

function NL(): String;
begin
  Result := Chr(13) + Chr(10);
end;

// Convert backslashes to forward slashes for MySQL my.ini paths
function SlashPath(S: String): String;
var
  i: Integer;
begin
  Result := S;
  for i := 1 to Length(Result) do
    if Result[i] = '\' then
      Result[i] := '/';
end;

function IsPortFree(): Boolean;
var
  RC: Integer;
begin
  // findstr RC=0 means match found (port IN USE), RC=1 means no match (port FREE)
  Exec('cmd.exe',
    '/c netstat -an | findstr /L ":{#MySQLPort} "',
    '', SW_HIDE, ewWaitUntilTerminated, RC);
  Result := (RC <> 0);
end;

function IsDatabaseInitialized(): Boolean;
begin
  Result := FileExists(
    ExpandConstant('{commonappdata}\Shree Annapurna\Database\.initialized')
  );
end;

function DataDirectoryExists(): Boolean;
begin
  Result := DirExists(
    ExpandConstant('{commonappdata}\Shree Annapurna\Database\data')
  );
end;

function WaitForPort(MaxSec: Integer): Boolean;
var
  RC, i: Integer;
begin
  Result := False;
  for i := 1 to MaxSec do
  begin
    // findstr RC=0 means LISTENING line found on our port -> MySQL is up
    Exec('cmd.exe',
      '/c netstat -an | findstr /L ":{#MySQLPort} " | findstr "LISTENING"',
      '', SW_HIDE, ewWaitUntilTerminated, RC);
    if RC = 0 then
    begin
      Result := True;
      Exit;
    end;
    Sleep(1000);
  end;
end;

function InitializeSetup(): Boolean;
begin
  Result := True;
  if not IsPortFree() then
  begin
    MsgBox(
      'Port {#MySQLPort} is currently in use by another application.' + NL() +
      NL() +
      'Shree Annapurna Billing System cannot complete installation' + NL() +
      'until this port is available.' + NL() +
      NL() +
      'Please close the application using port {#MySQLPort} and try again.',
      mbCriticalError, MB_OK);
    Result := False;
    Exit;
  end;
  IsFirstInstall := not IsDatabaseInitialized();
end;

procedure CurStepChanged(CurStep: TSetupStep);
var
  MySQLExe, MySQLAdminExe, MySQLClientExe: String;
  MySQLBaseDir, MySQLDataDir, MySQLLogsDir, MySQLIni: String;
  IniContent: String;
  RC: Integer;
begin
  if CurStep <> ssPostInstall then Exit;

  MySQLBaseDir   := ExpandConstant('{autopf}\Shree Annapurna\mysql');
  MySQLDataDir   := ExpandConstant('{commonappdata}\Shree Annapurna\Database\data');
  MySQLLogsDir   := ExpandConstant('{commonappdata}\Shree Annapurna\Database\logs');
  MySQLIni       := MySQLBaseDir + '\my.ini';
  MySQLExe       := MySQLBaseDir + '\bin\mysqld.exe';
  MySQLClientExe := MySQLBaseDir + '\bin\mysql.exe';
  MySQLAdminExe  := MySQLBaseDir + '\bin\mysqladmin.exe';

  // STEP 1: Write my.ini
  WizardForm.StatusLabel.Caption := 'Configuring database engine...';

  IniContent :=
    '[mysqld]' + NL() +
    'port={#MySQLPort}' + NL() +
    'basedir=' + SlashPath(MySQLBaseDir) + NL() +
    'datadir=' + SlashPath(MySQLDataDir) + NL() +
    'log-error=' + SlashPath(MySQLLogsDir) + '/mysqld.err' + NL() +
    'character-set-server=utf8mb4' + NL() +
    'collation-server=utf8mb4_unicode_ci' + NL() +
    'max_connections=50' + NL() +
    NL() +
    '[client]' + NL() +
    'port={#MySQLPort}' + NL() +
    'default-character-set=utf8mb4' + NL();

  SaveStringToFile(MySQLIni, IniContent, False);

  // STEP 2: Initialize data dir (first install only)
  if IsFirstInstall then
  begin
    WizardForm.StatusLabel.Caption := 'Preparing database (first run)...';

    // If a partial/failed directory exists, we must stop any leftover service and delete it
    if DataDirectoryExists() then
    begin
      Exec('net.exe', 'stop {#MySQLSvcName}', '', SW_HIDE, ewWaitUntilTerminated, RC);
      Exec(MySQLExe, '--remove {#MySQLSvcName}', MySQLBaseDir, SW_HIDE, ewWaitUntilTerminated, RC);
      Exec('cmd.exe', '/c rmdir /s /q "' + MySQLDataDir + '"', '', SW_HIDE, ewWaitUntilTerminated, RC);
    end;

    Exec('cmd.exe', '/c mkdir "' + MySQLDataDir + '"', '', SW_HIDE, ewWaitUntilTerminated, RC);

    Exec(MySQLExe,
      '--defaults-file="' + MySQLIni + '" --initialize-insecure',
      MySQLBaseDir, SW_HIDE, ewWaitUntilTerminated, RC);

    if RC <> 0 then
    begin
      MsgBox(
        'Database initialization failed (code ' + IntToStr(RC) + ').' + NL() +
        'Check log: ' + MySQLLogsDir + '\mysqld-init.err',
        mbCriticalError, MB_OK);
      Exit;
    end;

    // STEP 3: Start temporary instance to set root password
    WizardForm.StatusLabel.Caption := 'Securing database...';

    Exec('cmd.exe',
      '/c start "" "' + MySQLExe + '" --defaults-file="' + MySQLIni + '"',
      MySQLBaseDir, SW_HIDE, ewNoWait, RC);

    Sleep(5000);
    if not WaitForPort(25) then
    begin
      MsgBox(
        'Database engine failed to respond during initial setup.' + NL() +
        'Check: ' + MySQLLogsDir + '\mysqld.err',
        mbCriticalError, MB_OK);
      Exit;
    end;

    // STEP 4: Set root password
    Exec(MySQLClientExe,
      '--host=127.0.0.1' +
      ' --port={#MySQLPort}' +
      ' --user=root' +
      ' --execute="ALTER USER ''root''@''localhost'' IDENTIFIED BY ''root123''; FLUSH PRIVILEGES;"',
      MySQLBaseDir, SW_HIDE, ewWaitUntilTerminated, RC);

    if RC <> 0 then
    begin
      MsgBox('Failed to set database credentials. Setup cannot continue.', mbCriticalError, MB_OK);
      Exit;
    end;

    // STEP 5: Shutdown temp instance
    Exec(MySQLAdminExe,
      '--host=127.0.0.1 --port={#MySQLPort} --user=root --password=root123 shutdown',
      MySQLBaseDir, SW_HIDE, ewWaitUntilTerminated, RC);
    Sleep(3000);

    // Create initialization marker
    SaveStringToFile(ExpandConstant('{commonappdata}\Shree Annapurna\Database\.initialized'), 'Initialization Complete', False);
  end
  else
  begin
    WizardForm.StatusLabel.Caption := 'Existing database detected, reusing...';
    Sleep(1000);
  end;

  // STEP 6: Register Windows service (our isolated ShreeAnnapurnaDB only)
  WizardForm.StatusLabel.Caption := 'Registering database service...';

  // Remove stale registration if any (safe — our own service)
  Exec(MySQLExe, '--remove {#MySQLSvcName}', MySQLBaseDir, SW_HIDE, ewWaitUntilTerminated, RC);

  Exec(MySQLExe,
    '--install {#MySQLSvcName} --defaults-file="' + MySQLIni + '"',
    MySQLBaseDir, SW_HIDE, ewWaitUntilTerminated, RC);

  if RC <> 0 then
  begin
    MsgBox(
      'Service registration failed (code ' + IntToStr(RC) + ').' + NL() +
      'Ensure the installer is running as Administrator.',
      mbCriticalError, MB_OK);
    Exit;
  end;

  // Set automatic startup type
  Exec('sc.exe', 'config {#MySQLSvcName} start= auto', '', SW_HIDE, ewWaitUntilTerminated, RC);

  // STEP 7: Start the service
  WizardForm.StatusLabel.Caption := 'Starting database service...';
  Exec('net.exe', 'start {#MySQLSvcName}', '', SW_HIDE, ewWaitUntilTerminated, RC);

  if not WaitForPort(30) then
  begin
    MsgBox(
      'The Shree Annapurna database service failed to start.' + NL() +
      NL() +
      'Possible causes:' + NL() +
      '- Antivirus software blocking mysqld.exe' + NL() +
      '- Port {#MySQLPort} became occupied during installation' + NL() +
      NL() +
      'Error log: ' + MySQLLogsDir + '\mysqld.err',
      mbCriticalError, MB_OK);
    Exit;
  end;

  WizardForm.StatusLabel.Caption := 'Creating shortcuts...';
end;

procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
begin
  if CurUninstallStep = usUninstall then
  begin
    MsgBox(
      'The Shree Annapurna Billing System has been uninstalled.' + NL() +
      NL() +
      'Your business data has been preserved at:' + NL() +
      ExpandConstant('{commonappdata}\Shree Annapurna\Database') + NL() +
      NL() +
      'To permanently remove all data, delete that folder manually.',
      mbInformation, MB_OK);
  end;
end;
