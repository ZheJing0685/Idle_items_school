package com.idleitems.school.service;

import com.idleitems.school.entity.Dispute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Map;

public interface DisputeService {

    /**
     * 发起纠纷
     */
    Dispute createDispute(Long applicantId, Long orderId, Integer disputeType, String reason,
                          String description, String evidenceImages, String expectResult, BigDecimal expectRefundAmount);

    /**
     * 获取用户的纠纷列表
     */
    Page<Dispute> getMyDisputes(Long userId, Dispute.DisputeStatus status, Pageable pageable);

    /**
     * 获取纠纷详情
     */
    Dispute getDisputeById(Long disputeId, Long userId);

    /**
     * 回复纠纷
     */
    Dispute replyDispute(Long disputeId, Long userId, String content);

    /**
     * 管理员处理纠纷
     */
    Dispute handleDispute(Long disputeId, Long handlerId, String result, String action,
                          BigDecimal actualRefundAmount, String processRemark);

    /**
     * 管理员获取所有纠纷列表
     */
    Page<Dispute> getAllDisputes(Dispute.DisputeStatus status, Pageable pageable);

    /**
     * 获取纠纷统计
     */
    Map<String, Object> getDisputeStats();

    /**
     * 分配纠纷
     */
    Dispute assignDispute(Long disputeId, Long handlerId, Integer priority);

    /**
     * 开始处理纠纷
     */
    Dispute startProcess(Long disputeId, Long handlerId);

    /**
     * 升级纠纷
     */
    Dispute escalateDispute(Long disputeId, Long escalatedTo, String reason);

    /**
     * 提交满意度评价
     */
    Dispute submitSatisfaction(Long disputeId, Long userId, Integer score, String remark);

    /**
     * 检查是否可以对订单申请纠纷
     */
    Map<String, Object> canCreateDispute(Long orderId, Long userId);

    /**
     * 获取订单的活跃纠纷
     */
    Dispute getActiveDisputeByOrder(Long orderId, Long userId);

    /**
     * 标记纠纷为紧急
     */
    Dispute markAsUrgent(Long disputeId, boolean urgent);

    /**
     * 关闭纠纷
     */
    Dispute closeDispute(Long disputeId, Long userId, Integer closeType, String reason);
}