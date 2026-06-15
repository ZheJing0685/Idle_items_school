package com.idleitems.school.module.carbon.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.module.carbon.service.CarbonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "碳减排统计", description = "减碳量统计相关接口")
@RestController
@RequestMapping(ApiPaths.Carbon.BASE)
@RequiredArgsConstructor
public class CarbonController {

    private final CarbonService carbonService;

    @Operation(summary = "获取碳减排统计", description = "返回本月减碳量、累计减碳量、等效植树数、交易数、参与人数")
    @GetMapping(ApiPaths.Carbon.STATS_PATH)
    public Result<Map<String, Object>> getStats() {
        CarbonService.MonthlyStats monthly = carbonService.getMonthlyStats();
        BigDecimal totalSavingKg = carbonService.getTotalSavingKg();

        Map<String, Object> stats = new HashMap<>();
        stats.put("monthlySavingKg", monthly.getMonthlySavingKg());
        stats.put("totalSavingKg", totalSavingKg);
        stats.put("treeEquivalent", monthly.getTreeEquivalent());
        stats.put("transactionCount", monthly.getTransactionCount());
        stats.put("participantCount", monthly.getParticipantCount());

        return Result.success(stats);
    }
}
