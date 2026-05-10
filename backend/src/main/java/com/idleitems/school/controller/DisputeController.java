package com.idleitems.school.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.entity.Dispute;
import com.idleitems.school.service.DisputeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping
    public Result<Dispute> createDispute(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateDisputeRequest request) {
        Dispute dispute = disputeService.createDispute(
                userId, 
                request.getOrderId(), 
                request.getReason(), 
                request.getDescription(), 
                request.getEvidenceImages()
        );
        return Result.success("纠纷已提交", dispute);
    }

    @GetMapping
    public Result<Page<Dispute>> getMyDisputes(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "status", required = false) Dispute.DisputeStatus status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(disputeService.getMyDisputes(userId, status, pageable));
    }

    @GetMapping("/{id}")
    public Result<Dispute> getDispute(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        return Result.success(disputeService.getDisputeById(id, userId));
    }

    @PostMapping("/{id}/reply")
    public Result<Dispute> replyDispute(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody ReplyDisputeRequest request) {
        Dispute dispute = disputeService.replyDispute(id, userId, request.getContent());
        return Result.success("回复成功", dispute);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getDisputeStats() {
        return Result.success(disputeService.getDisputeStats());
    }

    @Data
    public static class CreateDisputeRequest {
        private Long orderId;
        @NotBlank(message = "纠纷原因不能为空")
        private String reason;
        private String description;
        private String evidenceImages;
    }

    @Data
    public static class ReplyDisputeRequest {
        @NotBlank(message = "回复内容不能为空")
        private String content;
    }
}
