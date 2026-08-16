param(
    [string]$OutputPath = "core/src/main/assets/sprites/pestilence_knight.png"
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

$frameSize = 32
$frameCount = 17
$bitmap = [System.Drawing.Bitmap]::new(
    $frameSize * $frameCount,
    $frameSize,
    [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)

# Sixteen opaque colours plus transparent black. The hierarchy follows the
# concept sheet: charcoal silhouette, black-violet cloth, bone mask, old brass,
# and very restrained plague light.
$palette = @{
    outline    = [System.Drawing.Color]::FromArgb(255, 18, 15, 27)
    abyss      = [System.Drawing.Color]::FromArgb(255, 28, 23, 39)
    deep       = [System.Drawing.Color]::FromArgb(255, 42, 32, 54)
    cloth      = [System.Drawing.Color]::FromArgb(255, 61, 43, 70)
    clothHi    = [System.Drawing.Color]::FromArgb(255, 82, 56, 86)
    steelDark  = [System.Drawing.Color]::FromArgb(255, 66, 68, 78)
    steel      = [System.Drawing.Color]::FromArgb(255, 102, 105, 113)
    ivoryDark  = [System.Drawing.Color]::FromArgb(255, 143, 132, 104)
    ivory      = [System.Drawing.Color]::FromArgb(255, 201, 190, 148)
    ivoryHi    = [System.Drawing.Color]::FromArgb(255, 239, 228, 179)
    brassDark  = [System.Drawing.Color]::FromArgb(255, 101, 72, 38)
    brass      = [System.Drawing.Color]::FromArgb(255, 169, 126, 58)
    verdigris  = [System.Drawing.Color]::FromArgb(255, 57, 91, 78)
    plague     = [System.Drawing.Color]::FromArgb(255, 125, 169, 56)
    glow       = [System.Drawing.Color]::FromArgb(255, 205, 226, 91)
    purple     = [System.Drawing.Color]::FromArgb(255, 104, 56, 118)
}

function Set-Pixel([int]$frame, [int]$x, [int]$y, [System.Drawing.Color]$color) {
    if ($x -ge 0 -and $x -lt $frameSize -and $y -ge 0 -and $y -lt $frameSize) {
        $bitmap.SetPixel($frame * $frameSize + $x, $y, $color)
    }
}

function Fill-Rect([int]$frame, [int]$x, [int]$y, [int]$width, [int]$height,
        [System.Drawing.Color]$color) {
    for ($py = $y; $py -lt $y + $height; $py++) {
        for ($px = $x; $px -lt $x + $width; $px++) {
            Set-Pixel $frame $px $py $color
        }
    }
}

function Draw-Line([int]$frame, [int]$x0, [int]$y0, [int]$x1, [int]$y1,
        [System.Drawing.Color]$color) {
    $dx = [Math]::Abs($x1 - $x0)
    $sx = if ($x0 -lt $x1) { 1 } else { -1 }
    $dy = -[Math]::Abs($y1 - $y0)
    $sy = if ($y0 -lt $y1) { 1 } else { -1 }
    $err = $dx + $dy
    while ($true) {
        Set-Pixel $frame $x0 $y0 $color
        if ($x0 -eq $x1 -and $y0 -eq $y1) { break }
        $twice = 2 * $err
        if ($twice -ge $dy) { $err += $dy; $x0 += $sx }
        if ($twice -le $dx) { $err += $dx; $y0 += $sy }
    }
}

function Draw-Halo([int]$frame, [int]$shift, [int]$bob) {
    # A broken brass arc sits behind the hat. Its gaps read as a ruined crown.
    Set-Pixel $frame (10 + $shift) (3 + $bob) $palette.brassDark
    Set-Pixel $frame (11 + $shift) (2 + $bob) $palette.brass
    Set-Pixel $frame (15 + $shift) (1 + $bob) $palette.brass
    Set-Pixel $frame (19 + $shift) (2 + $bob) $palette.brass
    Set-Pixel $frame (20 + $shift) (3 + $bob) $palette.brassDark
}

function Draw-Head([int]$frame, [int]$shift, [int]$bob) {
    Draw-Halo $frame $shift $bob

    # Crown and broad torn brim.
    Fill-Rect $frame (12 + $shift) (3 + $bob) 8 1 $palette.outline
    Fill-Rect $frame (10 + $shift) (4 + $bob) 12 2 $palette.outline
    Fill-Rect $frame (11 + $shift) (4 + $bob) 9 1 $palette.deep
    Fill-Rect $frame (7 + $shift) (6 + $bob) 18 2 $palette.outline
    Fill-Rect $frame (9 + $shift) (6 + $bob) 13 1 $palette.cloth
    Set-Pixel $frame (6 + $shift) (7 + $bob) $palette.outline
    Set-Pixel $frame (24 + $shift) (8 + $bob) $palette.outline

    # Hood and ivory faceplate.
    Fill-Rect $frame (11 + $shift) (8 + $bob) 10 6 $palette.outline
    Fill-Rect $frame (12 + $shift) (8 + $bob) 7 5 $palette.ivoryDark
    Fill-Rect $frame (13 + $shift) (8 + $bob) 5 4 $palette.ivory
    Set-Pixel $frame (14 + $shift) (8 + $bob) $palette.ivoryHi
    Set-Pixel $frame (12 + $shift) (10 + $bob) $palette.glow
    Set-Pixel $frame (13 + $shift) (10 + $bob) $palette.plague

    # Long hooked beak, deliberately separated from the chest by negative space.
    Draw-Line $frame (3 + $shift) (12 + $bob) (12 + $shift) (10 + $bob) $palette.outline
    Draw-Line $frame (3 + $shift) (12 + $bob) (8 + $shift) (15 + $bob) $palette.outline
    Draw-Line $frame (8 + $shift) (15 + $bob) (13 + $shift) (12 + $bob) $palette.outline
    Draw-Line $frame (5 + $shift) (12 + $bob) (11 + $shift) (11 + $bob) $palette.ivoryHi
    Draw-Line $frame (6 + $shift) (13 + $bob) (10 + $shift) (14 + $bob) $palette.ivory
    Set-Pixel $frame (4 + $shift) (12 + $bob) $palette.ivory
    Set-Pixel $frame (8 + $shift) (14 + $bob) $palette.ivoryDark
}

function Draw-Body([int]$frame, [int]$shift, [int]$bob, [int]$leftStep, [int]$rightStep) {
    # Neck, asymmetric pauldrons and segmented breastplate.
    Fill-Rect $frame (14 + $shift) (13 + $bob) 5 3 $palette.outline
    Fill-Rect $frame (15 + $shift) (13 + $bob) 3 2 $palette.steelDark
    Fill-Rect $frame (8 + $shift) (15 + $bob) 15 3 $palette.outline
    Fill-Rect $frame (9 + $shift) (15 + $bob) 4 2 $palette.steel
    Fill-Rect $frame (19 + $shift) (15 + $bob) 3 2 $palette.steelDark
    Set-Pixel $frame (10 + $shift) (15 + $bob) $palette.brass

    Fill-Rect $frame (10 + $shift) (18 + $bob) 12 7 $palette.outline
    Fill-Rect $frame (11 + $shift) (18 + $bob) 5 6 $palette.clothHi
    Fill-Rect $frame (16 + $shift) (18 + $bob) 5 6 $palette.deep
    Draw-Line $frame (16 + $shift) (18 + $bob) (16 + $shift) (24 + $bob) $palette.steelDark
    Draw-Line $frame (12 + $shift) (20 + $bob) (20 + $shift) (20 + $bob) $palette.steelDark
    Set-Pixel $frame (15 + $shift) (18 + $bob) $palette.brass
    Set-Pixel $frame (16 + $shift) (19 + $bob) $palette.verdigris

    # Split coat tails create a sharp mounted-rider silhouette without a horse.
    Draw-Line $frame (10 + $shift) (24 + $bob) (9 + $shift) (29 + $bob) $palette.outline
    Draw-Line $frame (22 + $shift) (24 + $bob) (23 + $shift) (29 + $bob) $palette.outline
    Fill-Rect $frame (11 + $shift) (24 + $bob) 11 3 $palette.deep
    Draw-Line $frame (11 + $shift) (27 + $bob) (14 + $shift) (30 + $bob) $palette.cloth
    Draw-Line $frame (21 + $shift) (27 + $bob) (18 + $shift) (30 + $bob) $palette.abyss
    Set-Pixel $frame (10 + $shift) (28 + $bob) $palette.clothHi
    Set-Pixel $frame (22 + $shift) (28 + $bob) $palette.purple

    # Armoured feet stay bottom-anchored while stride offsets sell locomotion.
    $leftX = 12 + $shift + $leftStep
    $rightX = 19 + $shift + $rightStep
    Draw-Line $frame (14 + $shift) (26 + $bob) $leftX 30 $palette.outline
    Draw-Line $frame (18 + $shift) (26 + $bob) $rightX 30 $palette.outline
    Fill-Rect $frame ($leftX - 1) 30 4 2 $palette.steelDark
    Fill-Rect $frame ($rightX - 1) 30 4 2 $palette.steel
    Set-Pixel $frame ($leftX - 1) 31 $palette.outline
    Set-Pixel $frame ($rightX + 2) 31 $palette.outline
}

function Draw-Vial([int]$frame, [int]$shift, [int]$bob, [string]$pose, [int]$pulse) {
    if ($pose -eq 'raised') { $x = 6 + $shift; $y = 9 + $bob }
    elseif ($pose -eq 'cast') { $x = 3 + $shift; $y = 8 + $bob }
    else { $x = 7 + $shift; $y = 19 + $bob }

    Draw-Line $frame (10 + $shift) (17 + $bob) ($x + 2) ($y + 2) $palette.outline
    Fill-Rect $frame ($x + 1) $y 2 2 $palette.brass
    Fill-Rect $frame $x ($y + 2) 4 5 $palette.outline
    Fill-Rect $frame ($x + 1) ($y + 3) 2 3 $palette.plague
    Set-Pixel $frame ($x + 2) ($y + 3) $palette.glow
    if ($pulse -ge 1) {
        Set-Pixel $frame ($x - 1) ($y + 3) $palette.plague
        Set-Pixel $frame ($x + 4) ($y + 1) $palette.glow
    }
    if ($pulse -ge 2) {
        Set-Pixel $frame ($x - 2) ($y + 1) $palette.glow
        Set-Pixel $frame ($x + 5) ($y + 4) $palette.plague
        Set-Pixel $frame ($x + 1) ($y - 2) $palette.glow
    }
}

function Draw-Staff([int]$frame, [int]$shift, [int]$bob, [string]$pose, [int]$pulse) {
    if ($pose -eq 'back') {
        $x0 = 23 + $shift; $y0 = 4 + $bob; $x1 = 29 + $shift; $y1 = 30
    } elseif ($pose -eq 'sweep') {
        $x0 = 3 + $shift; $y0 = 18 + $bob; $x1 = 29 + $shift; $y1 = 14 + $bob
    } elseif ($pose -eq 'raised') {
        $x0 = 26 + $shift; $y0 = 1 + $bob; $x1 = 27 + $shift; $y1 = 30
    } else {
        $x0 = 27 + $shift; $y0 = 5 + $bob; $x1 = 27 + $shift; $y1 = 30
    }

    Draw-Line $frame $x0 $y0 $x1 $y1 $palette.outline
    if ($pose -ne 'sweep') { Draw-Line $frame ($x0 - 1) ($y0 + 4) ($x1 - 1) $y1 $palette.brassDark }

    # Censer/halberd head. It remains a narrow readable anchor rather than a giant prop.
    Set-Pixel $frame $x0 ($y0 - 1) $palette.brass
    Set-Pixel $frame ($x0 - 1) $y0 $palette.ivoryHi
    Set-Pixel $frame ($x0 + 1) $y0 $palette.brass
    Set-Pixel $frame ($x0 - 2) ($y0 + 1) $palette.verdigris
    Set-Pixel $frame ($x0 + 2) ($y0 + 1) $palette.outline
    if ($pulse -gt 0) {
        Set-Pixel $frame ($x0 - 2) ($y0 - 1) $palette.glow
        Set-Pixel $frame ($x0 + 2) ($y0 - 1) $palette.plague
    }
}

function Draw-Standing([int]$frame, [int]$bob, [int]$shift,
        [int]$leftStep, [int]$rightStep, [string]$staffPose,
        [string]$vialPose, [int]$pulse) {
    Draw-Head $frame $shift $bob
    Draw-Body $frame $shift $bob $leftStep $rightStep
    Draw-Vial $frame $shift $bob $vialPose $pulse
    Draw-Staff $frame $shift $bob $staffPose $pulse
}

function Draw-Collapsed([int]$frame, [bool]$residue) {
    if (-not $residue) {
        # Hat and beak retain identity after the body hits the ground.
        Fill-Rect $frame 3 23 12 3 $palette.outline
        Fill-Rect $frame 5 23 8 1 $palette.deep
        Draw-Line $frame 1 27 11 25 $palette.outline
        Draw-Line $frame 2 27 9 26 $palette.ivory
        Fill-Rect $frame 10 24 15 6 $palette.outline
        Fill-Rect $frame 11 25 12 4 $palette.deep
        Draw-Line $frame 15 25 22 29 $palette.clothHi
        Draw-Line $frame 24 24 29 30 $palette.brassDark
        Fill-Rect $frame 24 29 6 2 $palette.steelDark
        Fill-Rect $frame 7 29 17 3 $palette.abyss
        Set-Pixel $frame 8 30 $palette.purple
        Set-Pixel $frame 25 26 $palette.verdigris
    } else {
        Fill-Rect $frame 5 29 22 3 $palette.outline
        Fill-Rect $frame 8 28 15 2 $palette.abyss
        Draw-Line $frame 2 29 10 27 $palette.ivoryDark
        Draw-Line $frame 3 29 8 28 $palette.ivoryHi
        Fill-Rect $frame 22 27 4 3 $palette.outline
        Set-Pixel $frame 23 28 $palette.plague
        Set-Pixel $frame 24 28 $palette.glow
        Set-Pixel $frame 26 30 $palette.purple
        Set-Pixel $frame 19 27 $palette.brass
    }
}

# Idle: controlled breathing and a one-pixel pulse, without floaty displacement.
Draw-Standing 0 0 0 0 0 'vertical' 'low' 0
Draw-Standing 1 -1 0 0 0 'vertical' 'low' 1
Draw-Standing 2 0 0 0 0 'vertical' 'low' 0

# Run: opposing feet, compression and a restrained lateral weight transfer.
Draw-Standing 3 0 -1 -2 1 'vertical' 'low' 0
Draw-Standing 4 1 0 -1 0 'vertical' 'low' 0
Draw-Standing 5 0 1 1 -2 'vertical' 'low' 0
Draw-Standing 6 1 0 0 -1 'vertical' 'low' 1

# Attack: staff retracts, cuts across the silhouette, then returns to guard.
Draw-Standing 7 0 0 0 0 'back' 'low' 0
Draw-Standing 8 1 -1 -1 1 'sweep' 'low' 0
Draw-Standing 9 0 0 0 0 'vertical' 'low' 0

# Cast/harvest: vial and staff form two separate luminous anchors.
Draw-Standing 10 0 0 0 0 'raised' 'raised' 1
Draw-Standing 11 -1 0 0 0 'raised' 'cast' 2
Draw-Standing 12 0 0 0 0 'raised' 'raised' 2

# Death: recoil, collapse to one knee, full fall, then mask-and-vial residue.
Draw-Standing 13 1 0 -1 0 'back' 'low' 0
Draw-Standing 14 3 -1 -2 0 'vertical' 'low' 0
Draw-Collapsed 15 $false
Draw-Collapsed 16 $true

$absolute = if ([System.IO.Path]::IsPathRooted($OutputPath)) {
    [System.IO.Path]::GetFullPath($OutputPath)
} else {
    [System.IO.Path]::GetFullPath((Join-Path (Get-Location) $OutputPath))
}
$directory = [System.IO.Path]::GetDirectoryName($absolute)
if (-not [System.IO.Directory]::Exists($directory)) {
    [System.IO.Directory]::CreateDirectory($directory) | Out-Null
}
$bitmap.Save($absolute, [System.Drawing.Imaging.ImageFormat]::Png)
$bitmap.Dispose()
Write-Output $absolute
