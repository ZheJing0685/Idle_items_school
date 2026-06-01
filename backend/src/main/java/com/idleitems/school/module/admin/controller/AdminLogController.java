package com.idleitems.school.module.admin.controller;

import com.idleitems.school.common.annotation.RequireRole;
import com.idleitems.school.common.Result;
import com.idleitems.school.module.admin.entity.AdminLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.admin.service.AdminLogService;
import com.idleitems.school.config.ApiPaths;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(ApiPaths.Admin.LOGS)
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN}, message = "需要管理员权限")
@Tag(name = "管理员-日志管理", description = "管理员操作日志相关接口")
public class AdminLogController {

    private final AdminLogService adminLogService;

    @GetMapping
    @Operation(summary = "获取操作日志列表", description = "分页查询管理员操作日志，支持多条件筛选")
    public Result<Page<AdminLog>> getAdminLogs(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "adminId", required = false) Long adminId,
            @RequestParam(value = "targetType", required = false) String targetType,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AdminLog> logs = adminLogService.getAdminLogsByFilters(keyword, adminId, targetType, startDate, endDate, pageable);
        
        return Result.success(logs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取日志详情", description = "根据ID获取指定操作日志的详细信息")
    public Result<AdminLog> getAdminLog(@PathVariable Long id) {
        AdminLog log = adminLogService.getAdminLogById(id);
        return Result.success(log);
    }

    @GetMapping("/export")
    @Operation(summary = "导出操作日志", description = "导出操作日志列表为CSV文件")
    public void exportLogs(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "adminId", required = false) Long adminId,
            @RequestParam(value = "targetType", required = false) String targetType,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletResponse response) throws IOException {
        
        List<AdminLog> logs = adminLogService.getAdminLogsForExport(keyword, adminId, targetType, startDate, endDate);
        
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", 
            "attachment;filename=logs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
        
        StringBuilder csv = new StringBuilder();
        csv.append("ID,操作人ID,操作类型,目标类型,目标ID,详情,IP地址,时间\n");
        
        for (AdminLog log : logs) {
            csv.append(log.getId()).append(",")
               .append(log.getAdminId()).append(",")
               .append(log.getOperation()).append(",")
               .append(log.getTargetType()).append(",")
               .append(log.getTargetId() != null ? log.getTargetId() : "").append(",")
               .append(log.getDetails() != null ? log.getDetails().replace(",", ";") : "").append(",")
               .append(log.getIpAddress() != null ? log.getIpAddress() : "").append(",")
               .append(log.getCreatedAt() != null ? log.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "").append("\n");
        }
        
        response.getOutputStream().write(csv.toString().getBytes("UTF-8"));
        response.getOutputStream().flush();
    }
}
