#!/bin/bash
# ============================================
# 数据库备份脚本
# 用法: ./backup.sh [环境: dev|staging|prod]
# ============================================

set -e

# 配置
BACKUP_DIR="/var/backups/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
MYSQL_HOST=${DB_HOST:-mysql}
MYSQL_USER=${DB_USER:-idle_items_backup}
MYSQL_PASSWORD=${DB_PASSWORD}
DATABASE=${DB_NAME:-idle_items_school}
ENV=${1:-dev}

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

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

# 检查依赖
check_dependencies() {
    if ! command -v mysqldump &> /dev/null; then
        log_error "mysqldump 未安装"
        exit 1
    fi
    
    if ! command -v gzip &> /dev/null; then
        log_error "gzip 未安装"
        exit 1
    fi
}

# 创建备份目录
create_backup_dir() {
    mkdir -p "$BACKUP_DIR"
    if [ $? -ne 0 ]; then
        log_error "无法创建备份目录: $BACKUP_DIR"
        exit 1
    fi
}

# 执行备份
perform_backup() {
    local backup_file="$BACKUP_DIR/backup_${ENV}_${DATE}.sql.gz"
    
    log_info "开始备份数据库: $DATABASE"
    log_info "备份文件: $backup_file"
    
    # 使用mysqldump备份
    mysqldump -h "$MYSQL_HOST" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" \
        --single-transaction \
        --routines \
        --triggers \
        --events \
        --set-gtid-purged=OFF \
        "$DATABASE" | gzip > "$backup_file"
    
    if [ $? -ne 0 ]; then
        log_error "数据库备份失败"
        rm -f "$backup_file"
        exit 1
    fi
    
    # 验证备份文件
    local file_size=$(stat -f%z "$backup_file" 2>/dev/null || stat -c%s "$backup_file" 2>/dev/null)
    if [ "$file_size" -lt 100 ]; then
        log_error "备份文件异常，可能为空"
        rm -f "$backup_file"
        exit 1
    fi
    
    log_info "备份成功完成"
    log_info "文件大小: $((file_size / 1024 / 1024))MB"
}

# 清理旧备份
cleanup_old_backups() {
    local days_to_keep=7
    
    case "$ENV" in
        dev)
            days_to_keep=2
            ;;
        staging)
            days_to_keep=7
            ;;
        prod)
            days_to_keep=30
            ;;
    esac
    
    log_info "清理${days_to_keep}天前的备份文件..."
    
    find "$BACKUP_DIR" -name "backup_${ENV}_*.sql.gz" -mtime +$days_to_keep -delete 2>/dev/null
    
    log_info "清理完成"
}

# 记录日志
log_backup() {
    local log_file="$BACKUP_DIR/backup.log"
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $ENV - $DATE - 成功" >> "$log_file"
}

# 显示备份统计
show_stats() {
    log_info "当前备份文件列表:"
    ls -lh "$BACKUP_DIR"/backup_${ENV}_*.sql.gz 2>/dev/null | tail -5
    
    log_info "备份目录总大小:"
    du -sh "$BACKUP_DIR" 2>/dev/null || echo "无法获取目录大小"
}

# 主函数
main() {
    log_info "========== 数据库备份开始 =========="
    log_info "环境: $ENV"
    log_info "数据库: $DATABASE"
    log_info "时间: $(date)"
    
    check_dependencies
    create_backup_dir
    perform_backup
    cleanup_old_backups
    log_backup
    show_stats
    
    log_info "========== 数据库备份完成 =========="
}

# 执行主函数
main
