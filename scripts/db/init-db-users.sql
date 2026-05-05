-- ============================================================
-- Database User Initialization Script
-- Usage: mysql -u root -p < init-db-users.sql
-- Apply minimum privilege principle: each account only gets
-- the permissions it needs
-- ============================================================

SET @db_name = 'idle_items_school';

SELECT '=== Creating Database Users ===' AS '';

-- ============================================================
-- 1. Application User (DML only)
-- Used by Spring Boot application at runtime
-- ============================================================
CREATE USER IF NOT EXISTS 'idle_items_app'@'%'
    IDENTIFIED BY '${APP_DB_PASSWORD}'
    PASSWORD EXPIRE INTERVAL 90 DAY;

GRANT SELECT, INSERT, UPDATE, DELETE ON `idle_items_school`.* TO 'idle_items_app'@'%';
GRANT EXECUTE ON `idle_items_school`.* TO 'idle_items_app'@'%';

SELECT 'Created application user: idle_items_app' AS '';

-- ============================================================
-- 2. Flyway Migration User (DDL + DML)
-- Used only during schema migrations, with elevated privileges
-- ============================================================
CREATE USER IF NOT EXISTS 'idle_items_flyway'@'%'
    IDENTIFIED BY '${FLYWAY_DB_PASSWORD}'
    PASSWORD EXPIRE INTERVAL 90 DAY;

GRANT ALL PRIVILEGES ON `idle_items_school`.* TO 'idle_items_flyway'@'%';

SELECT 'Created flyway user: idle_items_flyway' AS '';

-- ============================================================
-- 3. Backup User (Read-only + Admin operations)
-- Used by backup scripts
-- ============================================================
CREATE USER IF NOT EXISTS 'idle_items_backup'@'%'
    IDENTIFIED BY '${BACKUP_DB_PASSWORD}'
    PASSWORD EXPIRE INTERVAL 90 DAY;

GRANT SELECT, SHOW VIEW, TRIGGER, EVENT, LOCK TABLES ON `idle_items_school`.* TO 'idle_items_backup'@'%';
GRANT PROCESS ON *.* TO 'idle_items_backup'@'%';

SELECT 'Created backup user: idle_items_backup' AS '';

-- ============================================================
-- 4. Monitor User (Read-only)
-- Used for monitoring and diagnostics
-- ============================================================
CREATE USER IF NOT EXISTS 'idle_items_monitor'@'%'
    IDENTIFIED BY '${MONITOR_DB_PASSWORD}'
    PASSWORD EXPIRE INTERVAL 90 DAY;

GRANT SELECT ON `idle_items_school`.* TO 'idle_items_monitor'@'%';
GRANT PROCESS, SHOW DATABASES ON *.* TO 'idle_items_monitor'@'%';

SELECT 'Created monitor user: idle_items_monitor' AS '';

-- ============================================================
-- Apply changes and verify
-- ============================================================
FLUSH PRIVILEGES;

SELECT '=== User Privileges Verification ===' AS '';
SELECT
    user AS 'User',
    host AS 'Host',
    Repl_slave_priv AS 'SuperUser?'
FROM mysql.user
WHERE user LIKE 'idle_items_%';

SELECT '=== Grant Verification ===' AS '';
SHOW GRANTS FOR 'idle_items_app'@'%';
SHOW GRANTS FOR 'idle_items_flyway'@'%';
SHOW GRANTS FOR 'idle_items_backup'@'%';
SHOW GRANTS FOR 'idle_items_monitor'@'%';

-- ============================================================
-- Cleanup: Remove old root-only application user if exists
-- WARNING: Only uncomment after verifying new users work
-- ============================================================
-- DROP USER IF EXISTS 'old_app_user'@'%';

SELECT '=== Database User Initialization Complete ===' AS '';
