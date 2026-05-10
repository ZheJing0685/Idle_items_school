#!/bin/bash
# ============================================
# 数据库恢复脚本
# 用法: ./restore.sh <备份文件路径>
# ============================================

set -e

# 配置
MYSQL_HOST=${DB_HOST:-mysql}
MYSQL_USER=${DB_USER:-root}
MYSQL_PASSWORD=${DB_PASSWORD}
DATABASE=${DB_NAME:-idle_items_school}

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 显示使用帮助
show_help() {
    echo "用法: $0 <备份文件路径>"
    echo ""
    echo "示例:"
    echo "  $0 /var/backups/mysql/backup_prod_20240101_120000.sql.gz"
    echo ""
    echo "选项:"
    echo "  -h, --help    显示此帮助信息"
    echo "  -f, --force   强制恢复（跳过确认）"
    echo "  -l, --list    列出可用的备份文件"
}

# 列出可用备份文件
list_backups() {
    local backup_dir="/var/backups/mysql"
    
    log_info "可用的备份文件:"
    echo ""
    
    if [ ! -d "$backup_dir" ]; then
        log_warn "备份目录不存在: $backup_dir"
        return 1
    fi
    
    ls -lh "$backup_dir"/backup_*.sql.gz 2>/dev/null | awk '{print $NF, $5, $6, $7, $8, $9}'
    
    echo ""
    log_info "共 $(ls "$backup_dir"/backup_*.sql.gz 2>/dev/null | wc -l) 个备份文件"
}

# 检查备份文件
check_backup_file() {
    local backup_file=$1
    
    if [ ! -f "$backup_file" ]; then
        log_error "备份文件不存在: $backup_file"
        exit 1
    fi
    
    # 检查文件是否为有效的gzip文件
    if ! gzip -t "$backup_file" 2>/dev/null; then
        log_error "备份文件损坏: $backup_file"
        exit 1
    fi
    
    log_info "备份文件验证通过"
}

# 备份当前数据库
backup_current_database() {
    local backup_file="/var/backups/mysql/pre_restore_$(date +%Y%m%d_%H%M%S).sql.gz"
    
    log_info "备份当前数据库到: $backup_file"
    
    mysqldump -h "$MYSQL_HOST" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" \
        --single-transaction \
        --routines \
        --triggers \
        --events \
        "$DATABASE" | gzip > "$backup_file"
    
    if [ $? -eq 0 ]; then
        log_info "当前数据库备份完成"
    else
        log_warn "备份当前数据库失败，继续恢复操作"
    fi
}

# 恢复数据库
restore_database() {
    local backup_file=$1
    
    log_info "开始恢复数据库: $DATABASE"
    log_info "从备份文件: $backup_file"
    
    # 解压并恢复
    gunzip -c "$backup_file" | mysql -h "$MYSQL_HOST" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" "$DATABASE"
    
    if [ $? -eq 0 ]; then
        log_info "数据库恢复成功"
    else
        log_error "数据库恢复失败"
        exit 1
    fi
}

# 验证恢复结果
verify_restore() {
    log_info "验证恢复结果..."
    
    # 检查表数量
    local table_count=$(mysql -h "$MYSQL_HOST" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" \
        -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$DATABASE'" 2>/dev/null | tail -1)
    
    log_info "恢复后的表数量: $table_count"
}

# 用户确认
confirm_restore() {
    local backup_file=$1
    local force=$2
    
    if [ "$force" = "true" ]; then
        return 0
    fi
    
    echo ""
    log_warn "即将恢复数据库: $DATABASE"
    log_warn "备份文件: $backup_file"
    echo ""
    read -p "此操作将覆盖当前数据库，是否继续？(y/N): " -n 1 -r
    echo ""
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "操作已取消"
        exit 0
    fi
}

# 主函数
main() {
    local backup_file=""
    local force="false"
    local list_only="false"
    
    # 解析参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -f|--force)
                force="true"
                shift
                ;;
            -l|--list)
                list_only="true"
                shift
                ;;
            *)
                backup_file=$1
                shift
                ;;
        esac
    done
    
    # 列出备份文件
    if [ "$list_only" = "true" ]; then
        list_backups
        exit 0
    fi
    
    # 检查是否提供了备份文件
    if [ -z "$backup_file" ]; then
        log_error "请指定备份文件路径"
        echo ""
        show_help
        exit 1
    fi
    
    log_info "========== 数据库恢复开始 =========="
    log_info "环境: ${ENV:-unknown}"
    log_info "数据库: $DATABASE"
    log_info "备份文件: $backup_file"
    log_info "时间: $(date)"
    
    # 执行恢复流程
    check_backup_file "$backup_file"
    confirm_restore "$backup_file" "$force"
    backup_current_database
    restore_database "$backup_file"
    verify_restore
    
    log_info "========== 数据库恢复完成 =========="
}

# 执行主函数
main "$@"
