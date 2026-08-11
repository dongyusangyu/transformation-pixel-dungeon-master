param(
    [string]$OutputPath = "core/src/main/assets/sprites/pestilence_knight.png"
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

$frameSize = 16
$frameCount = 17
$bitmap = [System.Drawing.Bitmap]::new(
    $frameSize * $frameCount,
    $frameSize,
    [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)

$palette = @{
    outline = [System.Drawing.Color]::FromArgb(255, 23, 20, 31)
    deep    = [System.Drawing.Color]::FromArgb(255, 39, 34, 43)
    cloth   = [System.Drawing.Color]::FromArgb(255, 58, 52, 57)
    ivory   = [System.Drawing.Color]::FromArgb(255, 193, 186, 157)
    light   = [System.Drawing.Color]::FromArgb(255, 232, 221, 183)
    metal   = [System.Drawing.Color]::FromArgb(255, 105, 103, 92)
    verdigris = [System.Drawing.Color]::FromArgb(255, 67, 103, 82)
    plague  = [System.Drawing.Color]::FromArgb(255, 132, 174, 66)
    glow    = [System.Drawing.Color]::FromArgb(255, 196, 218, 105)
    gold    = [System.Drawing.Color]::FromArgb(255, 169, 132, 69)
    purple  = [System.Drawing.Color]::FromArgb(255, 91, 54, 105)
}

function Set-Pixel([int]$frame, [int]$x, [int]$y, [System.Drawing.Color]$color) {
    if ($x -ge 0 -and $x -lt 16 -and $y -ge 0 -and $y -lt 16) {
        $bitmap.SetPixel($frame * 16 + $x, $y, $color)
    }
}

function Draw-Line([int]$frame, [int]$x0, [int]$y0, [int]$x1, [int]$y1,
        [System.Drawing.Color]$color) {
    $dx = [Math]::Abs($x1 - $x0); $sx = if ($x0 -lt $x1) { 1 } else { -1 }
    $dy = -[Math]::Abs($y1 - $y0); $sy = if ($y0 -lt $y1) { 1 } else { -1 }
    $err = $dx + $dy
    while ($true) {
        Set-Pixel $frame $x0 $y0 $color
        if ($x0 -eq $x1 -and $y0 -eq $y1) { break }
        $twice = 2 * $err
        if ($twice -ge $dy) { $err += $dy; $x0 += $sx }
        if ($twice -le $dx) { $err += $dx; $y0 += $sy }
    }
}

function Draw-Base([int]$frame, [int]$bob, [int]$leftFoot, [int]$rightFoot,
        [bool]$raisedStaff, [bool]$castGlow) {
    # Broken circular crown/halo: four isolated anchors keep the silhouette readable at 1x.
    Set-Pixel $frame 5 (1 + $bob) $palette.gold
    Set-Pixel $frame 9 (1 + $bob) $palette.gold
    Set-Pixel $frame 4 (2 + $bob) $palette.outline
    Set-Pixel $frame 10 (2 + $bob) $palette.outline

    # Hood and ivory plague mask with a left-facing hooked beak.
    Draw-Line $frame 6 (2 + $bob) 9 (2 + $bob) $palette.outline
    Draw-Line $frame 5 (3 + $bob) 9 (3 + $bob) $palette.outline
    Set-Pixel $frame 5 (4 + $bob) $palette.outline
    Set-Pixel $frame 6 (4 + $bob) $palette.ivory
    Set-Pixel $frame 7 (4 + $bob) $palette.light
    Set-Pixel $frame 8 (4 + $bob) $palette.ivory
    Set-Pixel $frame 9 (4 + $bob) $palette.outline
    Draw-Line $frame 2 (5 + $bob) 8 (5 + $bob) $palette.outline
    Draw-Line $frame 3 (4 + $bob) 7 (4 + $bob) $palette.ivory
    Set-Pixel $frame 2 (4 + $bob) $palette.light
    Set-Pixel $frame 7 (3 + $bob) $palette.verdigris

    # Inverted-triangle coat, shoulder armour, and oxidised clasp.
    Draw-Line $frame 4 (6 + $bob) 10 (6 + $bob) $palette.outline
    Set-Pixel $frame 4 (7 + $bob) $palette.metal
    Set-Pixel $frame 10 (7 + $bob) $palette.metal
    for ($y = 7 + $bob; $y -le 12; $y++) {
        $inset = [Math]::Max(0, $y - (10 + $bob))
        $left = 4 + $inset; $right = 10 - $inset
        Set-Pixel $frame $left $y $palette.outline
        Set-Pixel $frame $right $y $palette.outline
        for ($x = $left + 1; $x -lt $right; $x++) {
            $shade = if ($x -le 6) { $palette.cloth } else { $palette.deep }
            Set-Pixel $frame $x $y $shade
        }
    }
    Set-Pixel $frame 6 (7 + $bob) $palette.verdigris
    Set-Pixel $frame 7 (8 + $bob) $palette.gold
    Set-Pixel $frame 5 (10 + $bob) $palette.ivory
    Set-Pixel $frame 9 (11 + $bob) $palette.verdigris

    # Split coat tails and planted armoured feet.
    Draw-Line $frame 5 12 (5 + $leftFoot) 14 $palette.outline
    Draw-Line $frame 9 12 (9 + $rightFoot) 14 $palette.outline
    Set-Pixel $frame (4 + $leftFoot) 15 $palette.metal
    Set-Pixel $frame (5 + $leftFoot) 15 $palette.outline
    Set-Pixel $frame (9 + $rightFoot) 15 $palette.outline
    Set-Pixel $frame (10 + $rightFoot) 15 $palette.metal

    # The diagnosis staff rises above the body; its diamond head is the fourth silhouette anchor.
    $staffX = if ($raisedStaff) { 12 } else { 13 }
    $top = if ($raisedStaff) { 0 } else { 2 }
    Draw-Line $frame $staffX $top $staffX 14 $palette.outline
    Set-Pixel $frame ($staffX - 1) ($top + 1) $palette.verdigris
    Set-Pixel $frame $staffX $top $palette.light
    Set-Pixel $frame ($staffX + 1) ($top + 1) $palette.gold
    if ($castGlow) {
        Set-Pixel $frame ($staffX - 1) ($top + 2) $palette.glow
        Set-Pixel $frame ($staffX + 1) ($top + 2) $palette.plague
        Set-Pixel $frame 3 (9 + $bob) $palette.plague
        Set-Pixel $frame 11 (10 + $bob) $palette.glow
    }
}

# Idle: restrained breathing; Run: opposing feet and coat bob.
Draw-Base 0 0 0 0 $false $false
Draw-Base 1 1 0 0 $false $false
Draw-Base 2 0 0 0 $false $false
Draw-Base 3 0 -1 1 $false $false
Draw-Base 4 1 0 0 $false $false
Draw-Base 5 0 1 -1 $false $false
Draw-Base 6 1 0 0 $false $false

# Attack: staff retracts, sweeps left, then settles.
Draw-Base 7 0 0 0 $true $false
Draw-Base 8 0 -1 0 $true $false
Draw-Line 8 3 8 13 5 $palette.outline
Set-Pixel 8 3 8 $palette.light
Draw-Base 9 1 0 0 $false $false

# Cast/harvest: raised staff and asymmetric yellow-green plague light.
Draw-Base 10 0 0 0 $true $true
Draw-Base 11 1 0 0 $true $true
Set-Pixel 11 2 10 $palette.glow
Set-Pixel 11 11 8 $palette.plague
Draw-Base 12 0 0 0 $true $true
Set-Pixel 12 7 6 $palette.light

# Death: recoil, kneel, collapse, dark residue. Key poses beat in-between noise.
Draw-Base 13 1 0 0 $false $false
Draw-Base 14 2 -1 0 $false $false
for ($x = 3; $x -le 11; $x++) { Set-Pixel 15 $x 13 $palette.outline }
for ($x = 4; $x -le 10; $x++) { Set-Pixel 15 $x 14 $palette.deep }
Set-Pixel 15 2 12 $palette.ivory; Set-Pixel 15 11 12 $palette.verdigris
for ($x = 2; $x -le 12; $x++) { Set-Pixel 16 $x 14 $palette.outline }
for ($x = 4; $x -le 10; $x++) { Set-Pixel 16 $x 13 $palette.deep }
Set-Pixel 16 2 13 $palette.ivory; Set-Pixel 16 12 13 $palette.purple

$absolute = [System.IO.Path]::GetFullPath((Join-Path (Get-Location) $OutputPath))
$directory = [System.IO.Path]::GetDirectoryName($absolute)
if (-not [System.IO.Directory]::Exists($directory)) {
    [System.IO.Directory]::CreateDirectory($directory) | Out-Null
}
$bitmap.Save($absolute, [System.Drawing.Imaging.ImageFormat]::Png)
$bitmap.Dispose()
Write-Output $absolute
