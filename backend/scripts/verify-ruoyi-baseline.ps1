$ErrorActionPreference = "Stop"

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")

$requiredPaths = @(
    "backend/pom.xml",
    "backend/mvnw.cmd",
    "backend/ruoyi-admin",
    "backend/ruoyi-system",
    "backend/ruoyi-framework",
    "backend/TEMPLATE_SOURCE.md",
    "ruoyi-ui/package.json"
)

$missing = @()
foreach ($relativePath in $requiredPaths) {
    $path = Join-Path $projectRoot $relativePath
    if (-not (Test-Path -LiteralPath $path)) {
        $missing += $relativePath
    }
}

if ($missing.Count -gt 0) {
    Write-Error ("Missing required Ruoyi baseline paths: " + ($missing -join ", "))
}

Write-Output "Ruoyi baseline structure verified."
