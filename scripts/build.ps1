$ErrorActionPreference = 'Stop'
$localJdk = Get-ChildItem "$PSScriptRoot\..\.tools\jdk25" -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
$localMaven = "$PSScriptRoot\..\.tools\apache-maven-3.9.9\bin\mvn.cmd"
if ($localJdk) { $env:JAVA_HOME = $localJdk.FullName; $env:Path = "$($localJdk.FullName)\bin;$env:Path" }
if (Test-Path $localMaven) { & $localMaven clean package } else { & mvn clean package }
