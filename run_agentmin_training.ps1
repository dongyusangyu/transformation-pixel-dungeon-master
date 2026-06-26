param(
    [int]$Port = 8765,
    [int]$UpdateInterval = 64,
    [int]$WarmupSeconds = 15,
    [string]$Device = "auto",
    [int]$PPOEpochs = 1,
    [int]$PPOMinibatchSize = 2,
    [double]$PPOLearningRate = 0.000015,
    [double]$PPOGamma = 0.995,
    [double]$PPOGAELambda = 0.95,
    [double]$PPOEntropyCoef = 0.012,
    [double]$PPOClipRange = 0.10,
    [double]$PPOValueCoef = 0.50,
    [double]$PPOMaxGradNorm = 0.5,
    [double]$PPOWeightDecay = 0.0001,
    [double]$PPOTargetKL = 0.020,
    [bool]$DisableAMP = $false,
    [int]$MaxVramMB = 7168,
    [string]$JarPath = "",
    [string]$InitialCheckpoint = "",
    [bool]$PreferInitialCheckpoint = $false,
    [string]$Curriculum = "",
    [switch]$SkipBuild,
    [bool]$StopExisting = $true,
    [bool]$ClearUnfinishedSaves = $true,
    [string]$SaveRoot = "",
    [bool]$MonitorGameProcess = $true,
    [int]$MonitorSeconds = 0,
    [int]$BridgeStallSeconds = 90,
    [int]$BridgeRequestTimeoutMs = 60000,
    [string]$HeroClass = "WARRIOR",
    [string]$WandProbe = "false",
    [switch]$StopGameOnExit
)

$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Python = "D:\anaconda\envs\wy\python.exe"
$TrainingDir = Join-Path $Root "core\src\main\java\com\shatteredpixel\shatteredpixeldungeon\custom\agentMin\training"
$Server = Join-Path $TrainingDir "agent_min_bridge_server.py"
$Log = Join-Path $TrainingDir "logs\bridge_server.log"
$Checkpoint = Join-Path $TrainingDir "logs\agent_min_real_game_$Port.pt"
$InitialCheckpointPath = $InitialCheckpoint.Trim()
if ($InitialCheckpointPath -ne "" -and !(Test-Path -LiteralPath $InitialCheckpointPath)) {
    throw "Initial checkpoint not found: $InitialCheckpointPath"
}
$CurriculumName = $Curriculum.Trim()
if ($CurriculumName -ne "") {
    $safeCurriculum = $CurriculumName -replace "[^A-Za-z0-9_-]", "_"
    $Checkpoint = Join-Path $TrainingDir "logs\agent_min_${safeCurriculum}_$Port.pt"
}
$CrashLog = Join-Path $TrainingDir "logs\game_crashes.log"
$CrashMarker = Join-Path $TrainingDir "logs\game_crash.marker"
$EpisodeLog = Join-Path $TrainingDir "logs\agent_min_episode_results.log"
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

    $matched = New-Object System.Collections.Generic.List[object]
    $candidates = New-Object System.Collections.Generic.List[object]
    if ($IncludeBridge) {
        Get-Process -Name "python", "python3" -ErrorAction SilentlyContinue | ForEach-Object { $candidates.Add($_) }
    }
    if ($IncludeGame) {
        Get-Process -Name "java", "javaw" -ErrorAction SilentlyContinue | ForEach-Object { $candidates.Add($_) }
    }

    foreach ($process in $candidates) {
        if ($ExcludeProcessIds -contains $process.Id) {
            continue
        }
        $name = if ($process.ProcessName) { ($process.ProcessName + ".exe").ToLowerInvariant() } else { "" }
        $cmd = ""
        try {
            $cim = Get-CimInstance Win32_Process -Filter "ProcessId=$($process.Id)" -ErrorAction Stop
            if ($cim -and $cim.CommandLine) {
                $cmd = $cim.CommandLine
            }
        } catch {
            Write-AgentMinSupervisorLog "Process command line probe skipped for PID $($process.Id): $($_.Exception.Message)"
            continue
        }

        $isBridge = $IncludeBridge -and $name -like "python*.exe" -and $cmd -like "*agent_min_bridge_server.py*"
        $isGame = $IncludeGame -and ($name -eq "java.exe" -or $name -eq "javaw.exe") -and $cmd -like "*-jar*desktop-*-debug.jar*"
        if ($isBridge -or $isGame) {
            $matched.Add([pscustomobject]@{
                ProcessId = $process.Id
                Name = $name
                CommandLine = $cmd
            })
        }
    }
    return $matched
}

function Assert-AgentMinDebugJar {
    param([string]$Path)
    $name = [System.IO.Path]::GetFileName($Path)
    if ($name -notlike "desktop-*-debug.jar") {
        throw "AgentMin training requires a desktop debug jar. Got: $Path"
    }
}

function Confirm-AgentMinDebugJarContents {
    param([string]$Path)
    Assert-AgentMinDebugJar -Path $Path
    if (!(Test-Path $Path)) {
        throw "Debug jar not found: $Path"
    }

    Add-Type -AssemblyName System.IO.Compression.FileSystem -ErrorAction SilentlyContinue
    $requiredClasses = @(
        "com/shatteredpixel/shatteredpixeldungeon/actors/buffs/Sleep.class",
        "com/shatteredpixel/shatteredpixeldungeon/levels/traps/PoisonDartTrap`$1.class",
        "com/shatteredpixel/shatteredpixeldungeon/levels/traps/PoisonDartTrap`$1`$1.class",
        "com/shatteredpixel/shatteredpixeldungeon/effects/particles/EarthParticle.class",
        "com/shatteredpixel/shatteredpixeldungeon/effects/Beam`$LightRay.class",
        "com/shatteredpixel/shatteredpixeldungeon/ui/BuffIcon.class",
        "com/badlogic/gdx/backends/lwjgl3/Lwjgl3Window`$4`$1.class"
    )
    $zip = $null
    $missing = New-Object System.Collections.Generic.List[string]
    try {
        $zip = [System.IO.Compression.ZipFile]::OpenRead($Path)
        $entries = New-Object "System.Collections.Generic.HashSet[string]"
        foreach ($entry in $zip.Entries) {
            [void]$entries.Add($entry.FullName)
        }
        foreach ($className in $requiredClasses) {
            if (!$entries.Contains($className)) {
                $missing.Add($className)
            }
        }
    } finally {
        if ($null -ne $zip) {
            $zip.Dispose()
        }
    }
    if ($missing.Count -gt 0) {
        throw "Debug jar is missing required AgentMin runtime classes: $($missing -join ', ')"
    }
    Write-AgentMinSupervisorLog "Debug jar class check OK: $Path"
}

function Stop-AgentMinProcesses {
    param([bool]$IncludeBridge = $true, [bool]$IncludeGame = $true, [int[]]$ExcludeProcessIds = @())
    try {
        $processes = @(Get-AgentMinProcesses -IncludeBridge $IncludeBridge -IncludeGame $IncludeGame -ExcludeProcessIds $ExcludeProcessIds)
    } catch {
        Write-AgentMinSupervisorLog "Process scan failed while stopping AgentMin processes: $($_.Exception.Message)"
        return
    }
    foreach ($process in $processes) {
        try {
            Stop-Process -Id $process.ProcessId -Force -ErrorAction Stop
            Write-Host "[AgentMin] Stopped PID $($process.ProcessId)"
        } catch {
            Write-Host "[AgentMin] Could not stop PID $($process.ProcessId): $($_.Exception.Message)"
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

function Test-AgentMinProcessAlive {
    param([object]$Process)
    if ($null -eq $Process) {
        return $false
    }
    try {
        $Process.Refresh()
        return !$Process.HasExited
    } catch {
        Write-AgentMinSupervisorLog "Process handle probe failed: $($_.Exception.Message)"
        return $false
    }
}

function Get-AgentMinLogAgeSeconds {
    param([string]$Path)
    if (!(Test-Path $Path)) {
        return [double]::PositiveInfinity
    }
    try {
        return ((Get-Date) - (Get-Item -LiteralPath $Path).LastWriteTime).TotalSeconds
    } catch {
        Write-AgentMinSupervisorLog "Log age probe failed for ${Path}: $($_.Exception.Message)"
        return 0
    }
}

function Restart-AgentMinTrainingRun {
    param([string]$Reason)
    Write-AgentMinSupervisorLog "$Reason Restarting bridge and desktop game."
    Stop-AgentMinProcesses -IncludeBridge $true -IncludeGame $true -ExcludeProcessIds @($PID)
    if ($ClearUnfinishedSaves) {
        Clear-AgentMinUnfinishedSaves
    }
    Start-Sleep -Seconds 2
    $script:serverProcess = Start-AgentMinBridge
    Start-Sleep -Seconds 2
    $script:gameStartTime = Get-Date
    $script:gameProcess = Start-AgentMinGame -JarPath $script:desktopJar
    Start-Sleep -Seconds 8
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
    Confirm-AgentMinDebugJarContents -Path $jar.FullName
    return $jar.FullName
}

function Start-AgentMinBridge {
    Write-Host "[AgentMin] Starting Python realtime training bridge..."
    $bridgeEnv = [System.Environment]::GetEnvironmentVariable("PYTORCH_CUDA_ALLOC_CONF", "Process")
    if ([string]::IsNullOrWhiteSpace($bridgeEnv)) {
        [System.Environment]::SetEnvironmentVariable("PYTORCH_CUDA_ALLOC_CONF", "expandable_segments:True,max_split_size_mb:64", "Process")
    }
    [System.Environment]::SetEnvironmentVariable("CUDA_MODULE_LOADING", "LAZY", "Process")
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
        "--ppo-weight-decay", "$PPOWeightDecay",
        "--ppo-target-kl", "$PPOTargetKL",
        "--max-vram-mb", "$MaxVramMB"
    )
    if ($InitialCheckpointPath -ne "") {
        $args += @("--initial-checkpoint", $InitialCheckpointPath)
        Write-Host "[AgentMin] Initializing PPO from supervised checkpoint: $InitialCheckpointPath"
    }
    if ($PreferInitialCheckpoint) {
        $args += "--prefer-initial-checkpoint"
    }
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
    Assert-AgentMinDebugJar -Path $JarPath
    Confirm-AgentMinDebugJarContents -Path $JarPath
    Write-Host "[AgentMin] Starting desktop game, auto-selecting $HeroClass and creating a run..."
    $env:AGENTMIN_ENABLED = "true"
    $env:AGENTMIN_AUTO_START = "true"
    $env:AGENTMIN_HOST = "127.0.0.1"
    $env:AGENTMIN_PORT = "$Port"
    $env:AGENTMIN_TIMEOUT_MS = "$BridgeRequestTimeoutMs"
    $env:AGENTMIN_LOGGING = "true"
	$env:AGENTMIN_HERO_CLASS = $HeroClass
	$env:AGENTMIN_WAND_PROBE = $WandProbe
    $env:AGENTMIN_CRASH_LOG = $CrashLog
    $env:AGENTMIN_CRASH_MARKER = $CrashMarker
    $env:AGENTMIN_EPISODE_LOG = $EpisodeLog
    if ($CurriculumName -ne "") {
        $env:AGENTMIN_CURRICULUM = $CurriculumName
        Write-Host "[AgentMin] Curriculum mode: $CurriculumName"
    } else {
        Remove-Item Env:\AGENTMIN_CURRICULUM -ErrorAction SilentlyContinue
    }

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
    Assert-AgentMinDebugJar -Path $desktopJar
    try {
        Confirm-AgentMinDebugJarContents -Path $desktopJar
        Write-AgentMinSupervisorLog "Using prebuilt desktop jar: $desktopJar"
    } catch {
        Write-AgentMinSupervisorLog "Prebuilt debug jar failed class check: $($_.Exception.Message). Rebuilding desktop:debugJar."
        $desktopJar = Build-AgentMinDesktopJar
        Assert-AgentMinDebugJar -Path $desktopJar
    }
} else {
    $desktopJar = Build-AgentMinDesktopJar
    Assert-AgentMinDebugJar -Path $desktopJar
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

    Write-Host "[AgentMin] Process monitor is running. Game crashes or stalled bridge decisions restart training; manually closing the game stops this script."
    $started = Get-Date
    while ($true) {
        Start-Sleep -Seconds 5

        if ($MonitorSeconds -gt 0 -and ((Get-Date) - $started).TotalSeconds -ge $MonitorSeconds) {
            Write-Host "[AgentMin] Monitor time reached. Leaving processes running."
            break
        }

        $gameAlive = Test-AgentMinProcessAlive -Process $gameProcess
        if (!$gameAlive) {
            try {
                $gameAlive = @(Get-AgentMinProcesses -IncludeBridge $false -IncludeGame $true).Count -gt 0
            } catch {
                Write-AgentMinSupervisorLog "Fallback game process scan failed: $($_.Exception.Message)"
                $gameAlive = $false
            }
        }
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

        $bridgeAlive = Test-AgentMinProcessAlive -Process $serverProcess
        if (!$bridgeAlive) {
            Restart-AgentMinTrainingRun -Reason "Python bridge process exited while the game was still running."
            continue
        }

        if ($BridgeStallSeconds -gt 0) {
            $age = Get-AgentMinLogAgeSeconds -Path $Log
            $runAge = ((Get-Date) - $gameStartTime).TotalSeconds
            if ($runAge -gt [Math]::Max($WarmupSeconds, 30) -and $age -gt $BridgeStallSeconds) {
                Restart-AgentMinTrainingRun -Reason "No bridge decision log update for $([int]$age) seconds."
                continue
            }
        }
    }
} finally {
    Write-Host "[AgentMin] Stopping AgentMin bridge processes."
    try {
        if ($null -ne $serverProcess) {
            $serverProcess.Refresh()
            if (!$serverProcess.HasExited) {
                Stop-Process -Id $serverProcess.Id -Force -ErrorAction Stop
                Write-Host "[AgentMin] Stopped bridge PID $($serverProcess.Id)"
            }
        }
    } catch {
        Write-AgentMinSupervisorLog "Could not stop tracked bridge process: $($_.Exception.Message)"
    }
    Stop-AgentMinProcesses -IncludeBridge $true -IncludeGame $false -ExcludeProcessIds @($PID)
    if ($StopGameOnExit) {
        Write-Host "[AgentMin] Stopping AgentMin game processes."
        try {
            if ($null -ne $gameProcess) {
                $gameProcess.Refresh()
                if (!$gameProcess.HasExited) {
                    Stop-Process -Id $gameProcess.Id -Force -ErrorAction Stop
                    Write-Host "[AgentMin] Stopped game PID $($gameProcess.Id)"
                }
            }
        } catch {
            Write-AgentMinSupervisorLog "Could not stop tracked game process: $($_.Exception.Message)"
        }
        Stop-AgentMinProcesses -IncludeBridge $false -IncludeGame $true -ExcludeProcessIds @($PID)
    }
}
