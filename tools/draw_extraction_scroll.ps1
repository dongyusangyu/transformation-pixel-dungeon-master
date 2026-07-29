param(
	[string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot)
)

Add-Type -AssemblyName System.Drawing

$itemsPath = Join-Path $ProjectRoot "core/src/main/assets/sprites/items.png"
$exItemsPath = Join-Path $ProjectRoot "core/src/main/assets/sprites/ex_items.png"

$itemsSource = [System.Drawing.Bitmap]::FromFile($itemsPath)
$exItemsSource = [System.Drawing.Bitmap]::FromFile($exItemsPath)
$items = New-Object System.Drawing.Bitmap -ArgumentList $itemsSource
$exItems = New-Object System.Drawing.Bitmap -ArgumentList $exItemsSource
$itemsSource.Dispose()
$exItemsSource.Dispose()

if ($items.Width -ne 256 -or $items.Height -ne 512) {
	throw "items.png must be 256x512"
}
if ($exItems.Width -ne 256 -or $exItems.Height -ne 512) {
	throw "ex_items.png must be 256x512"
}

$metaX = 240
$metaY = 288
$extractX = 0
$extractY = 32

for ($y = 0; $y -lt 16; $y++) {
	for ($x = 0; $x -lt 16; $x++) {
		$exItems.SetPixel($extractX + $x, $extractY + $y,
				$items.GetPixel($metaX + $x, $metaY + $y))
	}
}

$paper = [System.Drawing.Color]::FromArgb(255, 197, 188, 159)
$dark = [System.Drawing.Color]::FromArgb(255, 92, 84, 61)
$cyan = [System.Drawing.Color]::FromArgb(255, 92, 205, 207)
$shine = [System.Drawing.Color]::FromArgb(255, 205, 255, 248)

for ($y = 4; $y -le 8; $y++) {
	for ($x = 5; $x -le 9; $x++) {
		$exItems.SetPixel($extractX + $x, $extractY + $y, $paper)
	}
}

$pixels = @(
	@(7, 4, $shine),
	@(6, 5, $cyan), @(7, 5, $cyan), @(8, 5, $cyan),
	@(7, 6, $cyan),
	@(5, 7, $dark), @(9, 7, $dark),
	@(5, 8, $dark), @(6, 8, $dark), @(7, 8, $dark), @(8, 8, $dark), @(9, 8, $dark)
)

foreach ($pixel in $pixels) {
	$exItems.SetPixel($extractX + $pixel[0], $extractY + $pixel[1], $pixel[2])
}

$items.Dispose()
$exItems.Save($exItemsPath, [System.Drawing.Imaging.ImageFormat]::Png)
$exItems.Dispose()
