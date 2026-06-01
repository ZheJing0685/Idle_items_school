package com.idleitems.school.module.admin.service;

import com.idleitems.school.module.admin.dto.LogAnalysisResponse;
import com.idleitems.school.module.admin.repository.AdminLogRepository;
import com.idleitems.school.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogAnalysisService {

    private final AdminLogRepository adminLogRepository;
    private final UserRepository userRepository;

    public LogAnalysisResponse getLogAnalysis() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime weekStart = now.minusWeeks(1);
        LocalDateTime monthStart = now.minusMonths(1);

        // 统计总数
        Long totalOperations = adminLogRepository.count();
        Long todayOperations = adminLogRepository.countByCreatedAtAfter(todayStart);
        Long weekOperations = adminLogRepository.countByCreatedAtAfter(weekStart);
        Long monthOperations = adminLogRepository.countByCreatedAtAfter(monthStart);

        // 统计操作类型
        List<Object[]> operationCounts = adminLogRepository.countGroupByOperation();
        List<LogAnalysisResponse.OperationCount> operationCountList = operationCounts.stream()
                .map(obj -> LogAnalysisResponse.OperationCount.builder()
                        .operation((String) obj[0])
                        .count((Long) obj[1])
                        .build())
                .collect(Collectors.toList());

        // 统计目标类型
        List<Object[]> targetTypeCounts = adminLogRepository.countGroupByTargetType();
        List<LogAnalysisResponse.TargetTypeCount> targetTypeCountList = targetTypeCounts.stream()
                .map(obj -> LogAnalysisResponse.TargetTypeCount.builder()
                        .targetType((String) obj[0])
                        .count((Long) obj[1])
                        .build())
                .collect(Collectors.toList());

        // 统计每日操作数（最近7天）
        List<LogAnalysisResponse.DailyCount> dailyCounts = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.plusDays(1);
            Long count = adminLogRepository.countByCreatedAtBetween(dayStart, dayEnd);
            dailyCounts.add(LogAnalysisResponse.DailyCount.builder()
                    .date(dayStart)
                    .count(count)
                    .build());
        }

        // 统计活跃管理员（操作次数前5）
        List<Object[]> topAdminOperations = adminLogRepository.findTop5AdminsByOperationCount();
        List<LogAnalysisResponse.AdminActivity> topAdmins = topAdminOperations.stream()
                .map(obj -> {
                    Long adminId = (Long) obj[0];
                    Long count = (Long) obj[1];
                    String adminName = userRepository.findById(adminId)
                            .map(user -> user.getUsername())
                            .orElse("未知用户");
                    return LogAnalysisResponse.AdminActivity.builder()
                            .adminId(adminId)
                            .adminName(adminName)
                            .operationCount(count)
                            .build();
                })
                .collect(Collectors.toList());

        return LogAnalysisResponse.builder()
                .totalOperations(totalOperations)
                .todayOperations(todayOperations)
                .weekOperations(weekOperations)
                .monthOperations(monthOperations)
                .operationCounts(operationCountList)
                .dailyCounts(dailyCounts)
                .targetTypeCounts(targetTypeCountList)
                .topAdmins(topAdmins)
                .build();
    }
}