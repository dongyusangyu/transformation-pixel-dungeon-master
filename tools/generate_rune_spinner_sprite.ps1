param(
	[string]$Source = "core/src/main/assets/sprites/spinner.png",
	[string]$Output = "core/src/main/assets/sprites/rune_spinner.png"
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

$sourcePath = (Resolve-Path $Source).Path
$outputPath = [System.IO.Path]::GetFullPath((Join-Path (Get-Location) $Output))
$sourceImage = [System.Drawing.Bitmap]::new($sourcePath)
$result = [System.Drawing.Bitmap]::new($sourceImage.Width, $sourceImage.Height,
	[System.Drawing.Imaging.PixelFormat]::Format32bppArgb)

$palette = @{
	"8DDB9C" = [System.Drawing.Color]::FromArgb(255, 122, 85, 204)
	"598B61" = [System.Drawing.Color]::FromArgb(255, 46, 36, 85)
	"7EB48D" = [System.Drawing.Color]::FromArgb(255, 86, 64, 144)
	"33563E" = [System.Drawing.Color]::FromArgb(255, 23, 19, 41)
	"518F52" = [System.Drawing.Color]::FromArgb(255, 56, 41, 105)
	"476F54" = [System.Drawing.Color]::FromArgb(255, 37, 29, 66)
	"B6C39D" = [System.Drawing.Color]::FromArgb(255, 143, 167, 217)
	"6AA66B" = [System.Drawing.Color]::FromArgb(255, 73, 55, 127)
	"427249" = [System.Drawing.Color]::FromArgb(255, 32, 26, 56)
	"D9D1AE" = [System.Drawing.Color]::FromArgb(255, 174, 235, 255)
	"90AE8B" = [System.Drawing.Color]::FromArgb(255, 111, 137, 184)
	"000000" = [System.Drawing.Color]::FromArgb(255, 0, 0, 0)
	"E1F8D7" = [System.Drawing.Color]::FromArgb(255, 216, 251, 255)
	"CFE5D7" = [System.Drawing.Color]::FromArgb(255, 181, 248, 255)
}

try {
	for ($y = 0; $y -lt $sourceImage.Height; $y++) {
		for ($x = 0; $x -lt $sourceImage.Width; $x++) {
			$pixel = $sourceImage.GetPixel($x, $y)
			if ($pixel.A -eq 0) {
				$result.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(0, 0, 0, 0))
				continue
			}
			$key = "{0:X2}{1:X2}{2:X2}" -f $pixel.R, $pixel.G, $pixel.B
			$mapped = $palette[$key]
			if ($null -eq $mapped) {
				$mapped = [System.Drawing.Color]::FromArgb(255, $pixel.R, $pixel.G, $pixel.B)
			}
			$result.SetPixel($x, $y, $mapped)
		}
	}

	$runeBright = [System.Drawing.Color]::FromArgb(255, 185, 248, 255)
	$runeMid = [System.Drawing.Color]::FromArgb(255, 95, 209, 235)
	for ($frame = 0; $frame -lt 10; $frame++) {
		$origin = $frame * 16
		foreach ($mark in @(@(7, 5, $runeMid), @(8, 6, $runeBright), @(7, 7, $runeMid))) {
			$x = $origin + [int]$mark[0]
			$y = [int]$mark[1]
			if ($sourceImage.GetPixel($x, $y).A -gt 0) {
				$result.SetPixel($x, $y, [System.Drawing.Color]$mark[2])
			}
		}
	}

	[System.IO.Directory]::CreateDirectory([System.IO.Path]::GetDirectoryName($outputPath)) | Out-Null
	$result.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
} finally {
	$result.Dispose()
	$sourceImage.Dispose()
}

Write-Output "Generated $outputPath"
