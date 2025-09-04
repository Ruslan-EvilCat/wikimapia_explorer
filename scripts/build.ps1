$ErrorActionPreference = 'Stop'

$javaHome = 'C:\\Program Files\\Android\\Android Studio\\jbr'
if (-not (Test-Path "$javaHome\\bin\\java.exe")) {
  Write-Error "Java not found at $javaHome. Please update scripts/build.ps1 with your JDK path."
}

$env:JAVA_HOME = $javaHome
$env:Path = "$env:JAVA_HOME\\bin;$env:Path"

& "$env:JAVA_HOME\\bin\\java.exe" -version
& "${PSScriptRoot}\\..\\gradlew.bat" --version
& "${PSScriptRoot}\\..\\gradlew.bat" clean assembleDebug

