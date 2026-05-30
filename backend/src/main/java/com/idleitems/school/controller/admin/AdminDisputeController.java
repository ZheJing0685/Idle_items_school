package com.idleitems.school.controller.admin;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.common.Result;
import com.idleitems.school.entity.Dispute;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.idleitems.school.entity.User;
import com.idleitems.school.service.DisputeQueryService;
import com.idleitems.school.service.DisputeCommandService;
import com.idleitems.school.config.ApiPaths;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping(ApiPaths.Admin.DISPUTES)
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN}, message = "需要管理员权限")
@Tag(name = "管理员-纠纷管理", description = "管理员纠纷处理相关接口")
public class AdminDisputeController {

    private final DisputeCommandService disputeCommandService;
    private final DisputeQueryService disputeQueryService;

    @GetMapping
    @Operation(summary = "获取纠纷列表", description = "分页查询所有纠纷，支持按状态筛选")
    public Result<Page<Dispute>> getDisputes(
            @RequestParam(value = "status", required = false) Dispute.DisputeStatus status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "priority", "createdAt"));
        return Result.success(disputeQueryService.getAllDisputes(status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取纠纷详情", description = "根据ID获取指定纠纷的详细信息")
    public Result<Dispute> getDispute(@PathVariable Long id) {
        return Result.success(disputeQueryService.getDisputeById(id, 0L));
    }

    @PostMapping
    @Operation(summary = "创建纠纷", description = "管理员代用户创建纠纷")
    @RequireRole(value = {User.Role.ADMIN}, message = "需要管理员权限")
    public Result<Dispute> createDisputeForAdmin(
            @RequestAttribute("userId") Long adminId,
            @Valid @RequestBody CreateDisputeRequest request) {
        Dispute dispute = disputeCommandService.createDispute(
                request.getApplicantId(),
                request.getOrderId(),
                request.getDisputeType(),
                request.getReason(),
                request.getDescription(),
                request.getEvidenceImages(),
                request.getExpectResult(),
                request.getExpectRefundAmount()
        );
        return Result.success("纠纷已创建", dispute);
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "分配纠纷", description = "将纠纷分配给指定的处理人")
    public Result<Dispute> assignDispute(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @Valid @RequestBody AssignDisputeRequest request) {
        Dispute dispute = disputeCommandService.assignDispute(id, request.getHandlerId(), request.getPriority());
        return Result.success("纠纷已分配", dispute);
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "开始处理纠纷", description = "标记纠纷开始处理")
    public Result<Dispute> startProcess(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id) {
        Dispute dispute = disputeCommandService.startProcess(id, adminId);
        return Result.success("开始处理", dispute);
    }

    @PostMapping("/{id}/handle")
    @Operation(summary = "处理纠纷", description = "处理纠纷并给出处理结果")
    public Result<Dispute> handleDispute(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @Valid @RequestBody HandleDisputeRequest request) {
        Dispute dispute = disputeCommandService.handleDispute(
                id, adminId, request.getResult(), request.getAction(),
                request.getActualRefundAmount(), request.getProcessRemark()
        );
        return Result.success("纠纷已处理", dispute);
    }

    @PostMapping("/{id}/escalate")
    @Operation(summary = "升级纠纷", description = "将纠纷升级到更高级别处理")
    public Result<Dispute> escalateDispute(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @Valid @RequestBody EscalateDisputeRequest request) {
        Dispute dispute = disputeCommandService.escalateDispute(id, request.getEscalatedTo(), request.getReason());
        return Result.success("纠纷已升级", dispute);
    }

    @PostMapping("/{id}/urgent")
    @Operation(summary = "标记紧急纠纷", description = "标记或取消标记纠纷为紧急状态")
    public Result<Dispute> markAsUrgent(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestParam boolean urgent) {
        Dispute dispute = disputeCommandService.markAsUrgent(id, urgent);
        return Result.success(urgent ? "已标记为紧急" : "已取消紧急标记", dispute);
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "关闭纠纷", description = "关闭指定的纠纷")
    public Result<Dispute> closeDispute(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @Valid @RequestBody CloseDisputeRequest request) {
        Dispute dispute = disputeCommandService.closeDispute(id, adminId, request.getCloseType(), request.getReason());
        return Result.success("纠纷已关闭", dispute);
    }

    @GetMapping("/stats")
    @Operation(summary = "获取纠纷统计", description = "获取纠纷的统计数据")
    public Result<Map<String, Object>> getDisputeStats() {
        return Result.success(disputeQueryService.getDisputeStats());
    }

    @Data
    public static class CreateDisputeRequest {
        @NotNull(message = "申请人ID不能为空")
        private Long applicantId;
        @NotNull(message = "订单ID不能为空")
        private Long orderId;
        private Integer disputeType;
        @NotBlank(message = "纠纷原因不能为空")
        private String reason;
        private String description;
        private String evidenceImages;
        private String expectResult;
        private BigDecimal expectRefundAmount;
    }

    @Data
    public static class AssignDisputeRequest {
        @NotNull(message = "处理人ID不能为空")
        private Long handlerId;
        private Integer priority;
    }

    @Data
    public static class HandleDisputeRequest {
        @NotBlank(message = "处理结果不能为空")
        private String result;
        @NotBlank(message = "操作类型不能为空")
        private String action;
        private BigDecimal actualRefundAmount;
        private String processRemark;
    }

    @Data
    public static class EscalateDisputeRequest {
        private Long escalatedTo;
        @NotBlank(message = "升级原因不能为空")
        private String reason;
    }

    @Data
    public static class CloseDisputeRequest {
        private Integer closeType;
        private String reason;
    }
}