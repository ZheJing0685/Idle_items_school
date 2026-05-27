package com.idleitems.school.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "系统指标响应")
public class SystemMetricsResponse {
    @Schema(description = "堆内存信息")
    private MemoryInfo heapMemory;
    @Schema(description = "非堆内存信息")
    private MemoryInfo nonHeapMemory;
    @Schema(description = "线程信息")
    private ThreadInfo threads;
    @Schema(description = "类加载信息")
    private ClassLoadingInfo classLoading;
    @Schema(description = "运行时信息")
    private RuntimeInfo runtime;
    @Schema(description = "数据库信息")
    private DatabaseInfo database;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "内存信息")
    public static class MemoryInfo {
        @Schema(description = "已使用内存（字节）")
        private Long used;
        @Schema(description = "已提交内存（字节）")
        private Long committed;
        @Schema(description = "最大内存（字节）")
        private Long max;
        @Schema(description = "使用百分比")
        private Double usagePercent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "线程信息")
    public static class ThreadInfo {
        @Schema(description = "当前线程数")
        private Integer count;
        @Schema(description = "守护线程数")
        private Integer daemonCount;
        @Schema(description = "峰值线程数")
        private Integer peakCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "类加载信息")
    public static class ClassLoadingInfo {
        @Schema(description = "已加载类数")
        private Integer loadedClassCount;
        @Schema(description = "总加载类数")
        private Long totalLoadedClassCount;
        @Schema(description = "已卸载类数")
        private Long unloadedClassCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "运行时信息")
    public static class RuntimeInfo {
        @Schema(description = "运行时间（毫秒）")
        private Long uptime;
        @Schema(description = "启动时间戳")
        private Long startTime;
        @Schema(description = "Java版本")
        private String javaVersion;
        @Schema(description = "Java供应商")
        private String javaVendor;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "数据库信息")
    public static class DatabaseInfo {
        @Schema(description = "活跃连接数")
        private Integer activeConnections;
        @Schema(description = "空闲连接数")
        private Integer idleConnections;
        @Schema(description = "总连接数")
        private Integer totalConnections;
        @Schema(description = "最大连接数")
        private Long maxConnections;
    }
}