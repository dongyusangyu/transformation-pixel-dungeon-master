param(
    [int]$Port = 8765,
    [int]$UpdateInterval = 256,
    [int]$WarmupSeconds = 45,
    [string]$Device = "auto",
    [int]$PPOEpochs = 4,
    [int]$PPOMinibatchSize = 128,
    [double]$PPOLearningRate = 0.00006,
    [double]$PPOGamma = 0.997,
    [double]$PPOGAELambda = 0.97,
    [double]$PPOEntropyCoef = 0.018,
    [double]$PPOClipRange = 0.18,
    [double]$PPOValueCoef = 0.55,
    [double]$PPOMaxGradNorm = 0.6,
    [double]$PPOWeightDecay = 0.01,
    [bool]$DisableAMP = $false,
    [string]$JarPath = "",
    [switch]$SkipBuild,
    [bool]$StopExisting = $true,
    [bool]$ClearUnfinishedSaves = $true,
    [string]$SaveRoot = "",
    [bool]$MonitorGameProcess = $true,
    [int]$MonitorSeconds = 0,
    [switch]$StopGameOnExit
)

$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Python = "D:\anaconda\envs\wy\python.exe"
$TrainingDir = Join-Path $Root "core\src\main\java\com\shatteredpixel\shatteredpixeldungeon\custom\agentMin\training"
$Server = Join-Path $TrainingDir "agent_min_bridge_server.py"
$Log = Join-Path $TrainingDir "logs\bridge_server.log"
$Checkpoint = Join-Path $TrainingDir "logs\agent_min_real_game_$Port.pt"
$CrashLog = Join-Path $TrainingDir "logs\game_crashes.log"
$CrashMarker = Join-Path $TrainingDir "logs\game_crash.marker"
$SupervisorLog = Join-Path $TrainingDir "logs\training_supervisor.log"
$Java = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME "bin\java.exe" } else { "java.exe" }

if (!(Test-Path $Python)) {
    throw "Python env not found: $Python"
}
if (!(Test-Path $Server)) {
    throw "Bridge server script not found: $Server"
}
if (!(Test-Path $Java)) {
    $Java = "java.exe"
}

function Get-AgentMinProcesses {
    param([bool]$IncludeBridge = $true, [bool]$IncludeGame = $true, [int[]]$ExcludeProcessIds = @())
    Get-CimInstance Win32_Process | Where-Object {
        $cmd = if ($_.CommandLine) { $_.CommandLine } else { "" }
        $name = if ($_.Name) { $_.Name.ToLowerInvariant() } else { "" }
        $isExcluded = $ExcludeProcessIds -contains $_.ProcessId
        $isBridge = $IncludeBridge -and $name -like "python*.exe" -and $cmd -like "*agent_min_bridge_server.py*"
        $isGame = $IncludeGame -and ($name -eq "java.exe" -or $name -eq "javaw.exe") -and (
            $cmd -like "*com.shatteredpixel.shatteredpixeldungeon.desktop.DesktopLauncher*" -or
            $cmd -like "*-jar*desktop-*.jar*"
        )
        !$isExcluded -and ($isBridge -or $isGame)
    }
}

function Stop-AgentMinProcesses {
    param([bool]$IncludeBridge = $true, [bool]$IncludeGame = $true, [int[]]$ExcludeProcessIds = @())
    Get-AgentMinProcesses -IncludeBridge $IncludeBridge -IncludeGame $IncludeGame -ExcludeProcessIds $ExcludeProcessIds |
        ForEach-Object {
            try {
                Stop-Process -Id $_.ProcessId -Force -ErrorAction Stop
                Write-Host "[AgentMin] Stopped PID $($_.ProcessId)"
            } catch {
                Write-Host "[AgentMin] Could not stop PID $($_.ProcessId): $($_.Exception.Message)"
            }
        }
}

function Write-AgentMinSupervisorLog {
    param([string]$Message)
    $dir = Split-Path -Parent $SupervisorLog
    if (!(Test-Path $dir)) {
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
    }
    $line = "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss.fff') | $Message"
    Add-Content -Path $SupervisorLog -Value $line -Encoding UTF8
    Write-Host "[AgentMin] $Message"
}

function Test-RecentAgentMinCrash {
    param([datetime]$Since)
    if (!(Test-Path $CrashMarker)) {
        return $false
    }
    $marker = Get-Item -LiteralPath $CrashMarker
    return $marker.LastWriteTime -ge $Since.AddSeconds(-2)
}

function Get-AgentMinSaveRoots {
    $roots = New-Object System.Collections.Generic.List[string]
    if ($SaveRoot -ne "") {
        if (Test-Path $SaveRoot) {
            $roots.Add((Resolve-Path $SaveRoot).Path)
        }
        return $roots
    }

    $vendorRoot = Join-Path $env:APPDATA ".shatteredpixel"
    if (!(Test-Path $vendorRoot)) {
        return $roots
    }

    foreach ($title in @("蜕变地牢", "铚曞彉鍦扮墷")) {
        $candidate = Join-Path $vendorRoot $title
        if (Test-Path $candidate) {
            $resolved = (Resolve-Path $candidate).Path
            if (!$roots.Contains($resolved)) {
                $roots.Add($resolved)
            }
        }
    }

    if ($roots.Count -eq 0) {
        Get-ChildItem -LiteralPath $vendorRoot -Directory -ErrorAction SilentlyContinue | ForEach-Object {
            $hasSaveSlots = @(Get-ChildItem -LiteralPath $_.FullName -Filter "save-*.json" -File -ErrorAction SilentlyContinue).Count -gt 0
            $hasLegacySlots = @(Get-ChildItem -LiteralPath $_.FullName -Directory -ErrorAction SilentlyContinue | Where-Object { $_.Name -match "^game\d+$" }).Count -gt 0
            if ($hasSaveSlots -or $hasLegacySlots) {
                $roots.Add($_.FullName)
            }
        }
    }

    return $roots
}

function Clear-AgentMinUnfinishedSaves {
    $roots = @(Get-AgentMinSaveRoots)
    if ($roots.Count -eq 0) {
        Write-Host "[AgentMin] No local save directory found to clear."
        return
    }

    foreach ($root in $roots) {
        $resolvedRoot = (Resolve-Path $root).Path
        Write-Host "[AgentMin] Clearing unfinished save slots in $resolvedRoot"

        for ($slot = 1; $slot -le 12; $slot++) {
            $saveFile = Join-Path $resolvedRoot ("save-{0:D3}.json" -f $slot)
            if (Test-Path $saveFile) {
                Remove-Item -LiteralPath $saveFile -Force
                Write-Host "[AgentMin] Removed $saveFile"
            }

            $legacyDir = Join-Path $resolvedRoot ("game{0}" -f $slot)
            if (Test-Path $legacyDir) {
                $resolvedLegacy = (Resolve-Path $legacyDir).Path
                if ($resolvedLegacy.StartsWith($resolvedRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
                    Remove-Item -LiteralPath $resolvedLegacy -Recurse -Force
                    Write-Host "[AgentMin] Removed $resolvedLegacy"
                }
            }
        }
    }
}

function Build-AgentMinDesktopJar {
    Write-AgentMinSupervisorLog "Building desktop debug jar before training."
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
    Write-AgentMinSupervisorLog "Using desktop debug jar: $($jar.FullName)"
    return $jar.FullName
}

function Start-AgentMinBridge {
    Write-Host "[AgentMin] Starting Python realtime training bridge..."
    $args = @(
        $Server,
        "--host", "127.0.0.1",
        "--port", "$Port",
        "--update-interval", "$UpdateInterval",
        "--checkpoint", $Checkpoint,
        "--device", $Device,
        "--ppo-epochs", "$PPOEpochs",
        "--ppo-minibatch-size", "$PPOMinibatchSize",
        "--ppo-lr", "$PPOLearningRate",
        "--ppo-gamma", "$PPOGamma",
        "--ppo-gae-lambda", "$PPOGAELambda",
        "--ppo-entropy-coef", "$PPOEntropyCoef",
        "--ppo-clip-range", "$PPOClipRange",
        "--ppo-value-coef", "$PPOValueCoef",
        "--ppo-max-grad-norm", "$PPOMaxGradNorm",
        "--ppo-weight-decay", "$PPOWeightDecay"
    )
    if ($DisableAMP) {
        $args += "--disable-amp"
    }
    Start-Process -FilePath $Python `
        -ArgumentList $args `
        -WorkingDirectory $Root `
        -WindowStyle Hidden `
        -PassThru
}

function Start-AgentMinGame {
    param([string]$JarPath)
    Write-Host "[AgentMin] Starting desktop game, auto-selecting Warrior and creating a run..."
    $env:AGENTMIN_ENABLED = "true"
    $env:AGENTMIN_AUTO_START = "true"
    $env:AGENTMIN_HOST = "127.0.0.1"
    $env:AGENTMIN_PORT = "$Port"
    $env:AGENTMIN_TIMEOUT_MS = "5000"
    $env:AGENTMIN_LOGGING = "true"
    $env:AGENTMIN_CRASH_LOG = $CrashLog
    $env:AGENTMIN_CRASH_MARKER = $CrashMarker

    Start-Process -FilePath $Java `
        -ArgumentList @("-jar", $JarPath) `
        -WorkingDirectory $Root `
        -PassThru
}

if ($StopExisting) {
    Write-Host "[AgentMin] Stopping existing AgentMin bridge/game processes..."
    Stop-AgentMinProcesses -IncludeBridge $true -IncludeGame $true -ExcludeProcessIds @($PID)
    Start-Sleep -Seconds 2
}
if ($ClearUnfinishedSaves) {
    Clear-AgentMinUnfinishedSaves
}
if ($SkipBuild -and $JarPath -ne "") {
    if (!(Test-Path $JarPath)) {
        throw "Provided jar path not found: $JarPath"
    }
    $desktopJar = (Resolve-Path $JarPath).Path
    Write-AgentMinSupervisorLog "Using prebuilt desktop jar: $desktopJar"
} else {
    $desktopJar = Build-AgentMinDesktopJar
}

$serverProcess = Start-AgentMinBridge
Start-Sleep -Seconds 2
$gameStartTime = Get-Date
$gameProcess = Start-AgentMinGame -JarPath $desktopJar

try {
    Write-Host "[AgentMin] Python launcher PID: $($serverProcess.Id)"
    Write-Host "[AgentMin] Game launcher PID: $($gameProcess.Id)"
    Write-Host "[AgentMin] Waiting $WarmupSeconds seconds for the first real game samples..."
    Start-Sleep -Seconds $WarmupSeconds

    if (Test-Path $Log) {
        Write-Host "[AgentMin] Latest bridge_server.log:"
        Get-Content -Path $Log -Tail 80
    } else {
        Write-Host "[AgentMin] Log file not created yet: $Log"
    }

    if (!$MonitorGameProcess) {
        Write-Host "[AgentMin] Training flow is running without process monitoring."
        return
    }

    Write-Host "[AgentMin] Process monitor is running. Game crashes restart the desktop process; manually closing the game stops this script."
    $started = Get-Date
    while ($true) {
        Start-Sleep -Seconds 5

        if ($MonitorSeconds -gt 0 -and ((Get-Date) - $started).TotalSeconds -ge $MonitorSeconds) {
            Write-Host "[AgentMin] Monitor time reached. Leaving processes running."
            break
        }

        $gameAlive = @(Get-AgentMinProcesses -IncludeBridge $false -IncludeGame $true).Count -gt 0
        if (!$gameAlive) {
            if (Test-RecentAgentMinCrash -Since $gameStartTime) {
                Write-AgentMinSupervisorLog "Desktop game crashed during training. Crash details were appended to $CrashLog. Restarting a new game process."
                if ($ClearUnfinishedSaves) {
                    Clear-AgentMinUnfinishedSaves
                }
                $gameStartTime = Get-Date
                $gameProcess = Start-AgentMinGame -JarPath $desktopJar
                Start-Sleep -Seconds 8
                continue
            } else {
                Write-AgentMinSupervisorLog "Desktop game process was closed without a new crash marker. Treating this as manual shutdown and stopping training."
                break
            }
        }
    }
} finally {
    Write-Host "[AgentMin] Stopping AgentMin bridge processes."
    Stop-AgentMinProcesses -IncludeBridge $true -IncludeGame $false -ExcludeProcessIds @($PID)
    if ($StopGameOnExit) {
        Write-Host "[AgentMin] Stopping AgentMin game processes."
        Stop-AgentMinProcesses -IncludeBridge $false -IncludeGame $true -ExcludeProcessIds @($PID)
    }
}
