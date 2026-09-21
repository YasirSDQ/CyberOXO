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
    git branch -M $Branch 2>$null
    git remote add origin $RepoUrl
}
else {
    $currentRemote = git remote get-url origin 2>$null
    if ($currentRemote -ne $RepoUrl) {
        Write-Host "Updating remote origin URL to $RepoUrl..." -ForegroundColor Yellow
        if ($currentRemote) {
            git remote set-url origin $RepoUrl
        }
        else {
            git remote add origin $RepoUrl
        }
    }
}

# Configure git to always merge on pull to avoid divergent branches error in newer git versions
git config pull.rebase false 2>$null


while ($true) {
    $timestamp = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    Write-Host "[$timestamp] Checking for updates..." -ForegroundColor Gray
    
    # 1. First, check if there are local changes and commit them
    $status = git status --porcelain
    if ($status) {
        Write-Host "[$timestamp] Local changes detected. Committing..." -ForegroundColor Yellow
        git add .
        git commit -m "Auto sync from local on $timestamp" | Out-Null
    }

    # 2. Fetch and pull changes from GitHub (auto update local)
    # Using --no-edit so git doesn't prompt for a merge commit message
    # Using --allow-unrelated-histories in case the repo was initialized locally and remotely independently
    $pullOutput = git pull origin $Branch --no-edit --allow-unrelated-histories 2>&1
    
    if ($LASTEXITCODE -ne 0) {
        if ($pullOutput -match "conflict" -or $pullOutput -match "Automatic merge failed") {
            Write-Host "[$timestamp] MERGE CONFLICT DETECTED!" -ForegroundColor Red
            Write-Host "Your friend edited the exact same lines as you." -ForegroundColor Red
            Write-Host "Aborting auto-merge to protect your code. You must fix this manually by typing 'git pull' in your terminal." -ForegroundColor Yellow
            git merge --abort
        }
        else {
            Write-Host "[$timestamp] Warning: Pull had an issue (maybe no remote branch yet)." -ForegroundColor Yellow
            Write-Host $pullOutput -ForegroundColor DarkGray
        }
    }
    
    # 3. Push to GitHub
    # This ensures both local commits and merge commits are pushed to the remote
    $pushOutput = git push origin $Branch 2>&1
    if ($LASTEXITCODE -eq 0) {
        if ($pushOutput -match "Everything up-to-date") {
            # Optionally suppress "Everything up-to-date" to reduce console spam
            # Write-Host "[$timestamp] Everything up-to-date." -ForegroundColor Gray
        } else {
            Write-Host "[$timestamp] Successfully synced to GitHub." -ForegroundColor Green
        }
    }
    else {
        Write-Host "[$timestamp] Error pushing to GitHub." -ForegroundColor Red
        Write-Host $pushOutput -ForegroundColor Red
    }
    
    Start-Sleep -Seconds $IntervalSeconds
}
