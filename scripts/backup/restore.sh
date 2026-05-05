#!/bin/bash
set -euo pipefail

BACKUP_FILE="${1:-}"
DB_NAME="idle_items_school"
DB_USER="idle_items_flyway"
DB_PASS="${DB_FLYWAY_PASSWORD:?DB_FLYWAY_PASSWORD not set}"
DB_HOST="localhost"
LOG_FILE="/var/log/db-restore.log"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" | tee -a "$LOG_FILE"
}

usage() {
    echo "Usage: $0 <backup-file.sql.gz>"
    echo ""
    echo "This script will:"
    echo "  1. Backup current Flyway migration history"
    echo "  2. Drop all tables in ${DB_NAME}"
    echo "  3. Restore from the specified backup file"
    echo "  4. Verify restore integrity"
    echo ""
    exit 1
}

confirm() {
    echo ""
    echo "WARNING: This will DROP ALL TABLES in '${DB_NAME}' and restore from:"
    echo "  ${BACKUP_FILE}"
    echo ""
    read -p "Type 'yes' to continue: " confirmation
    if [ "${confirmation}" != "yes" ]; then
        log "Restore cancelled by user"
        exit 0
    fi
}

verify_backup() {
    if [ ! -f "${BACKUP_FILE}" ]; then
        log "ERROR: Backup file not found: ${BACKUP_FILE}"
        exit 1
    fi
    if ! gzip -t "${BACKUP_FILE}" 2>/dev/null; then
        log "ERROR: Backup file is corrupted: ${BACKUP_FILE}"
        exit 1
    fi
}

backup_flyway_history() {
    local history_file="/tmp/flyway_history_${DB_NAME}_$(date +%Y%m%d_%H%M%S).sql"
    log "Backing up Flyway migration history to ${history_file}"
    mysqldump \
        --host="${DB_HOST}" \
        --user="${DB_USER}" \
        --password="${DB_PASS}" \
        --single-transaction \
        "${DB_NAME}" \
        flyway_schema_history \
        > "${history_file}"
    log "Flyway history backed up to ${history_file}"

    local latest_version=$(mysql \
        --host="${DB_HOST}" \
        --user="${DB_USER}" \
        --password="${DB_PASS}" \
        --batch \
        --skip-column-names \
        "${DB_NAME}" \
        -e "SELECT MAX(version) FROM flyway_schema_history" 2>/dev/null || echo "unknown")
    log "Current Flyway version before restore: ${latest_version}"
}

drop_tables() {
    log "Dropping all tables in ${DB_NAME}..."
    mysql \
        --host="${DB_HOST}" \
        --user="${DB_USER}" \
        --password="${DB_PASS}" \
        --execute="SET FOREIGN_KEY_CHECKS = 0;
            SELECT CONCAT('DROP TABLE IF EXISTS ', GROUP_CONCAT(table_name), ';')
            INTO @drop_stmt
            FROM information_schema.tables
            WHERE table_schema = '${DB_NAME}';
            PREPARE stmt FROM @drop_stmt;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
            SET FOREIGN_KEY_CHECKS = 1;" \
        "${DB_NAME}"
    log "All tables dropped"
}

restore() {
    log "Starting restore from ${BACKUP_FILE}..."
    gunzip -c "${BACKUP_FILE}" | mysql \
        --host="${DB_HOST}" \
        --user="${DB_USER}" \
        --password="${DB_PASS}" \
        "${DB_NAME}"
    log "Restore completed"
}

verify_restore() {
    log "Verifying restore integrity..."
    mysql \
        --host="${DB_HOST}" \
        --user="${DB_USER}" \
        --password="${DB_PASS}" \
        --batch \
        --skip-column-names \
        "${DB_NAME}" \
        -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${DB_NAME}'" \
        > /tmp/table_count_$$.txt
    local table_count=$(cat /tmp/table_count_$$.txt)
    log "Tables restored: ${table_count}"

    local flyway_entries=$(mysql \
        --host="${DB_HOST}" \
        --user="${DB_USER}" \
        --password="${DB_PASS}" \
        --batch \
        --skip-column-names \
        "${DB_NAME}" \
        -e "SELECT COUNT(*) FROM flyway_schema_history" 2>/dev/null || echo "0")
    log "Flyway migration entries: ${flyway_entries}"

    if [ "${table_count}" -gt 0 ]; then
        log "Restore verification PASSED (${table_count} tables restored)"
    else
        log "ERROR: Restore verification FAILED - no tables found"
        exit 1
    fi

    rm -f /tmp/table_count_$$.txt
}

main() {
    if [ -z "${BACKUP_FILE}" ]; then
        usage
    fi

    log "=== Restore started ==="
    verify_backup
    confirm
    backup_flyway_history
    drop_tables
    restore
    verify_restore

    echo ""
    echo "IMPORTANT: After restore, update Flyway baseline if needed:"
    echo "  mvn flyway:baseline -Dflyway.baselineVersion=<version>"
    echo ""
    log "=== Restore completed ==="
}

main "$@"
