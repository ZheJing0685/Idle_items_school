/**
 * 统一日志模块
 * 提供结构化日志记录，支持生产环境静默控制
 */

type LogLevel = 'log' | 'warn' | 'error';

interface LogEntry {
  level: LogLevel;
  message: string;
  args: unknown[];
  timestamp: string;
}

const isProduction = import.meta.env.PROD;

function createLogEntry(level: LogLevel, message: string, args: unknown[]): LogEntry {
  return {
    level,
    message,
    args,
    timestamp: new Date().toISOString(),
  };
}

function formatMessage(entry: LogEntry): string {
  const prefix = `[${entry.timestamp}] [${entry.level.toUpperCase()}]`;
  return `${prefix} ${entry.message}`;
}

export const logger = {
  log(message: string, ...args: unknown[]): void {
    if (isProduction) return;
    const entry = createLogEntry('log', message, args);
    console.log(formatMessage(entry), ...args);
  },

  warn(message: string, ...args: unknown[]): void {
    const entry = createLogEntry('warn', message, args);
    console.warn(formatMessage(entry), ...args);
  },

  error(message: string, ...args: unknown[]): void {
    const entry = createLogEntry('error', message, args);
    console.error(formatMessage(entry), ...args);
  },
};
