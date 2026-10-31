$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
if (-not (Get-Command java -ErrorAction SilentlyContinue)) { throw 'Install Java JDK 17 and add java to PATH first.' }
$toolsDir = Join-Path $PSScriptRoot '.tools'
New-Item -ItemType Directory -Force -Path $toolsDir | Out-Null
$gradleZip = Join-Path $toolsDir 'gradle-8.11.1-bin.zip'
$gradleHome = Join-Path $toolsDir 'gradle-8.11.1'
if (-not (Test-Path $gradleHome)) {
  Write-Host 'Downloading official Gradle 8.11.1...'
  Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-8.11.1-bin.zip' -OutFile $gradleZip
  $expectedHash = ((Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-8.11.1-bin.zip.sha256').Content).Trim()
  if ((Get-FileHash $gradleZip -Algorithm SHA256).Hash.ToLowerInvariant() -ne $expectedHash.ToLowerInvariant()) { Remove-Item $gradleZip; throw 'Gradle checksum mismatch.' }
  Expand-Archive $gradleZip -DestinationPath $toolsDir -Force
}
# Android Studio's default SDK location. A custom SDK can be set with ANDROID_HOME.
if (-not $env:ANDROID_HOME) { $env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
if (-not (Test-Path $env:ANDROID_HOME)) { throw 'Install Android Studio and Android SDK 35 first, or set ANDROID_HOME to the SDK folder.' }
$gradleBat = Join-Path $gradleHome 'bin\gradle.bat'
# Generate official wrapper files so Android Studio can import the project normally.
& $gradleBat wrapper --gradle-version 8.11.1 --distribution-type bin
if ($LASTEXITCODE -ne 0) { throw 'Gradle wrapper setup failed. See errors above.' }
& $gradleBat --no-daemon :app:assembleDebug :app:lintDebug
if ($LASTEXITCODE -ne 0) { throw 'Build failed. See errors above.' }
Copy-Item 'app\build\outputs\apk\debug\app-debug.apk' 'Desert-Ledger.apk' -Force
Write-Host 'Ready: Desert-Ledger.apk' -ForegroundColor Green
