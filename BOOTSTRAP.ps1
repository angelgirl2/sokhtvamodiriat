$ErrorActionPreference = 'Stop'
Write-Host 'Checking Flutter...' -ForegroundColor Cyan
flutter --version
Write-Host 'Fetching dependencies...' -ForegroundColor Cyan
flutter pub get
Write-Host 'Project ready. Run: flutter run' -ForegroundColor Green
