const fs = require('fs');

let sql = fs.readFileSync('D:\\Users\\ZheJing\\Desktop\\idle_items_school.sql', 'utf8');

// Remove MySQL-specific lines
sql = sql.replace(/SET NAMES utf8mb4;\n/g, '');
sql = sql.replace(/SET FOREIGN_KEY_CHECKS = 0;\n/g, '');
sql = sql.replace(/SET FOREIGN_KEY_CHECKS = 1;\n/g, '');

// Replace AUTO_INCREMENT with SERIAL
sql = sql.replace(/AUTO_INCREMENT = \d+/g, '');

// Replace ENGINE=InnoDB and charset definitions
sql = sql.replace(/ ENGINE = InnoDB[^;]*;$/gm, ';');
sql = sql.replace(/ ROW_FORMAT = Dynamic;/g, ';');
sql = sql.replace(/ ROW_FORMAT = DYNAMIC;/g, ';');

// Replace COMMENT in CREATE TABLE
sql = sql.replace(/ COMMENT '[^']*'/g, '');

// Replace ENGINE definition at end of CREATE TABLE
sql = sql.replace(/ ENGINE = InnoDB[^;]*;/g, ';');

// Remove USING BTREE from indexes
sql = sql.replace(/ USING BTREE/g, '');

// Replace COLUMN syntax: MODIFY COLUMN → ALTER COLUMN ... TYPE
// We skip these as they're MySQL-specific ALTER TABLE statements
sql = sql.replace(/ALTER TABLE.*MODIFY COLUMN.*;/g, '-- ALTER skipped');

// Replace data types
sql = sql.replace(/\bbigint\b/g, 'BIGINT');
sql = sql.replace(/\bint\b/g, 'INTEGER');
sql = sql.replace(/\btinyint\(1\)/g, 'BOOLEAN');
sql = sql.replace(/\btinyint\b/g, 'SMALLINT');
sql = sql.replace(/\btext\b/g, 'TEXT');
sql = sql.replace(/\bdatetime\(6\)/g, 'TIMESTAMP(6)');
sql = sql.replace(/\bdatetime\b/g, 'TIMESTAMP');
sql = sql.replace(/\bdate\b/g, 'DATE');
sql = sql.replace(/\bdecimal\((\d+),\s*(\d+)\)/g, 'NUMERIC($1,$2)');

// Replace ENUM with VARCHAR
sql = sql.replace(/ENUM\([^)]+\)/g, 'VARCHAR(50)');

// Replace CHARACTER SET and COLLATE
sql = sql.replace(/ CHARACTER SET utf8mb4/g, '');
sql = sql.replace(/ CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci/g, '');
sql = sql.replace(/ COLLATE utf8mb4_unicode_ci/g, '');
sql = sql.replace(/ COLLATE utf8mb4_0900_ai_ci/g, '');
sql = sql.replace(/ COLLATE utf8mb4/g, '');

// Replace DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
sql = sql.replace(/ DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP/g, '');

// Fix TRUE/FALSE for booleans
sql = sql.replace(/\b0\b(?!0)/g, 'FALSE');
sql = sql.replace(/\b1\b(?!0)/g, 'TRUE');

// Replace backticks with double quotes
sql = sql.replace(/`/g, '"');

// Remove table options after closing paren
sql = sql.replace(/\);/g, ');');

// Replace CONSTRAINT syntax - remove ON DELETE/UPDATE for PostgreSQL
sql = sql.replace(/ ON DELETE CASCADE ON UPDATE RESTRICT/g, '');
sql = sql.replace(/ ON DELETE SET NULL ON UPDATE RESTRICT/g, '');
sql = sql.replace(/ ON DELETE RESTRICT ON UPDATE RESTRICT/g, '');

// Fix boolean values in INSERT statements
sql = sql.replace(/, 0,/g, ', FALSE,');
sql = sql.replace(/, 1,/g, ', TRUE,');

// Fix boolean values at end of INSERT
sql = sql.replace(/, 0\);/g, ', FALSE);');
sql = sql.replace(/, 1\);/g, ', TRUE);');

// Remove CHECK constraints (PostgreSQL handles differently)
sql = sql.replace(/,\s*CHECK \(\([^)]+\)\)/g, '');

fs.writeFileSync('D:\\Users\\ZheJing\\Desktop\\idle_items_school_postgres.sql', sql, 'utf8');
console.log('Conversion complete!');
