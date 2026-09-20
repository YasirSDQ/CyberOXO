$repoPath = $PSScriptRoot
Set-Location -Path $repoPath

Write-Host "AutoSync started in $repoPath" -ForegroundColor Green
Write-Host "Checking for changes every 60 seconds. Press Ctrl+C to stop." -ForegroundColor Yellow

while ($true) {
    # Check if there are changes
    $status = git status --porcelain
    if ($status) {
        Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Changes detected. Syncing..." -ForegroundColor Cyan
        git add .
        $date = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        git commit -m "Auto sync: $date"
        git push origin main
        Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Sync complete." -ForegroundColor Green
    } else {
        Write-Host "[$(Get-Date -Format 'HH:mm:ss')] No changes detected." -ForegroundColor DarkGray
    }
    
    # Wait for 60 seconds
    Start-Sleep -Seconds 60
}
