package com.idleitems.school.service.impl;

import com.idleitems.school.entity.Dispute;
import com.idleitems.school.entity.Order;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.DisputeRepository;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.service.DisputeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisputeServiceImpl implements DisputeService {

    private final DisputeRepository disputeRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Dispute createDispute(Long applicantId, Long orderId, String reason, String description, String evidenceImages) {
        log.info("用户{}发起纠纷，订单ID: {}", applicantId, orderId);

        // 验证订单存在
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        // 验证用户是订单的买家或卖家
        if (!order.getBuyerId().equals(applicantId) && !order.getSellerId().equals(applicantId)) {
            throw new IllegalArgumentException("无权对此订单发起纠纷");
        }

        // 确定被申请人
        Long respondentId = order.getBuyerId().equals(applicantId) ? order.getSellerId() : order.getBuyerId();

        // 检查是否有未处理的纠纷
        if (disputeRepository.existsByOrderIdAndDisputeStatusIn(orderId, 
                java.util.List.of(Dispute.DisputeStatus.PENDING, Dispute.DisputeStatus.PROCESSING))) {
            throw new IllegalArgumentException("该订单已有未处理的纠纷");
        }

        // 创建纠纷
        Dispute dispute = new Dispute();
        dispute.setOrderId(orderId);
        dispute.setApplicantId(applicantId);
        dispute.setRespondentId(respondentId);
        dispute.setReason(reason);
        dispute.setDescription(description);
        dispute.setEvidenceImages(evidenceImages);
        dispute.setDisputeStatus(Dispute.DisputeStatus.PENDING);

        Dispute savedDispute = disputeRepository.save(dispute);
        log.info("纠纷创建成功，ID: {}", savedDispute.getId());

        return savedDispute;
    }

    @Override
    public Page<Dispute> getMyDisputes(Long userId, Dispute.DisputeStatus status, Pageable pageable) {
        if (status != null) {
            return disputeRepository.findByApplicantIdAndDisputeStatusOrderByCreatedAtDesc(userId, status, pageable);
        }
        return disputeRepository.findByApplicantIdOrRespondentIdOrderByCreatedAtDesc(userId, userId, pageable);
    }

    @Override
    public Dispute getDisputeById(Long disputeId, Long userId) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("纠纷不存在"));

        // 验证用户是纠纷的参与方或管理员
        if (!dispute.getApplicantId().equals(userId) && !dispute.getRespondentId().equals(userId)) {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null || user.getRole() != User.Role.ADMIN) {
                throw new IllegalArgumentException("无权查看此纠纷");
            }
        }

        return dispute;
    }

    @Override
    @Transactional
    public Dispute replyDispute(Long disputeId, Long userId, String content) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("纠纷不存在"));

        // 验证用户是纠纷的参与方
        if (!dispute.getApplicantId().equals(userId) && !dispute.getRespondentId().equals(userId)) {
            throw new IllegalArgumentException("无权回复此纠纷");
        }

        // 更新纠纷状态为处理中
        if (dispute.getDisputeStatus() == Dispute.DisputeStatus.PENDING) {
            dispute.setDisputeStatus(Dispute.DisputeStatus.PROCESSING);
        }

        // 追加回复内容
        String currentDescription = dispute.getDescription() != null ? dispute.getDescription() : "";
        String reply = "\n\n【" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "】" 
                + (dispute.getApplicantId().equals(userId) ? "申请人" : "被申请人") + "回复：" + content;
        dispute.setDescription(currentDescription + reply);

        return disputeRepository.save(dispute);
    }

    @Override
    @Transactional
    public Dispute handleDispute(Long disputeId, Long handlerId, String result, String action) {
        log.info("管理员处理纠纷，ID: {}, 操作: {}", disputeId, action);

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("纠纷不存在"));

        dispute.setHandlerId(handlerId);
        dispute.setResult(result);

        if ("RESOLVED".equals(action)) {
            dispute.setDisputeStatus(Dispute.DisputeStatus.RESOLVED);
            
            // 根据处理结果可能需要更新订单状态
            // 例如：同意退款时更新订单状态
        } else if ("CLOSED".equals(action)) {
            dispute.setDisputeStatus(Dispute.DisputeStatus.CLOSED);
        }

        return disputeRepository.save(dispute);
    }

    @Override
    public Page<Dispute> getAllDisputes(Dispute.DisputeStatus status, Pageable pageable) {
        if (status != null) {
            return disputeRepository.findByDisputeStatusOrderByCreatedAtDesc(status, pageable);
        }
        return disputeRepository.findAll(pageable);
    }

    @Override
    public Map<String, Object> getDisputeStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", disputeRepository.count());
        stats.put("pending", disputeRepository.countByDisputeStatus(Dispute.DisputeStatus.PENDING));
        stats.put("processing", disputeRepository.countByDisputeStatus(Dispute.DisputeStatus.PROCESSING));
        stats.put("resolved", disputeRepository.countByDisputeStatus(Dispute.DisputeStatus.RESOLVED));
        stats.put("closed", disputeRepository.countByDisputeStatus(Dispute.DisputeStatus.CLOSED));
        return stats;
    }
}
