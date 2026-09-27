$ErrorActionPreference = "Stop"
$RepoRoot = Split-Path -Parent $PSScriptRoot
Push-Location $RepoRoot
try {
    git config --local core.hooksPath .githooks
    if ($LASTEXITCODE -ne 0) { throw "Could not configure Git privacy hooks." }
    Write-Host "Privacy hooks installed for this clone."
} finally {
    Pop-Location
}
