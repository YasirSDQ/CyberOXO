param(
    [int]$IntervalSeconds = 60,
    [string]$RepoUrl = "https://github.com/YasirSDQ/CyberOXO.git",
    [string]$Branch = "main"
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " Starting Auto-Sync for CyberOXO" -ForegroundColor Green
Write-Host " Syncing every $IntervalSeconds seconds." -ForegroundColor Yellow
Write-Host " Repository: $RepoUrl" -ForegroundColor Yellow
Write-Host " Branch: $Branch" -ForegroundColor Yellow
Write-Host " Keep this window open to continue syncing." -ForegroundColor Yellow
Write-Host " Press Ctrl+C to stop." -ForegroundColor Red
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Ensure git is initialized and remote is set
if (-not (Test-Path ".git")) {
    Write-Host "Git repository not found. Initializing..." -ForegroundColor Yellow
    git init
    git remote add origin $RepoUrl
} else {
    $currentRemote = git remote get-url origin 2>$null
    if ($currentRemote -ne $RepoUrl) {
        Write-Host "Updating remote origin URL to $RepoUrl..." -ForegroundColor Yellow
        if ($currentRemote) {
            git remote set-url origin $RepoUrl
        } else {
            git remote add origin $RepoUrl
        }
    }
}

while ($true) {
    $timestamp = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    Write-Host "[$timestamp] Checking for updates..." -ForegroundColor Gray
    
    # Fetch and pull changes from GitHub (auto update local)
    $pullOutput = git pull origin $Branch --rebase 2>&1
    
    # Check if there are local changes
    $status = git status --porcelain
    if ($status) {
        Write-Host "[$timestamp] Local changes detected. Syncing to GitHub..." -ForegroundColor Yellow
        git add .
        git commit -m "Auto sync from local on $timestamp"
        $pushOutput = git push origin $Branch 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "[$timestamp] Successfully synced to GitHub." -ForegroundColor Green
        } else {
            Write-Host "[$timestamp] Error pushing to GitHub." -ForegroundColor Red
            Write-Host $pushOutput -ForegroundColor Red
        }
    }
    
    Start-Sleep -Seconds $IntervalSeconds
}
