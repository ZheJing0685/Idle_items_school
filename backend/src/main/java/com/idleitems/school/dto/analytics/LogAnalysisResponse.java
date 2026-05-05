package com.idleitems.school.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogAnalysisResponse {
    private Long totalOperations;
    private Long todayOperations;
    private Long weekOperations;
    private Long monthOperations;
    private List<OperationCount> operationCounts;
    private List<DailyCount> dailyCounts;
    private List<TargetTypeCount> targetTypeCounts;
    private List<AdminActivity> topAdmins;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationCount {
        private String operation;
        private Long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyCount {
        private LocalDateTime date;
        private Long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetTypeCount {
        private String targetType;
        private Long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminActivity {
        private Long adminId;
        private String adminName;
        private Long operationCount;
    }
}