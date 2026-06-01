@echo off
chcp 65001 >nul
echo ========================================
echo   网络优化脚本 - 提升Cloudflare Tunnel性能
echo ========================================
echo.
echo 警告: 此脚本需要管理员权限运行
echo.

:: 检查管理员权限
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 请以管理员身份运行此脚本
    echo 右键点击 -> 以管理员身份运行
    pause
    exit /b 1
)

echo [1/6] 优化TCP参数...

:: 优化TCP全局参数
netsh int tcp set global chimney=enabled
netsh int tcp set global dca=enabled
netsh int tcp set global netdma=enabled
netsh int tcp set global ecncapability=disabled
netsh int tcp set global timestamps=disabled
netsh int tcp set global initialRto=2000
netsh int tcp set global nonsynrtretries=8

echo [2/6] 优化TCP自动调优...

:: 设置自动调优级别
netsh int tcp set global autotuninglevel=normal
netsh int tcp set global adaptiveautotuninglevel=normal

echo [3/6] 优化网络适配器...

:: 获取活动网络适配器
for /f "tokens=1" %%a in ('netsh interface show interface ^| findstr /i "已连接"') do (
    echo 优化适配器: %%a
    netsh int tcp set supplemental template=Internet congestionprovider=ctcp
    netsh int tcp set supplemental template=InternetCustom congestionprovider=ctcp
)

echo [4/6] 优化DNS缓存...

:: 优化DNS缓存
netsh int ip set dns "Ethernet" static 1.1.1.1
netsh int ip add dns "Ethernet" 1.0.0.1 index=2

:: 清除DNS缓存
ipconfig /flushdns

echo [5/6] 优化MTU...

:: 测试最佳MTU
echo 测试MTU...
ping -f -l 1472 1.1.1.1 >nul 2>&1
if %errorlevel% equ 0 (
    echo MTU 1500 可用
    set MTU=1500
) else (
    ping -f -l 1400 1.1.1.1 >nul 2>&1
    if %errorlevel% equ 0 (
        echo MTU 1400 可用
        set MTU=1400
    ) else (
        echo 使用默认MTU 1500
        set MTU=1500
    )
)

echo [6/6] 应用网络优化...

:: 重置Winsock
netsh winsock reset >nul 2>&1

:: 重置IP
netsh int ip reset >nul 2>&1

echo.
echo ========================================
echo   网络优化完成！
echo ========================================
echo.
echo 建议:
echo 1. 重启计算机以应用所有更改
echo 2. 使用以下命令测试优化效果:
echo    ping -t your-tunnel-domain.com
echo    或使用 speedtest.net 测试网速
echo.
pause
