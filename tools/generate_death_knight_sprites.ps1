param(
    [string]$BossPath = "core/src/main/assets/sprites/death_knight.png",
    [string]$SlashPath = "core/src/main/assets/effects/death_knight_slash.png"
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

$S = 32
$facing = 'right-three-quarter'
$legWidth = 6
$bootHeight = 2
$boss = [System.Drawing.Bitmap]::new(30 * $S, $S, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$slash = [System.Drawing.Bitmap]::new(4 * $S, $S, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$p = @{
    outline=[System.Drawing.Color]::FromArgb(255,15,13,20)
    black=[System.Drawing.Color]::FromArgb(255,25,24,31)
    cloth=[System.Drawing.Color]::FromArgb(255,40,39,49)
    clothHi=[System.Drawing.Color]::FromArgb(255,63,60,70)
    steel=[System.Drawing.Color]::FromArgb(255,83,82,88)
    steelHi=[System.Drawing.Color]::FromArgb(255,130,126,122)
    ivoryDark=[System.Drawing.Color]::FromArgb(255,139,130,111)
    ivory=[System.Drawing.Color]::FromArgb(255,205,196,168)
    ivoryHi=[System.Drawing.Color]::FromArgb(255,245,235,202)
    bloodDark=[System.Drawing.Color]::FromArgb(255,74,19,27)
    blood=[System.Drawing.Color]::FromArgb(255,133,31,38)
    red=[System.Drawing.Color]::FromArgb(255,231,57,48)
    white=[System.Drawing.Color]::FromArgb(255,255,244,220)
    ash=[System.Drawing.Color]::FromArgb(255,102,91,88)
}

function Px($bmp,[int]$frame,[int]$x,[int]$y,$c) {
    if ($x -ge 0 -and $x -lt $S -and $y -ge 0 -and $y -lt $S) { $bmp.SetPixel($frame*$S+$x,$y,$c) }
}
function Rect($bmp,[int]$f,[int]$x,[int]$y,[int]$w,[int]$h,$c) {
    for ($yy=$y;$yy -lt $y+$h;$yy++){ for($xx=$x;$xx -lt $x+$w;$xx++){ Px $bmp $f $xx $yy $c } }
}
function Line($bmp,[int]$f,[int]$x0,[int]$y0,[int]$x1,[int]$y1,$c) {
    $dx=[Math]::Abs($x1-$x0); $sx=if($x0-lt$x1){1}else{-1}; $dy=-[Math]::Abs($y1-$y0); $sy=if($y0-lt$y1){1}else{-1}; $e=$dx+$dy
    while($true){ Px $bmp $f $x0 $y0 $c; if($x0-eq$x1-and$y0-eq$y1){break}; $e2=2*$e; if($e2-ge$dy){$e+=$dy;$x0+=$sx}; if($e2-le$dx){$e+=$dx;$y0+=$sy} }
}
function Knight([int]$f,[int]$sx,[int]$bob,[int]$left,[int]$right,[string]$pose,[int]$energy) {
    # Torn horsehair plume: the major red silhouette anchor.
    Line $boss $f (15+$sx) (4+$bob) (8+$sx) (2+$bob) $p.outline
    Line $boss $f (14+$sx) (5+$bob) (5+$sx) (5+$bob) $p.outline
    Line $boss $f (13+$sx) (6+$bob) (6+$sx) (8+$bob) $p.outline
    Line $boss $f (14+$sx) (4+$bob) (8+$sx) (3+$bob) $p.blood
    Line $boss $f (13+$sx) (5+$bob) (7+$sx) (6+$bob) $p.bloodDark
    Px $boss $f (5+$sx) (5+$bob) $p.blood

    # Mouthless ivory death mask and red eye slit.
    # The narrow rear edge and projecting right cheek/nose make a persistent 3/4 profile.
    Rect $boss $f (15+$sx) (5+$bob) 7 9 $p.outline
    Rect $boss $f (16+$sx) (5+$bob) 5 7 $p.ivoryDark
    Rect $boss $f (17+$sx) (5+$bob) 4 6 $p.ivory
    Px $boss $f (18+$sx) (5+$bob) $p.ivoryHi
    Px $boss $f (21+$sx) (7+$bob) $p.ivoryDark
    Px $boss $f (22+$sx) (8+$bob) $p.outline
    Px $boss $f (21+$sx) (8+$bob) $p.red
    Line $boss $f (16+$sx) (8+$bob) (20+$sx) (8+$bob) $p.red
    Px $boss $f (20+$sx) (12+$bob) $p.ivoryDark

    # Lamellar shoulders, torso and asymmetric torn mantle.
    Rect $boss $f (10+$sx) (14+$bob) 15 4 $p.outline
    Rect $boss $f (11+$sx) (14+$bob) 4 3 $p.steel
    Rect $boss $f (19+$sx) (13+$bob) 5 4 $p.steelHi
    Px $boss $f (24+$sx) (15+$bob) $p.outline
    Rect $boss $f (12+$sx) (17+$bob) 12 9 $p.outline
    Rect $boss $f (13+$sx) (17+$bob) 10 8 $p.cloth
    for($y=18+$bob;$y-le24+$bob;$y+=2){ Line $boss $f (13+$sx) $y (22+$sx) $y $p.steel }
    Line $boss $f (18+$sx) (17+$bob) (18+$sx) (25+$bob) $p.steelHi
    Line $boss $f (10+$sx) (18+$bob) (6+$sx) (29+$bob) $p.outline
    Line $boss $f (9+$sx) (19+$bob) (5+$sx) (27+$bob) $p.black
    Line $boss $f (22+$sx) (19+$bob) (26+$sx) (29+$bob) $p.outline
    Line $boss $f (23+$sx) (20+$bob) (27+$sx) (27+$bob) $p.cloth
    Px $boss $f (7+$sx) (26+$bob) $p.clothHi
    Px $boss $f (25+$sx) (25+$bob) $p.bloodDark

    # Thick compact hero-like legs. The right/front leg carries the pose;
    # boots occupy only the final two rows instead of reading as tall greaves.
    $lx=13+$sx+$left; $rx=20+$sx+$right
    Rect $boss $f ($lx-2) (24+$bob) $legWidth (6-$bob) $p.outline
    Rect $boss $f ($lx-1) (25+$bob) ($legWidth-2) (5-$bob) $p.steel
    Rect $boss $f ($rx-2) (23+$bob) $legWidth (7-$bob) $p.outline
    Rect $boss $f ($rx-1) (24+$bob) ($legWidth-2) (6-$bob) $p.steelHi
    Px $boss $f ($lx-1) (26+$bob) $p.clothHi
    Px $boss $f ($rx+1) (25+$bob) $p.ivoryDark
    Rect $boss $f ($lx-2) (32-$bootHeight) $legWidth $bootHeight $p.outline
    Rect $boss $f ($rx-2) (32-$bootHeight) $legWidth $bootHeight $p.outline
    Line $boss $f ($lx-1) 30 ($lx+2) 30 $p.steel
    Line $boss $f ($rx-1) 30 ($rx+2) 30 $p.steelHi

    # Ivory saber poses retain a large readable diagonal.
    if($pose-eq'guard'){$x0=11+$sx;$y0=20+$bob;$x1=28+$sx;$y1=28+$bob}
    elseif($pose-eq'back'){$x0=10+$sx;$y0=18+$bob;$x1=24+$sx;$y1=3+$bob}
    elseif($pose-eq'sweep'){$x0=6+$sx;$y0=23+$bob;$x1=30+$sx;$y1=15+$bob}
    elseif($pose-eq'thrust'){$x0=13+$sx;$y0=20+$bob;$x1=31;$y1=19+$bob}
    elseif($pose-eq'raised'){$x0=12+$sx;$y0=18+$bob;$x1=17+$sx;$y1=1+$bob}
    else{$x0=12+$sx;$y0=21+$bob;$x1=25+$sx;$y1=27+$bob}
    Line $boss $f $x0 $y0 $x1 $y1 $p.outline
    Line $boss $f $x0 ($y0-1) $x1 ($y1-1) $p.ivory
    Px $boss $f $x1 ($y1-1) $p.ivoryHi
    if($energy-gt0){ Line $boss $f $x0 ($y0-2) $x1 ($y1-2) $p.blood; Px $boss $f $x1 ($y1-2) $p.red }
    if($energy-gt1){ Px $boss $f (4+$sx) (22+$bob) $p.red; Px $boss $f (27+$sx) (13+$bob) $p.red; Px $boss $f (9+$sx) (11+$bob) $p.blood }
}
function Fallen([int]$f,[int]$stage){
    if($stage-lt4){
        Rect $boss $f (7-$stage) (20+$stage*2) (18+$stage*2) (10-$stage) $p.outline
        Rect $boss $f (9-$stage) (21+$stage*2) 13 (7-$stage) $p.cloth
        Rect $boss $f (18+$stage) (19+$stage*2) 7 6 $p.ivoryDark
        Line $boss $f 4 (29-$stage) 28 (24+$stage) $p.ivory
        Line $boss $f 4 (30-$stage) 28 (25+$stage) $p.outline
        Line $boss $f 8 (18+$stage*2) 2 (21+$stage*2) $p.blood
    } else {
        Rect $boss $f 5 29 22 3 $p.outline; Rect $boss $f 8 28 14 2 $p.cloth
        Line $boss $f 2 29 13 26 $p.ivory; Line $boss $f 3 30 14 27 $p.outline
        Rect $boss $f 22 27 6 3 $p.ivoryDark; Px $boss $f 24 28 $p.red
    }
}

# idle 0..3
Knight 0 0 0 0 0 'guard' 0; Knight 1 0 -1 0 0 'guard' 0; Knight 2 0 0 0 0 'guard' 1; Knight 3 0 -1 0 0 'guard' 0
# run 4..9
Knight 4 -1 0 -2 1 'guard' 0; Knight 5 0 1 -1 0 'guard' 0; Knight 6 1 0 1 -2 'guard' 0; Knight 7 0 1 0 -1 'guard' 0; Knight 8 -1 0 -2 1 'guard' 0; Knight 9 0 1 1 -2 'guard' 0
# melee 10..14
Knight 10 0 0 0 0 'back' 0; Knight 11 -1 1 -1 1 'raised' 1; Knight 12 0 0 -1 1 'sweep' 2; Knight 13 1 0 0 -1 'thrust' 1; Knight 14 0 0 0 0 'guard' 0
# charge 15..18
Knight 15 0 0 0 0 'raised' 1; Knight 16 0 -1 0 0 'raised' 2; Knight 17 0 0 0 0 'raised' 2; Knight 18 0 -1 0 0 'raised' 2
# leap 19..23
Knight 19 -2 1 -2 1 'back' 1; Knight 20 -1 0 -2 0 'raised' 2; Knight 21 1 -1 -1 1 'sweep' 2; Knight 22 2 0 1 -2 'thrust' 2; Knight 23 0 1 0 0 'guard' 1
# death 24..29
Knight 24 0 1 -1 0 'back' 0; Knight 25 -1 3 -2 0 'guard' 0; Fallen 26 0; Fallen 27 1; Fallen 28 3; Fallen 29 4

# Four-frame white/red crescent, intentionally sparse and hard edged.
for($f=0;$f-lt4;$f++){
    $radius=12-$f*2
    for($y=4+$f;$y-le27-$f;$y++){
        $dy=$y-16; $x=[int][Math]::Round(16+[Math]::Sqrt([Math]::Max(0,$radius*$radius-$dy*$dy)))
        if($x-ge16-and$x-lt31){ Px $slash $f $x $y $p.red; if($x-1-ge0){Px $slash $f ($x-1) $y $p.white}; if($f-lt2-and$x+1-lt32){Px $slash $f ($x+1) $y $p.blood} }
    }
    Line $slash $f (4+$f*2) (25-$f) (20+$f) (7+$f) $p.ivory
    Line $slash $f (5+$f*2) (26-$f) (21+$f) (8+$f) $p.blood
}

foreach($target in @(@($boss,$BossPath),@($slash,$SlashPath))){
    $bmp=$target[0]; $path=[System.IO.Path]::GetFullPath((Join-Path (Get-Location) $target[1])); $dir=[System.IO.Path]::GetDirectoryName($path)
    if(-not [System.IO.Directory]::Exists($dir)){[System.IO.Directory]::CreateDirectory($dir)|Out-Null}
    $bmp.Save($path,[System.Drawing.Imaging.ImageFormat]::Png); $bmp.Dispose(); Write-Output $path
}
