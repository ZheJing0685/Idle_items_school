# Database Scripts

## Directory Structure

```
scripts/
├── backup/
│   ├── backup.sh       # Automated backup script
│   └── restore.sh      # Database restore script
└── db/
    ├── data-integrity-check.sql  # Data integrity verification
    └── init-db-users.sql         # Database user initialization
```

## Quick Start

### 1. Initialize Database Users
```bash
mysql -u root -p < scripts/db/init-db-users.sql
```

### 2. Run Backup
```bash
# Dev environment (daily, 2-day retention)
DB_BACKUP_PASSWORD=<pwd> bash scripts/backup/backup.sh dev

# Production (daily/weekly/monthly rotation)
DB_BACKUP_PASSWORD=<pwd> bash scripts/backup/backup.sh prod
```

### 3. Restore from Backup
```bash
DB_FLYWAY_PASSWORD=<pwd> bash scripts/backup/restore.sh /path/to/backup.sql.gz
```

### 4. Data Integrity Check
```bash
mysql -u idle_items_monitor -p idle_items_school < scripts/db/data-integrity-check.sql
```

## Environment Variables

| Variable | Purpose |
|----------|---------|
| `APP_DB_PASSWORD` | Application DML user password |
| `FLYWAY_DB_PASSWORD` | Flyway migration user password |
| `BACKUP_DB_PASSWORD` | Backup user password |
| `MONITOR_DB_PASSWORD` | Monitor user password |
| `DB_BACKUP_PASSWORD` | Backup script auth |
| `DB_FLYWAY_PASSWORD` | Restore script auth |
