@echo off
chcp 65001 >nul
echo ========================================
echo   闲置物品校园交易平台 - 优化隧道启动
echo ========================================
echo.

:: 检查cloudflared是否安装
where cloudflared >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] cloudflared 未安装或不在PATH中
    echo 请从 https://developers.cloudflare.com/cloudflare-one/connections/connect-networks/downloads/ 下载
    echo 或使用: winget install cloudflare.cloudflared
    pause
    exit /b 1
)

:: 检查Node.js
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] Node.js 未安装
    echo 请从 https://nodejs.org/ 下载安装
    pause
    exit /b 1
)

echo [信息] 检查依赖...
call npm list -g vite >nul 2>&1
if %errorlevel% neq 0 (
    echo [信息] 安装Vite...
    call npm install -g vite
)

echo [信息] 启动优化隧道...
echo.

:: 设置环境变量优化性能
set NODE_OPTIONS=--max-old-space-size=4096
set NODE_ENV=development

:: 启动优化后的脚本
node dev-tunnel-optimized.js

pause
