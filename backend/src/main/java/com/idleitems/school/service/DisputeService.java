package com.idleitems.school.service;

import com.idleitems.school.entity.Dispute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface DisputeService {

    /**
     * 发起纠纷
     */
    Dispute createDispute(Long applicantId, Long orderId, String reason, String description, String evidenceImages);

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
    Dispute handleDispute(Long disputeId, Long handlerId, String result, String action);

    /**
     * 管理员获取所有纠纷列表
     */
    Page<Dispute> getAllDisputes(Dispute.DisputeStatus status, Pageable pageable);

    /**
     * 获取纠纷统计
     */
    Map<String, Object> getDisputeStats();
}
