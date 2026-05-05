package com.idleitems.school.controller.admin;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.common.Result;
import com.idleitems.school.dto.analytics.LogAnalysisResponse;
import com.idleitems.school.entity.User;
import com.idleitems.school.service.LogAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/logs/analysis")
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN})
public class LogAnalysisController {

    private final LogAnalysisService logAnalysisService;

    @GetMapping
    public Result<LogAnalysisResponse> getLogAnalysis() {
        return Result.success(logAnalysisService.getLogAnalysis());
    }
}