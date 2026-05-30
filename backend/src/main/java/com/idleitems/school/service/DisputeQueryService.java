package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.entity.Dispute;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.DisputeRepository;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisputeQueryService {

    private final DisputeRepository disputeRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    private static final List<Dispute.DisputeStatus> ACTIVE_STATUSES = List.of(
            Dispute.DisputeStatus.PENDING,
            Dispute.DisputeStatus.ASSIGNED,
            Dispute.DisputeStatus.PROCESSING,
            Dispute.DisputeStatus.ESCALATED
    );

    public Page<Dispute> getMyDisputes(Long userId, Dispute.DisputeStatus status, Pageable pageable) {
        if (status != null) {
            return disputeRepository.findByApplicantIdAndDisputeStatusOrderByCreatedAtDesc(userId, status, pageable);
        }
        return disputeRepository.findByApplicantIdOrRespondentIdOrderByCreatedAtDesc(userId, userId, pageable);
    }

    public Dispute getDisputeById(Long disputeId, Long userId) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Dispute not found"));

        if (userId != null) {
            if (!dispute.getApplicantId().equals(userId) && !dispute.getRespondentId().equals(userId)) {
                User user = userRepository.findById(userId).orElse(null);
                if (user == null || user.getRole() != User.Role.ADMIN) {
                    throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "No permission");
                }
            }
        }
        return dispute;
    }

    public Page<Dispute> getAllDisputes(Dispute.DisputeStatus status, Pageable pageable) {
        if (status != null) {
            return disputeRepository.findByDisputeStatusOrderByCreatedAtDesc(status, pageable);
        }
        return disputeRepository.findAll(pageable);
    }

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
        return stats;
    }

    public Map<String, Object> canCreateDispute(Long orderId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("canDispute", false);
        result.put("reason", "");

        var order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            result.put("reason", "Order not found");
            return result;
        }

        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            result.put("reason", "Not order participant");
            return result;
        }

        List<com.idleitems.school.entity.Order.OrderStatus> canDisputeStatuses = Arrays.asList(
                com.idleitems.school.entity.Order.OrderStatus.SHIPPED,
                com.idleitems.school.entity.Order.OrderStatus.COMPLETED,
                com.idleitems.school.entity.Order.OrderStatus.REFUND_REQUESTED
        );

        if (!canDisputeStatuses.contains(order.getOrderStatus())) {
            result.put("reason", "Invalid order status");
            return result;
        }

        if (disputeRepository.existsByOrderIdAndDisputeStatusIn(orderId, ACTIVE_STATUSES)) {
            result.put("reason", "Active dispute exists");
            return result;
        }

        result.put("canDispute", true);
        result.put("orderStatus", order.getOrderStatus().name());
        result.put("orderAmount", order.getPrice());
        return result;
    }

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
}
