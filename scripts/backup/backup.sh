#!/bin/bash
set -euo pipefail

BACKUP_DIR="/var/backups/idle_items_school"
DB_NAME="idle_items_school"
DB_USER="idle_items_backup"
DB_PASS="${DB_BACKUP_PASSWORD:?DB_BACKUP_PASSWORD not set}"
DB_HOST="localhost"
RETENTION_DAYS=7
RETENTION_WEEKS=4
RETENTION_MONTHS=3
LOG_FILE="/var/log/db-backup.log"
ENV="${1:-dev}"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" | tee -a "$LOG_FILE"
}

check_disk() {
    local available=$(df --output=avail "$BACKUP_DIR" | tail -1)
    local min_space=$((10 * 1024 * 1024))
    if [ "$available" -lt "$min_space" ]; then
        log "ERROR: Insufficient disk space ($((available / 1024)) MB available, ${min_space} MB required)"
        exit 1
    fi
}

backup() {
    local label="$1"
    local filename="${DB_NAME}_${label}_$(date +%Y%m%d_%H%M%S).sql.gz"
    local filepath="${BACKUP_DIR}/${label}/${filename}"

    mkdir -p "${BACKUP_DIR}/${label}"

    log "Starting ${label} backup: ${filename}"

    mysqldump \
        --host="${DB_HOST}" \
        --user="${DB_USER}" \
        --password="${DB_PASS}" \
        --single-transaction \
        --routines \
        --triggers \
        --events \
        --databases "${DB_NAME}" \
        | gzip > "${filepath}"

    if [ $? -eq 0 ] && gzip -t "${filepath}" 2>/dev/null; then
        local size=$(du -h "${filepath}" | cut -f1)
        log "Backup completed: ${filename} (${size})"
    else
        log "ERROR: Backup failed or corrupted: ${filename}"
        rm -f "${filepath}"
        exit 1
    fi
}

cleanup() {
    local label="$1"
    local retention="$2"
    log "Cleaning up ${label} backups (retention: ${retention} days)"
    find "${BACKUP_DIR}/${label}" -name "${DB_NAME}_${label}_*.sql.gz" -type f -mtime "+${retention}" -delete
}

main() {
    log "=== Backup started (environment: ${ENV}) ==="
    check_disk

    case "${ENV}" in
        prod|staging)
            local day_of_week=$(date +%u)
            local day_of_month=$(date +%d)
            if [ "${day_of_month}" = "01" ]; then
                backup "monthly"
                cleanup "monthly" $((RETENTION_MONTHS * 30))
            elif [ "${day_of_week}" = "7" ]; then
                backup "weekly"
                cleanup "weekly" $((RETENTION_WEEKS * 7))
            fi
            backup "daily"
            cleanup "daily" "${RETENTION_DAYS}"
            ;;
        dev)
            backup "daily"
            cleanup "daily" "2"
            ;;
        *)
            echo "Usage: $0 {dev|staging|prod}"
            exit 1
            ;;
    esac

    log "=== Backup completed ==="
}

main "$@"
