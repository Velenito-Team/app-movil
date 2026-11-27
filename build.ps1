param([string[]]$Tasks = @('testDebugUnitTest', 'lintDebug', 'assembleDebug'))
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    # Keeps Gradle's worker classpath outside accented Windows user-profile paths.
    & "$PSScriptRoot\gradlew.bat" -g "$PSScriptRoot\.tools\gradle-home" @Tasks --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'La compilación o las pruebas fallaron.' }
    if (($Tasks -contains 'assembleDebug') -and (Test-Path -LiteralPath "$PSScriptRoot\app\build\outputs\apk\debug\app-debug.apk")) {
        New-Item -ItemType Directory -Path "$PSScriptRoot\dist" -Force | Out-Null
        Copy-Item -LiteralPath "$PSScriptRoot\app\build\outputs\apk\debug\app-debug.apk" -Destination "$PSScriptRoot\dist\velenito-comprobantes-debug.apk" -Force
    }
} finally { Pop-Location }
