@echo off
REM 测试运行脚本 (Windows)
REM 使用方法: scripts\run-tests.bat [选项]

setlocal enabledelayedexpansion

REM 打印帮助信息
:show_help
echo 用法: scripts\run-tests.bat [选项]
echo.
echo 选项:
echo   all          运行所有测试
echo   frontend     运行前端测试
echo   backend      运行后端测试
echo   unit         运行单元测试
echo   integration  运行集成测试
echo   e2e          运行E2E测试
echo   coverage     运行测试并生成覆盖率报告
echo   help         显示帮助信息
echo.
echo 示例:
echo   scripts\run-tests.bat all
echo   scripts\run-tests.bat frontend
echo   scripts\run-tests.bat backend
goto :eof

REM 运行前端单元测试
:run_frontend_unit
echo 运行前端单元测试...
cd frontend
call npm run test:unit
cd ..
echo 前端单元测试完成
goto :eof

REM 运行前端E2E测试
:run_frontend_e2e
echo 运行前端E2E测试...
cd frontend
call npm run test:e2e
cd ..
echo 前端E2E测试完成
goto :eof

REM 运行前端测试（带覆盖率）
:run_frontend_coverage
echo 运行前端测试（带覆盖率）...
cd frontend
call npm run test:coverage
cd ..
echo 前端测试完成，覆盖率报告已生成
goto :eof

REM 运行后端单元测试
:run_backend_unit
echo 运行后端单元测试...
cd backend
call mvn test -Dtest="!*IntegrationTest"
cd ..
echo 后端单元测试完成
goto :eof

REM 运行后端集成测试
:run_backend_integration
echo 运行后端集成测试...
cd backend
call mvn test -Dtest="*IntegrationTest"
cd ..
echo 后端集成测试完成
goto :eof

REM 运行后端测试（带覆盖率）
:run_backend_coverage
echo 运行后端测试（带覆盖率）...
cd backend
call mvn test jacoco:report
cd ..
echo 后端测试完成，覆盖率报告已生成
goto :eof

REM 运行所有前端测试
:run_frontend_all
echo 运行所有前端测试...
cd frontend
call npm run test:all
cd ..
echo 所有前端测试完成
goto :eof

REM 运行所有后端测试
:run_backend_all
echo 运行所有后端测试...
cd backend
call mvn test
cd ..
echo 所有后端测试完成
goto :eof

REM 运行所有测试
:run_all
echo 运行所有测试...
echo.
call :run_frontend_all
echo.
call :run_backend_all
echo.
echo 所有测试完成
goto :eof

REM 运行单元测试
:run_unit
echo 运行所有单元测试...
echo.
call :run_frontend_unit
echo.
call :run_backend_unit
echo.
echo 所有单元测试完成
goto :eof

REM 运行集成测试
:run_integration
echo 运行所有集成测试...
echo.
call :run_backend_integration
echo.
echo 所有集成测试完成
goto :eof

REM 运行E2E测试
:run_e2e
echo 运行所有E2E测试...
echo.
call :run_frontend_e2e
echo.
echo 所有E2E测试完成
goto :eof

REM 运行测试并生成覆盖率报告
:run_coverage
echo 运行测试并生成覆盖率报告...
echo.
call :run_frontend_coverage
echo.
call :run_backend_coverage
echo.
echo 所有测试完成，覆盖率报告已生成
echo 前端覆盖率报告: frontend\coverage\index.html
echo 后端覆盖率报告: backend\target\site\jacoco\index.html
goto :eof

REM 主函数
if "%1"=="" goto show_help
if "%1"=="all" goto run_all
if "%1"=="frontend" goto run_frontend_all
if "%1"=="backend" goto run_backend_all
if "%1"=="unit" goto run_unit
if "%1"=="integration" goto run_integration
if "%1"=="e2e" goto run_e2e
if "%1"=="coverage" goto run_coverage
if "%1"=="help" goto show_help
if "%1"=="--help" goto show_help
if "%1"=="-h" goto show_help

echo 未知选项: %1
echo.
goto show_help
