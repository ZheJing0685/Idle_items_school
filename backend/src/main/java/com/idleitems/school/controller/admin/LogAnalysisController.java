package com.idleitems.school.controller.admin;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.common.Result;
import com.idleitems.school.dto.analytics.LogAnalysisResponse;
import com.idleitems.school.entity.User;
import com.idleitems.school.service.LogAnalysisService;
import com.idleitems.school.config.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.Admin.LOGS_ANALYSIS)
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN})
@Tag(name = "管理员-日志分析", description = "管理员日志分析相关接口")
public class LogAnalysisController {

    private final LogAnalysisService logAnalysisService;

    @GetMapping
    @Operation(summary = "获取日志分析", description = "获取操作日志的分析统计数据")
    public Result<LogAnalysisResponse> getLogAnalysis() {
        return Result.success(logAnalysisService.getLogAnalysis());
    }
}