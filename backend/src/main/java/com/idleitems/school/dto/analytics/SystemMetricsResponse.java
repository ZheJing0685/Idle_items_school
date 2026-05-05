package com.idleitems.school.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMetricsResponse {
    private MemoryInfo heapMemory;
    private MemoryInfo nonHeapMemory;
    private ThreadInfo threads;
    private ClassLoadingInfo classLoading;
    private RuntimeInfo runtime;
    private DatabaseInfo database;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemoryInfo {
        private Long used;
        private Long committed;
        private Long max;
        private Double usagePercent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThreadInfo {
        private Integer count;
        private Integer daemonCount;
        private Integer peakCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassLoadingInfo {
        private Integer loadedClassCount;
        private Long totalLoadedClassCount;
        private Long unloadedClassCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuntimeInfo {
        private Long uptime;
        private Long startTime;
        private String javaVersion;
        private String javaVendor;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatabaseInfo {
        private Integer activeConnections;
        private Integer idleConnections;
        private Integer totalConnections;
        private Long maxConnections;
    }
}