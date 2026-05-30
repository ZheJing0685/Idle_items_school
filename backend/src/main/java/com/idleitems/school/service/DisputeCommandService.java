package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idleitems.school.entity.Dispute;
import com.idleitems.school.entity.Order;
import com.idleitems.school.repository.DisputeRepository;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.util.SensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisputeCommandService {

    private final DisputeRepository disputeRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    private static final List<Dispute.DisputeStatus> ACTIVE_STATUSES = List.of(
            Dispute.DisputeStatus.PENDING,
            Dispute.DisputeStatus.ASSIGNED,
            Dispute.DisputeStatus.PROCESSING,
            Dispute.DisputeStatus.ESCALATED
    );

    @Transactional
    public Dispute createDispute(Long applicantId, Long orderId, Integer disputeType, String reason,
                                 String description, String evidenceImages, String expectResult, BigDecimal expectRefundAmount) {
        log.info("Creating dispute: userId={}, orderId={}", applicantId, orderId);

        List<String> sensitiveWords = new ArrayList<>(SensitiveWordFilter.findSensitiveWords(reason));
        if (description != null) {
            sensitiveWords.addAll(SensitiveWordFilter.findSensitiveWords(description));
        }
        if (!sensitiveWords.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, SensitiveWordFilter.getWarningMessage(sensitiveWords));
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getBuyerId().equals(applicantId) && !order.getSellerId().equals(applicantId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "No permission");
        }

        if (disputeRepository.existsByOrderIdAndDisputeStatusIn(orderId, ACTIVE_STATUSES)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Active dispute exists");
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

        notificationService.createNotification(respondentId, 3, "New dispute",
                "A dispute has been submitted for your order", savedDispute.getId(), "DISPUTE");

        log.info("Dispute created: id={}, no={}", savedDispute.getId(), savedDispute.getDisputeNo());
        return savedDispute;
    }

    @Transactional
    public Dispute replyDispute(Long disputeId, Long userId, String content) {
        List<String> sensitiveWords = SensitiveWordFilter.findSensitiveWords(content);
        if (!sensitiveWords.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, SensitiveWordFilter.getWarningMessage(sensitiveWords));
        }

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Dispute not found"));

        if (!dispute.getApplicantId().equals(userId) && !dispute.getRespondentId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "No permission");
        }

        if (dispute.getDisputeStatus() == Dispute.DisputeStatus.PENDING ||
            dispute.getDisputeStatus() == Dispute.DisputeStatus.ASSIGNED) {
            dispute.setDisputeStatus(Dispute.DisputeStatus.PROCESSING);
        }

        addProcessLog(dispute, "USER_REPLY", "User " + userId + " replied: " + content);

        String currentDescription = dispute.getDescription() != null ? dispute.getDescription() : "";
        String reply = "\n\n[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "] "
                + (dispute.getApplicantId().equals(userId) ? "Applicant" : "Respondent") + " replied: " + content;
        dispute.setDescription(currentDescription + reply);

        Long notifyUserId = dispute.getApplicantId().equals(userId) ? dispute.getRespondentId() : dispute.getApplicantId();
        try {
            notificationService.createNotification(notifyUserId, 3, "Dispute reply",
                    "New reply on your dispute", dispute.getId(), "DISPUTE");
        } catch (Exception e) {
            log.warn("Failed to send dispute reply notification: disputeId={}", disputeId, e.getMessage());
        }

        return disputeRepository.save(dispute);
    }

    @Transactional
    public Dispute handleDispute(Long disputeId, Long handlerId, String result, String action,
                                 BigDecimal actualRefundAmount, String processRemark) {
        log.info("Handling dispute: disputeId={}, handlerId={}, action={}", disputeId, handlerId, action);

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Dispute not found"));

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
            notifyDisputeParties(dispute, "Dispute resolved", "Your dispute has been resolved: " + result);

        } else if ("CLOSED".equals(action)) {
            dispute.setDisputeStatus(Dispute.DisputeStatus.CLOSED);
            dispute.setCloseTime(LocalDateTime.now());
            dispute.setCloseType(Dispute.CloseType.ADMIN_CLOSE.getValue());
            notifyDisputeParties(dispute, "Dispute closed", "Admin has closed this dispute");
        }

        addProcessLog(dispute, "HANDLE", "Admin handled: " + result);
        return disputeRepository.save(dispute);
    }

    @Transactional
    public Dispute assignDispute(Long disputeId, Long handlerId, Integer priority) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        dispute.setHandlerId(handlerId);
        dispute.setAssignTime(LocalDateTime.now());
        dispute.setDisputeStatus(Dispute.DisputeStatus.ASSIGNED);
        if (priority != null) dispute.setPriority(priority);

        addProcessLog(dispute, "ASSIGN", "Assigned to admin " + handlerId);
        return disputeRepository.save(dispute);
    }

    @Transactional
    public Dispute startProcess(Long disputeId, Long handlerId) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (dispute.getHandlerId() == null) dispute.setHandlerId(handlerId);
        dispute.setStartProcessTime(LocalDateTime.now());
        dispute.setDisputeStatus(Dispute.DisputeStatus.PROCESSING);

        addProcessLog(dispute, "START_PROCESS", "Processing started");
        return disputeRepository.save(dispute);
    }

    @Transactional
    public Dispute escalateDispute(Long disputeId, Long escalatedTo, String reason) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        dispute.setIsEscalated(true);
        dispute.setEscalatedTo(escalatedTo);
        dispute.setEscalatedTime(LocalDateTime.now());
        dispute.setEscalatedReason(reason);
        dispute.setDisputeStatus(Dispute.DisputeStatus.ESCALATED);

        addProcessLog(dispute, "ESCALATE", "Escalated: " + reason);
        return disputeRepository.save(dispute);
    }

    @Transactional
    public Dispute submitSatisfaction(Long disputeId, Long userId, Integer score, String remark) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!dispute.getApplicantId().equals(userId) && !dispute.getRespondentId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "No permission");
        }

        if (dispute.getDisputeStatus() != Dispute.DisputeStatus.RESOLVED &&
            dispute.getDisputeStatus() != Dispute.DisputeStatus.CLOSED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Can only rate after resolution");
        }

        dispute.setSatisfaction(score);
        dispute.setSatisfactionRemark(remark);

        addProcessLog(dispute, "SATISFACTION", "User " + userId + " rated: " + score);
        return disputeRepository.save(dispute);
    }

    @Transactional
    public Dispute markAsUrgent(Long disputeId, boolean urgent) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        dispute.setIsUrgent(urgent);
        if (urgent) dispute.setPriority(4);

        addProcessLog(dispute, "MARK_URGENT", urgent ? "Marked urgent" : "Unmarked urgent");
        return disputeRepository.save(dispute);
    }

    @Transactional
    public Dispute closeDispute(Long disputeId, Long userId, Integer closeType, String reason) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        dispute.setDisputeStatus(Dispute.DisputeStatus.CLOSED);
        dispute.setCloseTime(LocalDateTime.now());
        dispute.setCloseType(closeType);

        addProcessLog(dispute, "CLOSE", "Closed: " + reason);
        return disputeRepository.save(dispute);
    }

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private String generateDisputeNo() {
        return "DS" + System.currentTimeMillis() + String.format("%04d", SECURE_RANDOM.nextInt(10000));
    }

    private void addProcessLog(Dispute dispute, String action, String content) {
        try {
            List<Map<String, String>> logs = new ArrayList<>();
            if (dispute.getProcessLogs() != null && !dispute.getProcessLogs().isEmpty()) {
                logs = objectMapper.readValue(dispute.getProcessLogs(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
            }

            Map<String, String> logEntry = new HashMap<>();
            logEntry.put("action", action);
            logEntry.put("content", content);
            logEntry.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            logs.add(logEntry);
            dispute.setProcessLogs(objectMapper.writeValueAsString(logs));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse process logs", e);
        }
    }

    private void notifyDisputeParties(Dispute dispute, String title, String content) {
        try {
            notificationService.createNotification(dispute.getApplicantId(), 3, title, content, dispute.getId(), "DISPUTE");
            notificationService.createNotification(dispute.getRespondentId(), 3, title, content, dispute.getId(), "DISPUTE");
        } catch (Exception e) {
            log.warn("Failed to send dispute notification: disputeId={}", dispute.getId(), e.getMessage());
        }
    }
}
