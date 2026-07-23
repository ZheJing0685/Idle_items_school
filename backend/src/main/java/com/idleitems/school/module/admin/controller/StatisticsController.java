package com.idleitems.school.module.admin.controller;

import com.idleitems.school.common.annotation.RequireRole;
import com.idleitems.school.common.Result;
import com.idleitems.school.module.admin.service.StatisticsService;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.config.ApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiPaths.Admin.STATISTICS)
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN}, message = "需要管理员权限")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/dashboard")
    public Result<Object> getDashboard(
            @RequestParam(defaultValue = "today") String timeRange,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(statisticsService.getDashboard(timeRange, startDate, endDate));
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        return Result.success(statisticsService.getOverview());
    }

    @GetMapping("/monthly")
    public Result<Map<String, Object>> getMonthlyStatistics() {
        return Result.success(statisticsService.getMonthlyStatistics());
    }

    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> getCategoryStatistics() {
        return Result.success(statisticsService.getCategoryStatistics());
    }

    @GetMapping("/hot-items")
    public Result<List<Item>> getHotItems() {
        return Result.success(statisticsService.getHotItems());
    }
}
