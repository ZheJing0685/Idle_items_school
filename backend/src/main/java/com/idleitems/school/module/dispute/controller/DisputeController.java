package com.idleitems.school.module.dispute.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.module.dispute.dto.CreateDisputeRequest;
import com.idleitems.school.module.dispute.dto.ReplyDisputeRequest;
import com.idleitems.school.module.dispute.dto.SatisfactionRequest;
import com.idleitems.school.module.dispute.entity.Dispute;
import com.idleitems.school.module.dispute.service.DisputeQueryService;
import com.idleitems.school.module.dispute.service.DisputeCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "纠纷管理", description = "交易纠纷处理相关接口")
@RestController
@RequestMapping(ApiPaths.Dispute.BASE)
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeCommandService disputeCommandService;
    private final DisputeQueryService disputeQueryService;

    @Operation(summary = "创建纠纷", description = "买家或卖家提交交易纠纷申请")
    @PostMapping
    public Result<Dispute> createDispute(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateDisputeRequest request) {
        Dispute dispute = disputeCommandService.createDispute(
                userId, request.getOrderId(), request.getDisputeType(),
                request.getReason(), request.getDescription(), request.getEvidenceImages(),
                request.getExpectResult(), request.getExpectRefundAmount());
        return Result.success("纠纷已提交", dispute);
    }

    @Operation(summary = "获取我的纠纷列表", description = "分页查询当前用户的纠纷记录，可按状态筛选")
    @GetMapping
    public Result<Page<Dispute>> getMyDisputes(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "status", required = false) Dispute.DisputeStatus status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(disputeQueryService.getMyDisputes(userId, status, pageable));
    }

    @Operation(summary = "获取纠纷详情", description = "根据纠纷ID获取纠纷详细信息")
    @GetMapping("/{id}")
    public Result<Dispute> getDispute(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        return Result.success(disputeQueryService.getDisputeById(id, userId));
    }

    @Operation(summary = "回复纠纷", description = "对指定纠纷进行回复")
    @PostMapping("/{id}/reply")
    public Result<Dispute> replyDispute(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody ReplyDisputeRequest request) {
        Dispute dispute = disputeCommandService.replyDispute(id, userId, request.getContent());
        return Result.success("回复成功", dispute);
    }

    @Operation(summary = "提交纠纷满意度评价", description = "纠纷处理后提交满意度评分")
    @PostMapping("/{id}/satisfaction")
    public Result<Dispute> submitSatisfaction(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody SatisfactionRequest request) {
        Dispute dispute = disputeCommandService.submitSatisfaction(id, userId, request.getScore(), request.getRemark());
        return Result.success("评价提交成功", dispute);
    }

    @Operation(summary = "获取纠纷统计", description = "获取纠纷处理相关统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getDisputeStats(
            @RequestAttribute("userId") Long userId) {
        return Result.success(disputeQueryService.getDisputeStats());
    }

    @Operation(summary = "检查是否可以发起纠纷", description = "检查指定订单是否满足发起纠纷的条件")
    @GetMapping("/orders/{orderId}/can-dispute")
    public Result<Map<String, Object>> canCreateDispute(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long orderId) {
        return Result.success(disputeQueryService.canCreateDispute(orderId, userId));
    }

    @Operation(summary = "获取订单活跃纠纷", description = "获取指定订单的当前活跃纠纷信息")
    @GetMapping("/orders/{orderId}/dispute")
    public Result<Dispute> getActiveDispute(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long orderId) {
        Dispute dispute = disputeQueryService.getActiveDisputeByOrder(orderId, userId);
        return Result.success(dispute);
    }
}
