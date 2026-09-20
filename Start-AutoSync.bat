@echo off
title AutoSync
echo Starting AutoSync...
PowerShell -NoProfile -ExecutionPolicy Bypass -Command "& '%~dp0autosync.ps1'"
pause
