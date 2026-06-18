param(
    [string]$JarPath = "",
    [switch]$SkipBuild,
    [string]$DatasetDir = "",
    [switch]$StopExisting,
    [int]$RewardReportInterval = 10
)

$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Java = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME "bin\java.exe" } else { "java.exe" }

function Get-AgentMinRecordProcesses {
    param([int[]]$ExcludeProcessIds = @())
    Get-CimInstance Win32_Process | Where-Object {
        $cmd = if ($_.CommandLine) { $_.CommandLine } else { "" }
        $name = if ($_.Name) { $_.Name.ToLowerInvariant() } else { "" }
        $isExcluded = $ExcludeProcessIds -contains $_.ProcessId
        $isGame = ($name -eq "java.exe" -or $name -eq "javaw.exe") -and (
            $cmd -like "*com.shatteredpixel.shatteredpixeldungeon.desktop.DesktopLauncher*" -or
            $cmd -like "*-jar*desktop-*-debug.jar*"
        )
        !$isExcluded -and $isGame
    }
}

function Stop-AgentMinRecordProcesses {
    param([int[]]$ExcludeProcessIds = @())
    Get-AgentMinRecordProcesses -ExcludeProcessIds $ExcludeProcessIds | ForEach-Object {
        try {
            Stop-Process -Id $_.ProcessId -Force -ErrorAction Stop
            Write-Host "[AgentMin] Stopped PID $($_.ProcessId)"
        } catch {
            Write-Host "[AgentMin] Could not stop PID $($_.ProcessId): $($_.Exception.Message)"
        }
    }
}

function Build-AgentMinDebugJar {
    Write-Host "[AgentMin] Building desktop debug jar before recording..."
    & (Join-Path $Root "gradlew.bat") "desktop:debugJar"
    if ($LASTEXITCODE -ne 0) {
        throw "desktop:debugJar failed with exit code $LASTEXITCODE"
    }
    $jar = Get-ChildItem -LiteralPath (Join-Path $Root "desktop\build\libs") -Filter "desktop-*-debug.jar" -File |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
    if ($null -eq $jar) {
        throw "No desktop debug jar found in desktop\build\libs"
    }
    return $jar.FullName
}

function Start-AgentMinRecordGame {
    param([string]$ResolvedJarPath)
    Write-Host "[AgentMin] Starting desktop debug jar with dataset recording enabled..."
    $env:AGENTMIN_ENABLED = "false"
    $env:AGENTMIN_RECORD_ENABLED = "true"
    $env:AGENTMIN_RECORD_DIR = $DatasetDir
    $env:AGENTMIN_RECORD_REWARD_INTERVAL = [Math]::Max(1, $RewardReportInterval).ToString()
    $env:AGENTMIN_LOGGING = "true"

    Start-Process -FilePath $Java `
        -ArgumentList @("-jar", $ResolvedJarPath) `
        -WorkingDirectory $Root `
        -PassThru
}

if ($StopExisting) {
    Write-Host "[AgentMin] Stopping existing desktop debug game processes..."
    Stop-AgentMinRecordProcesses -ExcludeProcessIds @($PID)
    Start-Sleep -Seconds 2
}

if ($SkipBuild -and $JarPath -ne "") {
    if (!(Test-Path $JarPath)) {
        throw "Provided jar path not found: $JarPath"
    }
    $desktopJar = (Resolve-Path $JarPath).Path
} else {
    $desktopJar = Build-AgentMinDebugJar
}

if ([string]::IsNullOrWhiteSpace($DatasetDir)) {
    throw "DatasetDir is required."
}

if (!(Test-Path $DatasetDir)) {
    New-Item -ItemType Directory -Force -Path $DatasetDir | Out-Null
}

$gameProcess = Start-AgentMinRecordGame -ResolvedJarPath $desktopJar
Write-Host "[AgentMin] Dataset directory: $DatasetDir"
Write-Host "[AgentMin] Reward summary interval: $([Math]::Max(1, $RewardReportInterval)) turns"
Write-Host "[AgentMin] Game PID: $($gameProcess.Id)"
Write-Host "[AgentMin] Recording will stop automatically when the game process closes."

Wait-Process -Id $gameProcess.Id
Write-Host "[AgentMin] Game process closed. Recording session finished."
