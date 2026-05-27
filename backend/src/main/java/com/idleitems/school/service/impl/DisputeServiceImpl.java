package com.idleitems.school.service.impl;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idleitems.school.entity.Dispute;
import com.idleitems.school.entity.Order;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.DisputeRepository;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.service.DisputeService;
import com.idleitems.school.service.NotificationService;
import com.idleitems.school.util.SensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisputeServiceImpl implements DisputeService {

    private final DisputeRepository disputeRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    private static final List<Dispute.DisputeStatus> ACTIVE_STATUSES = List.of(
            Dispute.DisputeStatus.PENDING,
            Dispute.DisputeStatus.ASSIGNED,
            Dispute.DisputeStatus.PROCESSING,
            Dispute.DisputeStatus.ESCALATED
    );

    @Override
    @Transactional
    public Dispute createDispute(Long applicantId, Long orderId, Integer disputeType, String reason,
                                 String description, String evidenceImages, String expectResult, BigDecimal expectRefundAmount) {
        log.info("用户{}发起纠纷，订单ID: {}, 类型: {}", applicantId, orderId, disputeType);

        // 敏感词检查
        List<String> sensitiveWords = new ArrayList<>();
        sensitiveWords.addAll(SensitiveWordFilter.findSensitiveWords(reason));
        if (description != null) {
            sensitiveWords.addAll(SensitiveWordFilter.findSensitiveWords(description));
        }
        if (!sensitiveWords.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    SensitiveWordFilter.getWarningMessage(sensitiveWords));
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getBuyerId().equals(applicantId) && !order.getSellerId().equals(applicantId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权对此订单发起纠纷");
        }

        if (disputeRepository.existsByOrderIdAndDisputeStatusIn(orderId, ACTIVE_STATUSES)) {
            throw new BusinessException(ErrorCode.CONFLICT, "该订单已有未处理的纠纷");
        }

        Long respondentId = order.getBuyerId().equals(applicantId) ? order.getSellerId() : order.getBuyerId();

        Dispute dispute = new Dispute();
        dispute.setDisputeNo(generateDisputeNo());
        dispute.setOrderId(orderId);
        dispute.setApplicantId(applicantId);
        dispute.setRespondentId(respondentId);
        dispute.setDisputeType(disputeType != null ? disputeType : 1);
        dispute.setReason(reason);
        dispute.setDescription(description);
        dispute.setEvidenceImages(evidenceImages);
        dispute.setExpectResult(expectResult);
        dispute.setExpectRefundAmount(expectRefundAmount);
        dispute.setDisputeStatus(Dispute.DisputeStatus.PENDING);
        dispute.setIsUrgent(false);
        dispute.setPriority(1);
        dispute.setIsEscalated(false);

        Dispute savedDispute = disputeRepository.save(dispute);

        // 通知被申请人
        notificationService.createNotification(
                respondentId,
                3, // DISPUTE类型
                "您有新的纠纷需要处理",
                "订单号相关纠纷已提交，请及时处理",
                savedDispute.getId(),
                "DISPUTE"
        );

        log.info("纠纷创建成功，ID: {}, 编号: {}", savedDispute.getId(), savedDispute.getDisputeNo());

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
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "纠纷不存在"));

        if (userId != null) {
            if (!dispute.getApplicantId().equals(userId) && !dispute.getRespondentId().equals(userId)) {
                User user = userRepository.findById(userId).orElse(null);
                if (user == null || user.getRole() != User.Role.ADMIN) {
                    throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权查看此纠纷");
                }
            }
        }

        return dispute;
    }

    @Override
    @Transactional
    public Dispute replyDispute(Long disputeId, Long userId, String content) {
        // 敏感词检查
        List<String> sensitiveWords = SensitiveWordFilter.findSensitiveWords(content);
        if (!sensitiveWords.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    SensitiveWordFilter.getWarningMessage(sensitiveWords));
        }

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "纠纷不存在"));

        if (!dispute.getApplicantId().equals(userId) && !dispute.getRespondentId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权回复此纠纷");
        }

        if (dispute.getDisputeStatus() == Dispute.DisputeStatus.PENDING ||
            dispute.getDisputeStatus() == Dispute.DisputeStatus.ASSIGNED) {
            dispute.setDisputeStatus(Dispute.DisputeStatus.PROCESSING);
        }

        addProcessLog(dispute, "USER_REPLY", "用户" + userId + "回复: " + content);

        String currentDescription = dispute.getDescription() != null ? dispute.getDescription() : "";
        String reply = "\n\n【" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "】"
                + (dispute.getApplicantId().equals(userId) ? "申请人" : "被申请人") + "回复：" + content;
        dispute.setDescription(currentDescription + reply);

        // 通知对方有新回复
        Long notifyUserId = dispute.getApplicantId().equals(userId) ? dispute.getRespondentId() : dispute.getApplicantId();
        try {
            notificationService.createNotification(
                    notifyUserId, 3, "纠纷有新回复",
                    "您参与的纠纷有新的回复，请查看",
                    dispute.getId(), "DISPUTE");
        } catch (Exception e) {
            log.warn("纠纷回复通知发送失败: disputeId={}, error={}", disputeId, e.getMessage());
        }

        return disputeRepository.save(dispute);
    }

    @Override
    @Transactional
    public Dispute handleDispute(Long disputeId, Long handlerId, String result, String action,
                                 BigDecimal actualRefundAmount, String processRemark) {
        log.info("管理员{}处理纠纷，ID: {}, 操作: {}, 结果: {}", handlerId, disputeId, action, result);

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "纠纷不存在"));

        dispute.setHandlerId(handlerId);
        dispute.setResult(result);
        dispute.setProcessRemark(processRemark);

        if ("RESOLVED".equals(action)) {
            dispute.setDisputeStatus(Dispute.DisputeStatus.RESOLVED);
            dispute.setCompleteTime(LocalDateTime.now());

            if ("APPROVE_REFUND".equals(result) || "PARTIAL_REFUND".equals(result)) {
                Order order = orderRepository.findById(dispute.getOrderId()).orElse(null);
                if (order != null) {
                    order.setOrderStatus(Order.OrderStatus.REFUNDED);
                    order.setRefundAmount(actualRefundAmount != null ? actualRefundAmount :
                            (result.equals("APPROVE_REFUND") ? order.getPrice() : BigDecimal.ZERO));
                    order.setRefundTime(LocalDateTime.now());
                    order.setRefundResult(result);
                    order.setRefundAdminId(handlerId);
                    orderRepository.save(order);

                    dispute.setActualRefundAmount(actualRefundAmount != null ? actualRefundAmount : order.getPrice());
                }
            }

            // 通知双方纠纷已解决
            notifyDisputeParties(dispute, "纠纷已解决", "您提交的纠纷已处理完成，结果: " + result);

        } else if ("CLOSED".equals(action)) {
            dispute.setDisputeStatus(Dispute.DisputeStatus.CLOSED);
            dispute.setCloseTime(LocalDateTime.now());
            dispute.setCloseType(Dispute.CloseType.ADMIN_CLOSE.getValue());

            // 通知双方纠纷已关闭
            notifyDisputeParties(dispute, "纠纷已关闭", "管理员已关闭此纠纷");
        }

        addProcessLog(dispute, "HANDLE", "管理员处理: " + result);

        return disputeRepository.save(dispute);
    }

    /**
     * 通知纠纷双方
     */
    private void notifyDisputeParties(Dispute dispute, String title, String content) {
        try {
            notificationService.createNotification(
                    dispute.getApplicantId(), 3, title, content, dispute.getId(), "DISPUTE");
            notificationService.createNotification(
                    dispute.getRespondentId(), 3, title, content, dispute.getId(), "DISPUTE");
        } catch (Exception e) {
            log.warn("纠纷通知发送失败: disputeId={}, error={}", dispute.getId(), e.getMessage());
        }
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
        stats.put("assigned", disputeRepository.countByDisputeStatus(Dispute.DisputeStatus.ASSIGNED));
        stats.put("processing", disputeRepository.countByDisputeStatus(Dispute.DisputeStatus.PROCESSING));
        stats.put("escalated", disputeRepository.countByIsEscalatedTrue());
        stats.put("resolved", disputeRepository.countByDisputeStatus(Dispute.DisputeStatus.RESOLVED));
        stats.put("closed", disputeRepository.countByDisputeStatus(Dispute.DisputeStatus.CLOSED));
        stats.put("urgent", disputeRepository.countUrgentPending());

        stats.put("byPriority", Map.of(
                "high", disputeRepository.countByPriorityAndDisputeStatus(3, Dispute.DisputeStatus.PENDING),
                "medium", disputeRepository.countByPriorityAndDisputeStatus(2, Dispute.DisputeStatus.PENDING),
                "low", disputeRepository.countByPriorityAndDisputeStatus(1, Dispute.DisputeStatus.PENDING)
        ));

        return stats;
    }

    @Override
    @Transactional
    public Dispute assignDispute(Long disputeId, Long handlerId, Integer priority) {
        log.info("分配纠纷 {} 给管理员 {}, 优先级: {}", disputeId, handlerId, priority);

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        dispute.setHandlerId(handlerId);
        dispute.setAssignTime(LocalDateTime.now());
        dispute.setDisputeStatus(Dispute.DisputeStatus.ASSIGNED);

        if (priority != null) {
            dispute.setPriority(priority);
        }

        addProcessLog(dispute, "ASSIGN", "分配给管理员 " + handlerId + ", 优先级: " + priority);

        return disputeRepository.save(dispute);
    }

    @Override
    @Transactional
    public Dispute startProcess(Long disputeId, Long handlerId) {
        log.info("开始处理纠纷 {}", disputeId);

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (dispute.getHandlerId() == null) {
            dispute.setHandlerId(handlerId);
        }

        dispute.setStartProcessTime(LocalDateTime.now());
        dispute.setDisputeStatus(Dispute.DisputeStatus.PROCESSING);

        addProcessLog(dispute, "START_PROCESS", "开始处理纠纷");

        return disputeRepository.save(dispute);
    }

    @Override
    @Transactional
    public Dispute escalateDispute(Long disputeId, Long escalatedTo, String reason) {
        log.info("升级纠纷 {} 给管理员 {}, 原因: {}", disputeId, escalatedTo, reason);

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        dispute.setIsEscalated(true);
        dispute.setEscalatedTo(escalatedTo);
        dispute.setEscalatedTime(LocalDateTime.now());
        dispute.setEscalatedReason(reason);
        dispute.setDisputeStatus(Dispute.DisputeStatus.ESCALATED);

        addProcessLog(dispute, "ESCALATE", "升级纠纷, 原因: " + reason);

        return disputeRepository.save(dispute);
    }

    @Override
    @Transactional
    public Dispute submitSatisfaction(Long disputeId, Long userId, Integer score, String remark) {
        log.info("用户 {} 提交满意度评价，纠纷 {}，分数: {}", userId, disputeId, score);

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!dispute.getApplicantId().equals(userId) && !dispute.getRespondentId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权评价此纠纷");
        }

        if (dispute.getDisputeStatus() != Dispute.DisputeStatus.RESOLVED &&
            dispute.getDisputeStatus() != Dispute.DisputeStatus.CLOSED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "只能在纠纷解决后进行评价");
        }

        dispute.setSatisfaction(score);
        dispute.setSatisfactionRemark(remark);

        addProcessLog(dispute, "SATISFACTION", "用户 " + userId + " 提交满意度评价: " + score + "分");

        return disputeRepository.save(dispute);
    }

    @Override
    public Map<String, Object> canCreateDispute(Long orderId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("canDispute", false);
        result.put("reason", "");

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            result.put("reason", "订单不存在");
            return result;
        }

        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            result.put("reason", "您不是此订单的买卖双方");
            return result;
        }

        List<Order.OrderStatus> canDisputeStatuses = Arrays.asList(
                Order.OrderStatus.SHIPPED,
                Order.OrderStatus.COMPLETED,
                Order.OrderStatus.REFUND_REQUESTED
        );

        if (!canDisputeStatuses.contains(order.getOrderStatus())) {
            result.put("reason", "当前订单状态不允许申请纠纷");
            return result;
        }

        List<Dispute.DisputeStatus> activeStatuses = Arrays.asList(
                Dispute.DisputeStatus.PENDING,
                Dispute.DisputeStatus.ASSIGNED,
                Dispute.DisputeStatus.PROCESSING,
                Dispute.DisputeStatus.ESCALATED
        );

        if (disputeRepository.existsByOrderIdAndDisputeStatusIn(orderId, activeStatuses)) {
            result.put("reason", "该订单已有未处理的纠纷");
            return result;
        }

        result.put("canDispute", true);
        result.put("orderStatus", order.getOrderStatus().name());
        result.put("orderAmount", order.getPrice());

        return result;
    }

    @Override
    @Transactional
    public Dispute markAsUrgent(Long disputeId, boolean urgent) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        dispute.setIsUrgent(urgent);
        if (urgent) {
            dispute.setPriority(4);
        }

        addProcessLog(dispute, "MARK_URGENT", urgent ? "标记为紧急" : "取消紧急标记");

        return disputeRepository.save(dispute);
    }

    @Override
    public Dispute getActiveDisputeByOrder(Long orderId, Long userId) {
        List<Dispute> activeDisputes = disputeRepository.findActiveByOrderId(orderId);
        if (activeDisputes.isEmpty()) {
            return null;
        }
        Dispute dispute = activeDisputes.get(0);
        if (!dispute.getApplicantId().equals(userId) && !dispute.getRespondentId().equals(userId)) {
            return null;
        }
        return dispute;
    }

    @Override
    @Transactional
    public Dispute closeDispute(Long disputeId, Long userId, Integer closeType, String reason) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        dispute.setDisputeStatus(Dispute.DisputeStatus.CLOSED);
        dispute.setCloseTime(LocalDateTime.now());
        dispute.setCloseType(closeType);

        addProcessLog(dispute, "CLOSE", "关闭纠纷, 原因: " + reason);

        return disputeRepository.save(dispute);
    }

    private String generateDisputeNo() {
        return "DS" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
    }

    private void addProcessLog(Dispute dispute, String action, String content) {
        try {
            List<Map<String, String>> logs = new ArrayList<>();
            if (dispute.getProcessLogs() != null && !dispute.getProcessLogs().isEmpty()) {
                logs = objectMapper.readValue(dispute.getProcessLogs(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
            }

            Map<String, String> log = new HashMap<>();
            log.put("action", action);
            log.put("content", content);
            log.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            logs.add(log);
            dispute.setProcessLogs(objectMapper.writeValueAsString(logs));
        } catch (JsonProcessingException e) {
            log.error("解析处理日志失败", e);
        }
    }
}