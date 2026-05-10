package com.idleitems.school.controller.admin;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.common.Result;
import com.idleitems.school.entity.Dispute;
import com.idleitems.school.entity.User;
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
@RequestMapping("/api/admin/disputes")
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN}, message = "需要管理员权限")
public class AdminDisputeController {

    private final DisputeService disputeService;

    @GetMapping
    public Result<Page<Dispute>> getDisputes(
            @RequestParam(value = "status", required = false) Dispute.DisputeStatus status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(disputeService.getAllDisputes(status, pageable));
    }

    @GetMapping("/{id}")
    public Result<Dispute> getDispute(@PathVariable Long id) {
        // 管理员可以查看任何纠纷
        return Result.success(disputeService.getDisputeById(id, 0L));
    }

    @PutMapping("/{id}/handle")
    public Result<Dispute> handleDispute(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @Valid @RequestBody HandleDisputeRequest request) {
        Dispute dispute = disputeService.handleDispute(id, adminId, request.getResult(), request.getAction());
        return Result.success("纠纷已处理", dispute);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getDisputeStats() {
        return Result.success(disputeService.getDisputeStats());
    }

    @Data
    public static class HandleDisputeRequest {
        @NotBlank(message = "处理结果不能为空")
        private String result;
        @NotBlank(message = "操作类型不能为空")
        private String action;  // RESOLVED 或 CLOSED
    }
}
