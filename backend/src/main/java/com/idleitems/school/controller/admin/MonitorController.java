package com.idleitems.school.controller.admin;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.common.Result;
import com.idleitems.school.dto.analytics.SystemMetricsResponse;
import com.idleitems.school.entity.User;
import com.zaxxer.hikari.HikariDataSource;
import com.idleitems.school.config.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;

@RestController
@RequestMapping(ApiPaths.Admin.MONITOR)
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN})
@Tag(name = "管理员-系统监控", description = "管理员系统监控相关接口")
public class MonitorController {

    private final MeterRegistry meterRegistry;
    private final HikariDataSource dataSource;

    @GetMapping("/metrics")
    @Operation(summary = "获取系统指标", description = "获取系统内存、线程、类加载、运行时和数据库连接池等指标")
    public Result<SystemMetricsResponse> getSystemMetrics() {
        // 获取内存信息
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        SystemMetricsResponse.MemoryInfo heapMemory = getMemoryInfo(memoryMXBean.getHeapMemoryUsage());
        SystemMetricsResponse.MemoryInfo nonHeapMemory = getMemoryInfo(memoryMXBean.getNonHeapMemoryUsage());

        // 获取线程信息
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        SystemMetricsResponse.ThreadInfo threads = SystemMetricsResponse.ThreadInfo.builder()
                .count(threadMXBean.getThreadCount())
                .daemonCount(threadMXBean.getDaemonThreadCount())
                .peakCount(threadMXBean.getPeakThreadCount())
                .build();

        // 获取类加载信息
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        SystemMetricsResponse.ClassLoadingInfo classLoading = SystemMetricsResponse.ClassLoadingInfo.builder()
                .loadedClassCount(classLoadingMXBean.getLoadedClassCount())
                .totalLoadedClassCount(classLoadingMXBean.getTotalLoadedClassCount())
                .unloadedClassCount(classLoadingMXBean.getUnloadedClassCount())
                .build();

        // 获取运行时信息
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        SystemMetricsResponse.RuntimeInfo runtime = SystemMetricsResponse.RuntimeInfo.builder()
                .uptime(runtimeMXBean.getUptime())
                .startTime(runtimeMXBean.getStartTime())
                .javaVersion(System.getProperty("java.version"))
                .javaVendor(System.getProperty("java.vendor"))
                .build();

        // 获取数据库连接池信息
        SystemMetricsResponse.DatabaseInfo database = SystemMetricsResponse.DatabaseInfo.builder()
                .activeConnections(dataSource.getHikariPoolMXBean().getActiveConnections())
                .idleConnections(dataSource.getHikariPoolMXBean().getIdleConnections())
                .totalConnections(dataSource.getHikariPoolMXBean().getTotalConnections())
                .maxConnections((long) dataSource.getMaximumPoolSize())
                .build();

        SystemMetricsResponse response = SystemMetricsResponse.builder()
                .heapMemory(heapMemory)
                .nonHeapMemory(nonHeapMemory)
                .threads(threads)
                .classLoading(classLoading)
                .runtime(runtime)
                .database(database)
                .build();

        return Result.success(response);
    }

    private SystemMetricsResponse.MemoryInfo getMemoryInfo(java.lang.management.MemoryUsage memoryUsage) {
        long used = memoryUsage.getUsed();
        long committed = memoryUsage.getCommitted();
        long max = memoryUsage.getMax();
        double usagePercent = max > 0 ? (double) used / max * 100 : 0;

        return SystemMetricsResponse.MemoryInfo.builder()
                .used(used)
                .committed(committed)
                .max(max)
                .usagePercent(usagePercent)
                .build();
    }
}