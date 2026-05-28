#!/bin/bash

# 测试运行脚本
# 使用方法: ./scripts/run-tests.sh [选项]

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印帮助信息
show_help() {
    echo "用法: ./scripts/run-tests.sh [选项]"
    echo ""
    echo "选项:"
    echo "  all          运行所有测试"
    echo "  frontend     运行前端测试"
    echo "  backend      运行后端测试"
    echo "  unit         运行单元测试"
    echo "  integration  运行集成测试"
    echo "  e2e          运行E2E测试"
    echo "  coverage     运行测试并生成覆盖率报告"
    echo "  help         显示帮助信息"
    echo ""
    echo "示例:"
    echo "  ./scripts/run-tests.sh all"
    echo "  ./scripts/run-tests.sh frontend"
    echo "  ./scripts/run-tests.sh backend"
}

# 运行前端单元测试
run_frontend_unit() {
    echo -e "${BLUE}运行前端单元测试...${NC}"
    cd frontend
    npm run test:unit
    cd ..
    echo -e "${GREEN}前端单元测试完成${NC}"
}

# 运行前端E2E测试
run_frontend_e2e() {
    echo -e "${BLUE}运行前端E2E测试...${NC}"
    cd frontend
    npm run test:e2e
    cd ..
    echo -e "${GREEN}前端E2E测试完成${NC}"
}

# 运行前端测试（带覆盖率）
run_frontend_coverage() {
    echo -e "${BLUE}运行前端测试（带覆盖率）...${NC}"
    cd frontend
    npm run test:coverage
    cd ..
    echo -e "${GREEN}前端测试完成，覆盖率报告已生成${NC}"
}

# 运行后端单元测试
run_backend_unit() {
    echo -e "${BLUE}运行后端单元测试...${NC}"
    cd backend
    mvn test -Dtest="!*IntegrationTest"
    cd ..
    echo -e "${GREEN}后端单元测试完成${NC}"
}

# 运行后端集成测试
run_backend_integration() {
    echo -e "${BLUE}运行后端集成测试...${NC}"
    cd backend
    mvn test -Dtest="*IntegrationTest"
    cd ..
    echo -e "${GREEN}后端集成测试完成${NC}"
}

# 运行后端测试（带覆盖率）
run_backend_coverage() {
    echo -e "${BLUE}运行后端测试（带覆盖率）...${NC}"
    cd backend
    mvn test jacoco:report
    cd ..
    echo -e "${GREEN}后端测试完成，覆盖率报告已生成${NC}"
}

# 运行所有前端测试
run_frontend_all() {
    echo -e "${BLUE}运行所有前端测试...${NC}"
    cd frontend
    npm run test:all
    cd ..
    echo -e "${GREEN}所有前端测试完成${NC}"
}

# 运行所有后端测试
run_backend_all() {
    echo -e "${BLUE}运行所有后端测试...${NC}"
    cd backend
    mvn test
    cd ..
    echo -e "${GREEN}所有后端测试完成${NC}"
}

# 运行所有测试
run_all() {
    echo -e "${YELLOW}运行所有测试...${NC}"
    echo ""
    
    run_frontend_all
    echo ""
    
    run_backend_all
    echo ""
    
    echo -e "${GREEN}所有测试完成${NC}"
}

# 运行单元测试
run_unit() {
    echo -e "${YELLOW}运行所有单元测试...${NC}"
    echo ""
    
    run_frontend_unit
    echo ""
    
    run_backend_unit
    echo ""
    
    echo -e "${GREEN}所有单元测试完成${NC}"
}

# 运行集成测试
run_integration() {
    echo -e "${YELLOW}运行所有集成测试...${NC}"
    echo ""
    
    run_backend_integration
    echo ""
    
    echo -e "${GREEN}所有集成测试完成${NC}"
}

# 运行E2E测试
run_e2e() {
    echo -e "${YELLOW}运行所有E2E测试...${NC}"
    echo ""
    
    run_frontend_e2e
    echo ""
    
    echo -e "${GREEN}所有E2E测试完成${NC}"
}

# 运行测试并生成覆盖率报告
run_coverage() {
    echo -e "${YELLOW}运行测试并生成覆盖率报告...${NC}"
    echo ""
    
    run_frontend_coverage
    echo ""
    
    run_backend_coverage
    echo ""
    
    echo -e "${GREEN}所有测试完成，覆盖率报告已生成${NC}"
    echo -e "${BLUE}前端覆盖率报告: frontend/coverage/index.html${NC}"
    echo -e "${BLUE}后端覆盖率报告: backend/target/site/jacoco/index.html${NC}"
}

# 主函数
main() {
    case "$1" in
        all)
            run_all
            ;;
        frontend)
            run_frontend_all
            ;;
        backend)
            run_backend_all
            ;;
        unit)
            run_unit
            ;;
        integration)
            run_integration
            ;;
        e2e)
            run_e2e
            ;;
        coverage)
            run_coverage
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            echo -e "${RED}未知选项: $1${NC}"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"
