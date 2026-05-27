package com.idleitems.school.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "日志分析结果响应")
public class LogAnalysisResponse {
    @Schema(description = "总操作数")
    private Long totalOperations;
    @Schema(description = "今日操作数")
    private Long todayOperations;
    @Schema(description = "本周操作数")
    private Long weekOperations;
    @Schema(description = "本月操作数")
    private Long monthOperations;
    @Schema(description = "操作类型统计")
    private List<OperationCount> operationCounts;
    @Schema(description = "每日操作统计")
    private List<DailyCount> dailyCounts;
    @Schema(description = "目标类型统计")
    private List<TargetTypeCount> targetTypeCounts;
    @Schema(description = "活跃管理员TOP")
    private List<AdminActivity> topAdmins;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "操作计数")
    public static class OperationCount {
        @Schema(description = "操作类型")
        private String operation;
        @Schema(description = "数量")
        private Long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "每日计数")
    public static class DailyCount {
        @Schema(description = "日期")
        private LocalDateTime date;
        @Schema(description = "数量")
        private Long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "目标类型计数")
    public static class TargetTypeCount {
        @Schema(description = "目标类型")
        private String targetType;
        @Schema(description = "数量")
        private Long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "管理员活动信息")
    public static class AdminActivity {
        @Schema(description = "管理员ID")
        private Long adminId;
        @Schema(description = "管理员名称")
        private String adminName;
        @Schema(description = "操作次数")
        private Long operationCount;
    }
}