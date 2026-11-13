param(
    [string]$SourceIcon = "D:\Couple_fit\CoupleFit\icon.png",
    [string]$OutputDir = "D:\Couple_fit\CoupleFit\app\src\main\res",
    [string]$WebOutputDir = "D:\Couple_fit\CoupleFit\app\src\main"
)

Add-Type -AssemblyName System.Drawing

if (-not (Test-Path $SourceIcon)) {
    Write-Error "Source icon not found at: $SourceIcon"
    exit 1
}

$srcBmp = [System.Drawing.Bitmap]::FromFile($SourceIcon)
Write-Output "Source icon loaded: $($srcBmp.Width)x$($srcBmp.Height)"

# Android Density Scales
$densities = @(
    @{ Name = "mipmap-mdpi";    Scale = 1.0; LegacySize = 48;  AdaptiveSize = 108 },
    @{ Name = "mipmap-hdpi";    Scale = 1.5; LegacySize = 72;  AdaptiveSize = 162 },
    @{ Name = "mipmap-xhdpi";   Scale = 2.0; LegacySize = 96;  AdaptiveSize = 216 },
    @{ Name = "mipmap-xxhdpi";  Scale = 3.0; LegacySize = 144; AdaptiveSize = 324 },
    @{ Name = "mipmap-xxxhdpi"; Scale = 4.0; LegacySize = 192; AdaptiveSize = 432 }
)

# Helper function to resample with highest bicubic quality
function New-ResampledBitmap($src, $w, $h) {
    $bmp = New-Object System.Drawing.Bitmap($w, $h, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $g.DrawImage($src, 0, 0, $w, $h)
    $g.Dispose()
    return $bmp
}

# 1. Generate Legacy Full Icon (Square / Rounded)
function Generate-LegacyIcon($size, $isRound, $outPath) {
    $bmp = New-Object System.Drawing.Bitmap($size, $size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

    if ($isRound) {
        $path = New-Object System.Drawing.Drawing2D.GraphicsPath
        $path.AddEllipse(0, 0, $size, $size)
        $g.SetClip($path)
        $path.Dispose()
    }

    $g.DrawImage($srcBmp, 0, 0, $size, $size)
    $g.Dispose()

    $bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
}

# 2. Generate Adaptive Background Layer
function Generate-AdaptiveBackground($size, $outPath) {
    $bmp = New-Object System.Drawing.Bitmap($size, $size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality

    # Top dark indigo (#191654) fading down to pure obsidian (#000000)
    $rect = New-Object System.Drawing.Rectangle(0, 0, $size, $size)
    $brush = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
        $rect,
        [System.Drawing.Color]::FromArgb(25, 23, 75),
        [System.Drawing.Color]::FromArgb(0, 0, 0),
        90.0
    )
    $g.FillRectangle($brush, $rect)
    $brush.Dispose()
    $g.Dispose()

    $bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
}

# 3. Generate Adaptive Foreground Layer
function Generate-AdaptiveForeground($canvasSize, $outPath) {
    # Scale icon to 92% and center it within 108dp canvas
    $scale = 0.92
    $targetSize = [int]($canvasSize * $scale)
    $offset = [int](($canvasSize - $targetSize) / 2)

    $scaled = New-ResampledBitmap $srcBmp $targetSize $targetSize

    $fgBmp = New-Object System.Drawing.Bitmap($canvasSize, $canvasSize, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)

    for ($y = 0; $y -lt $targetSize; $y++) {
        for ($x = 0; $x -lt $targetSize; $x++) {
            $p = $scaled.GetPixel($x, $y)
            $distX = [Math]::Min($x, $targetSize - 1 - $x) / ($targetSize * 0.08)
            $distY = [Math]::Min($y, $targetSize - 1 - $y) / ($targetSize * 0.08)
            $edgeFactor = [Math]::Max(0.0, [Math]::Min(1.0, [Math]::Min($distX, $distY)))

            $alpha = [int](255 * $edgeFactor)
            $fgBmp.SetPixel($offset + $x, $offset + $y, [System.Drawing.Color]::FromArgb($alpha, $p.R, $p.G, $p.B))
        }
    }
    $scaled.Dispose()

    $fgBmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $fgBmp.Dispose()
}

# Process each density bucket
foreach ($d in $densities) {
    $dir = Join-Path $OutputDir $d.Name
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
    }

    Write-Output "Generating assets for $($d.Name)..."
    
    # Legacy Square & Round
    Generate-LegacyIcon $d.LegacySize $false (Join-Path $dir "ic_launcher.png")
    Generate-LegacyIcon $d.LegacySize $true  (Join-Path $dir "ic_launcher_round.png")

    # Adaptive Background & Foreground
    Generate-AdaptiveBackground $d.AdaptiveSize (Join-Path $dir "ic_launcher_background.png")
    Generate-AdaptiveForeground $d.AdaptiveSize (Join-Path $dir "ic_launcher_foreground.png")
}

# 4. Generate 512x512 Google Play Store graphic
$webPath = Join-Path $WebOutputDir "ic_launcher-web.png"
Write-Output "Generating 512x512 Play Store graphic at $webPath..."
$webBmp = New-ResampledBitmap $srcBmp 512 512
$webBmp.Save($webPath, [System.Drawing.Imaging.ImageFormat]::Png)
$webBmp.Dispose()

$srcBmp.Dispose()
Write-Output "All icon assets successfully generated!"
