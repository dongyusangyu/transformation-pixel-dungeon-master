param(
    [string]$RepositoryRoot = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

$spriteRoot = Join-Path $RepositoryRoot 'core\src\main\assets\sprites'

function New-MappedSheet {
    param(
        [string]$SourceName,
        [string]$TargetName,
        [int]$FrameWidth,
        [int]$FrameHeight,
        [int]$FrameCount,
        [scriptblock]$MapColor,
        [System.Drawing.Color]$DetailColor,
        [int]$DetailX,
        [int]$DetailY
    )

    $sourcePath = Join-Path $spriteRoot $SourceName
    $targetPath = Join-Path $spriteRoot $TargetName
    $source = [System.Drawing.Bitmap]::FromFile($sourcePath)
    try {
        if ($source.Width -lt $FrameWidth * $FrameCount -or $source.Height -lt $FrameHeight) {
            throw "$SourceName does not contain the required frame row"
        }

        $target = New-Object System.Drawing.Bitmap(
            ($FrameWidth * $FrameCount),
            $FrameHeight,
            [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        try {
            for ($y = 0; $y -lt $FrameHeight; $y++) {
                for ($x = 0; $x -lt $FrameWidth * $FrameCount; $x++) {
                    $sourceColor = $source.GetPixel($x, $y)
                    if ($sourceColor.A -ge 128) {
                        $target.SetPixel($x, $y, (& $MapColor $sourceColor))
                    } else {
                        $target.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(0, 0, 0, 0))
                    }
                }
            }

            # Add one restrained soul-core pixel per frame without changing silhouettes.
            for ($frame = 0; $frame -lt $FrameCount; $frame++) {
                $bestX = -1
                $bestY = -1
                $bestDistance = [int]::MaxValue
                for ($y = 0; $y -lt $FrameHeight; $y++) {
                    for ($localX = 0; $localX -lt $FrameWidth; $localX++) {
                        $x = $frame * $FrameWidth + $localX
                        if ($target.GetPixel($x, $y).A -eq 255) {
                            $distance = [Math]::Abs($localX - $DetailX) + [Math]::Abs($y - $DetailY)
                            if ($distance -lt $bestDistance) {
                                $bestDistance = $distance
                                $bestX = $x
                                $bestY = $y
                            }
                        }
                    }
                }
                if ($bestX -lt 0) {
                    throw "$TargetName frame $frame is empty"
                }
                $target.SetPixel($bestX, $bestY, $DetailColor)
            }

            $target.Save($targetPath, [System.Drawing.Imaging.ImageFormat]::Png)
        } finally {
            $target.Dispose()
        }
    } finally {
        $source.Dispose()
    }
}

$collectorMap = {
    param([System.Drawing.Color]$color)
    $value = $color.R
    if ($value -le 0)   { return [System.Drawing.Color]::FromArgb(255, 9, 7, 18) }
    if ($value -le 23)  { return [System.Drawing.Color]::FromArgb(255, 19, 17, 38) }
    if ($value -le 32)  { return [System.Drawing.Color]::FromArgb(255, 28, 24, 51) }
    if ($value -le 53)  { return [System.Drawing.Color]::FromArgb(255, 43, 37, 74) }
    if ($value -le 71)  { return [System.Drawing.Color]::FromArgb(255, 41, 71, 82) }
    if ($value -le 81)  { return [System.Drawing.Color]::FromArgb(255, 49, 92, 99) }
    if ($value -le 104) { return [System.Drawing.Color]::FromArgb(255, 74, 141, 137) }
    if ($value -le 119) { return [System.Drawing.Color]::FromArgb(255, 91, 190, 171) }
    return [System.Drawing.Color]::FromArgb(255, 218, 255, 239)
}

$wraithMap = {
    param([System.Drawing.Color]$color)
    if ($color.R -eq 0 -and $color.G -eq 0 -and $color.B -eq 0) {
        return [System.Drawing.Color]::FromArgb(255, 13, 8, 27)
    }
    if ($color.R -lt 200) {
        return [System.Drawing.Color]::FromArgb(255, 57, 31, 86)
    }
    return [System.Drawing.Color]::FromArgb(255, 197, 255, 244)
}

New-MappedSheet -SourceName 'necromancer.png' -TargetName 'soul_collector.png' `
    -FrameWidth 16 -FrameHeight 16 -FrameCount 13 -MapColor $collectorMap `
    -DetailColor ([System.Drawing.Color]::FromArgb(255, 61, 238, 207)) -DetailX 8 -DetailY 9

New-MappedSheet -SourceName 'wraith.png' -TargetName 'powerful_wraith.png' `
    -FrameWidth 14 -FrameHeight 15 -FrameCount 8 -MapColor $wraithMap `
    -DetailColor ([System.Drawing.Color]::FromArgb(255, 79, 244, 224)) -DetailX 7 -DetailY 7

Write-Output 'Generated soul_collector.png and powerful_wraith.png'
