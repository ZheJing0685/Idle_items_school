package com.idleitems.school.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idleitems.school.common.BusinessException;
import com.idleitems.school.module.dispute.entity.Dispute;
import com.idleitems.school.module.dispute.repository.DisputeRepository;
import com.idleitems.school.module.dispute.service.DisputeCommandService;
import com.idleitems.school.module.notification.service.NotificationService;
import com.idleitems.school.module.order.entity.Order;
import com.idleitems.school.module.user.repository.UserRepository;
import com.idleitems.school.module.order.repository.OrderRepository;
import com.idleitems.school.util.SensitiveWordFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisputeCommandServiceTest {

    @Mock
    private DisputeRepository disputeRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DisputeCommandService disputeCommandService;

    private Order testOrder;
    private Dispute testDispute;
    private static final List<Dispute.DisputeStatus> ACTIVE_STATUSES = List.of(
            Dispute.DisputeStatus.PENDING,
            Dispute.DisputeStatus.ASSIGNED,
            Dispute.DisputeStatus.PROCESSING,
            Dispute.DisputeStatus.ESCALATED
    );

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setBuyerId(1L);
        testOrder.setSellerId(2L);
        testOrder.setPrice(new BigDecimal("100.00"));
        testOrder.setOrderStatus(Order.OrderStatus.COMPLETED);

        testDispute = new Dispute();
        testDispute.setId(1L);
        testDispute.setDisputeNo("DS000001TEST");
        testDispute.setOrderId(1L);
        testDispute.setApplicantId(1L);
        testDispute.setRespondentId(2L);
        testDispute.setDisputeStatus(Dispute.DisputeStatus.PENDING);
        testDispute.setReason("Test reason");
        testDispute.setDescription("Test description");
        testDispute.setDisputeType(1);
    }

    @Test
    void createDispute_WhenValidRequest_CreatesDispute() {
        try (MockedStatic<SensitiveWordFilter> sensitiveWordFilter = mockStatic(SensitiveWordFilter.class)) {
            sensitiveWordFilter.when(() -> SensitiveWordFilter.findSensitiveWords(anyString())).thenReturn(List.of());
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(disputeRepository.existsByOrderIdAndDisputeStatusIn(1L, ACTIVE_STATUSES)).thenReturn(false);
            when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);
            when(userRepository.findByRole(any(), any())).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));

            Dispute result = disputeCommandService.createDispute(1L, 1L, 1, "Test reason", "Test description",
                    "img1.jpg", "REFUND", new BigDecimal("50.00"));

            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(disputeRepository, times(1)).save(any(Dispute.class));
            verify(notificationService, times(1)).createNotification(eq(2L), eq(3), anyString(), anyString(), eq(1L), eq("DISPUTE"));
        }
    }

    @Test
    void createDispute_WhenSensitiveWords_ThrowsException() {
        try (MockedStatic<SensitiveWordFilter> sensitiveWordFilter = mockStatic(SensitiveWordFilter.class)) {
            sensitiveWordFilter.when(() -> SensitiveWordFilter.findSensitiveWords(anyString())).thenReturn(List.of("badword"));
            sensitiveWordFilter.when(() -> SensitiveWordFilter.getWarningMessage(anyList())).thenReturn("Warning: badword");

            assertThrows(BusinessException.class, () ->
                    disputeCommandService.createDispute(1L, 1L, 1, "badword", null, null, null, null));

            verify(disputeRepository, never()).save(any());
        }
    }

    @Test
    void createDispute_WhenOrderNotFound_ThrowsException() {
        try (MockedStatic<SensitiveWordFilter> sensitiveWordFilter = mockStatic(SensitiveWordFilter.class)) {
            sensitiveWordFilter.when(() -> SensitiveWordFilter.findSensitiveWords(anyString())).thenReturn(List.of());
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(BusinessException.class, () ->
                    disputeCommandService.createDispute(1L, 999L, 1, "Test reason", null, null, null, null));
        }
    }

    @Test
    void createDispute_WhenNoPermission_ThrowsException() {
        try (MockedStatic<SensitiveWordFilter> sensitiveWordFilter = mockStatic(SensitiveWordFilter.class)) {
            sensitiveWordFilter.when(() -> SensitiveWordFilter.findSensitiveWords(anyString())).thenReturn(List.of());
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThrows(BusinessException.class, () ->
                    disputeCommandService.createDispute(3L, 1L, 1, "Test reason", null, null, null, null));
        }
    }

    @Test
    void createDispute_WhenActiveDisputeExists_ThrowsException() {
        try (MockedStatic<SensitiveWordFilter> sensitiveWordFilter = mockStatic(SensitiveWordFilter.class)) {
            sensitiveWordFilter.when(() -> SensitiveWordFilter.findSensitiveWords(anyString())).thenReturn(List.of());
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(disputeRepository.existsByOrderIdAndDisputeStatusIn(1L, ACTIVE_STATUSES)).thenReturn(true);

            assertThrows(BusinessException.class, () ->
                    disputeCommandService.createDispute(1L, 1L, 1, "Test reason", null, null, null, null));
        }
    }

    @Test
    void replyDispute_WhenValidRequest_Replies() throws JsonProcessingException {
        try (MockedStatic<SensitiveWordFilter> sensitiveWordFilter = mockStatic(SensitiveWordFilter.class)) {
            sensitiveWordFilter.when(() -> SensitiveWordFilter.findSensitiveWords(anyString())).thenReturn(List.of());
            when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));
            doReturn("[]").when(objectMapper).writeValueAsString(any());
            when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

            Dispute result = disputeCommandService.replyDispute(1L, 1L, "I want a refund");

            assertNotNull(result);
            verify(disputeRepository, times(1)).save(any(Dispute.class));
            verify(notificationService, times(1)).createNotification(eq(2L), eq(3), anyString(), anyString(), eq(1L), eq("DISPUTE"));
        }
    }

    @Test
    void replyDispute_WhenSensitiveWords_ThrowsException() {
        try (MockedStatic<SensitiveWordFilter> sensitiveWordFilter = mockStatic(SensitiveWordFilter.class)) {
            sensitiveWordFilter.when(() -> SensitiveWordFilter.findSensitiveWords(anyString())).thenReturn(List.of("badword"));
            sensitiveWordFilter.when(() -> SensitiveWordFilter.getWarningMessage(anyList())).thenReturn("Warning: badword");

            assertThrows(BusinessException.class, () ->
                    disputeCommandService.replyDispute(1L, 1L, "badword"));
        }
    }

    @Test
    void replyDispute_WhenDisputeNotFound_ThrowsException() {
        try (MockedStatic<SensitiveWordFilter> sensitiveWordFilter = mockStatic(SensitiveWordFilter.class)) {
            sensitiveWordFilter.when(() -> SensitiveWordFilter.findSensitiveWords(anyString())).thenReturn(List.of());
            when(disputeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(BusinessException.class, () ->
                    disputeCommandService.replyDispute(999L, 1L, "content"));
        }
    }

    @Test
    void replyDispute_WhenNoPermission_ThrowsException() {
        try (MockedStatic<SensitiveWordFilter> sensitiveWordFilter = mockStatic(SensitiveWordFilter.class)) {
            sensitiveWordFilter.when(() -> SensitiveWordFilter.findSensitiveWords(anyString())).thenReturn(List.of());
            when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));

            assertThrows(BusinessException.class, () ->
                    disputeCommandService.replyDispute(1L, 999L, "content"));
        }
    }

    @Test
    void replyDispute_WhenPendingStatus_AutoSetsProcessing() throws JsonProcessingException {
        try (MockedStatic<SensitiveWordFilter> sensitiveWordFilter = mockStatic(SensitiveWordFilter.class)) {
            sensitiveWordFilter.when(() -> SensitiveWordFilter.findSensitiveWords(anyString())).thenReturn(List.of());

            Dispute capturedDispute = new Dispute();
            capturedDispute.setId(1L);
            capturedDispute.setApplicantId(1L);
            capturedDispute.setRespondentId(2L);
            capturedDispute.setDisputeStatus(Dispute.DisputeStatus.PENDING);

            when(disputeRepository.findById(1L)).thenReturn(Optional.of(capturedDispute));
            doReturn("[]").when(objectMapper).writeValueAsString(any());
            when(disputeRepository.save(any(Dispute.class))).thenAnswer(invocation -> invocation.getArgument(0));

            disputeCommandService.replyDispute(1L, 2L, "I disagree");

            assertEquals(Dispute.DisputeStatus.PROCESSING, capturedDispute.getDisputeStatus());
        }
    }

    @Test
    void handleDispute_WhenApproveRefund_ResolvesAndRefunds() throws JsonProcessingException {
        Dispute processingDispute = new Dispute();
        processingDispute.setId(1L);
        processingDispute.setOrderId(1L);
        processingDispute.setApplicantId(1L);
        processingDispute.setRespondentId(2L);
        processingDispute.setDisputeStatus(Dispute.DisputeStatus.PROCESSING);

        when(disputeRepository.findById(1L)).thenReturn(Optional.of(processingDispute));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        doReturn("[]").when(objectMapper).writeValueAsString(any());
        when(disputeRepository.save(any(Dispute.class))).thenReturn(processingDispute);

        Dispute result = disputeCommandService.handleDispute(1L, 3L, "APPROVE_REFUND", "RESOLVED", null, "Full refund");

        assertEquals(Dispute.DisputeStatus.RESOLVED, result.getDisputeStatus());
        assertEquals(3L, result.getHandlerId());
        assertEquals(new BigDecimal("100.00"), processingDispute.getActualRefundAmount());
        assertEquals(Order.OrderStatus.REFUNDED, testOrder.getOrderStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(notificationService, times(2)).createNotification(anyLong(), eq(3), anyString(), anyString(), eq(1L), eq("DISPUTE"));
    }

    @Test
    void handleDispute_WhenPartialRefundWithValidAmount_Resolves() throws JsonProcessingException {
        Dispute processingDispute = new Dispute();
        processingDispute.setId(1L);
        processingDispute.setOrderId(1L);
        processingDispute.setApplicantId(1L);
        processingDispute.setRespondentId(2L);
        processingDispute.setDisputeStatus(Dispute.DisputeStatus.PROCESSING);

        when(disputeRepository.findById(1L)).thenReturn(Optional.of(processingDispute));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        doReturn("[]").when(objectMapper).writeValueAsString(any());
        when(disputeRepository.save(any(Dispute.class))).thenReturn(processingDispute);

        Dispute result = disputeCommandService.handleDispute(1L, 3L, "PARTIAL_REFUND", "RESOLVED", new BigDecimal("50.00"), "Partial");

        assertEquals(Dispute.DisputeStatus.RESOLVED, result.getDisputeStatus());
        assertEquals(new BigDecimal("50.00"), processingDispute.getActualRefundAmount());
        assertEquals(new BigDecimal("50.00"), testOrder.getRefundAmount());
        assertEquals(Order.OrderStatus.REFUNDED, testOrder.getOrderStatus());
    }

    @Test
    void handleDispute_WhenPartialRefundWithNullAmount_ThrowsException() {
        Dispute processingDispute = new Dispute();
        processingDispute.setId(1L);
        processingDispute.setOrderId(1L);
        processingDispute.setApplicantId(1L);
        processingDispute.setRespondentId(2L);
        processingDispute.setDisputeStatus(Dispute.DisputeStatus.PROCESSING);

        when(disputeRepository.findById(1L)).thenReturn(Optional.of(processingDispute));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(BusinessException.class, () ->
                disputeCommandService.handleDispute(1L, 3L, "PARTIAL_REFUND", "RESOLVED", null, "Partial"));
    }

    @Test
    void handleDispute_WhenPartialRefundExceedsPrice_ThrowsException() {
        Dispute processingDispute = new Dispute();
        processingDispute.setId(1L);
        processingDispute.setOrderId(1L);
        processingDispute.setApplicantId(1L);
        processingDispute.setRespondentId(2L);
        processingDispute.setDisputeStatus(Dispute.DisputeStatus.PROCESSING);

        when(disputeRepository.findById(1L)).thenReturn(Optional.of(processingDispute));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(BusinessException.class, () ->
                disputeCommandService.handleDispute(1L, 3L, "PARTIAL_REFUND", "RESOLVED", new BigDecimal("200.00"), "Partial"));
    }

    @Test
    void handleDispute_WhenClosed_ClosesDispute() throws JsonProcessingException {
        Dispute processingDispute = new Dispute();
        processingDispute.setId(1L);
        processingDispute.setOrderId(1L);
        processingDispute.setApplicantId(1L);
        processingDispute.setRespondentId(2L);
        processingDispute.setDisputeStatus(Dispute.DisputeStatus.PROCESSING);

        when(disputeRepository.findById(1L)).thenReturn(Optional.of(processingDispute));
        doReturn("[]").when(objectMapper).writeValueAsString(any());
        when(disputeRepository.save(any(Dispute.class))).thenReturn(processingDispute);

        Dispute result = disputeCommandService.handleDispute(1L, 3L, "REJECT", "CLOSED", null, "No issue");

        assertEquals(Dispute.DisputeStatus.CLOSED, result.getDisputeStatus());
        assertNotNull(processingDispute.getCloseTime());
        assertEquals(Dispute.CloseType.ADMIN_CLOSE.getValue(), processingDispute.getCloseType());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void assignDispute_WhenValid_AssignsHandler() throws JsonProcessingException {
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));
        doReturn("[]").when(objectMapper).writeValueAsString(any());
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        Dispute result = disputeCommandService.assignDispute(1L, 3L, 2);

        assertEquals(Dispute.DisputeStatus.ASSIGNED, result.getDisputeStatus());
        assertEquals(3L, result.getHandlerId());
        assertEquals(2, result.getPriority());
        assertNotNull(result.getAssignTime());
        verify(disputeRepository, times(1)).save(any(Dispute.class));
    }

    @Test
    void assignDispute_WhenNotFound_ThrowsException() {
        when(disputeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                disputeCommandService.assignDispute(999L, 3L, 1));
    }

    @Test
    void startProcess_WhenValid_StartsProcessing() throws JsonProcessingException {
        Dispute assignedDispute = new Dispute();
        assignedDispute.setId(1L);
        assignedDispute.setHandlerId(3L);
        assignedDispute.setDisputeStatus(Dispute.DisputeStatus.ASSIGNED);

        when(disputeRepository.findById(1L)).thenReturn(Optional.of(assignedDispute));
        doReturn("[]").when(objectMapper).writeValueAsString(any());
        when(disputeRepository.save(any(Dispute.class))).thenReturn(assignedDispute);

        Dispute result = disputeCommandService.startProcess(1L, 3L);

        assertEquals(Dispute.DisputeStatus.PROCESSING, result.getDisputeStatus());
        assertEquals(3L, result.getHandlerId());
        assertNotNull(result.getStartProcessTime());
    }

    @Test
    void startProcess_WhenHandlerIdNull_AutoSetsHandlerId() throws JsonProcessingException {
        Dispute unassignedDispute = new Dispute();
        unassignedDispute.setId(1L);
        unassignedDispute.setHandlerId(null);
        unassignedDispute.setDisputeStatus(Dispute.DisputeStatus.PENDING);

        when(disputeRepository.findById(1L)).thenReturn(Optional.of(unassignedDispute));
        doReturn("[]").when(objectMapper).writeValueAsString(any());
        when(disputeRepository.save(any(Dispute.class))).thenReturn(unassignedDispute);

        Dispute result = disputeCommandService.startProcess(1L, 5L);

        assertEquals(Dispute.DisputeStatus.PROCESSING, result.getDisputeStatus());
        assertEquals(5L, result.getHandlerId());
    }

    @Test
    void startProcess_WhenDisputeNotFound_ThrowsException() {
        when(disputeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                disputeCommandService.startProcess(999L, 3L));
    }

    @Test
    void escalateDispute_WhenValid_Escalates() throws JsonProcessingException {
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));
        doReturn("[]").when(objectMapper).writeValueAsString(any());
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        Dispute result = disputeCommandService.escalateDispute(1L, 4L, "Need supervisor review");

        assertTrue(result.getIsEscalated());
        assertEquals(4L, result.getEscalatedTo());
        assertEquals(Dispute.DisputeStatus.ESCALATED, result.getDisputeStatus());
        assertNotNull(result.getEscalatedTime());
        assertEquals("Need supervisor review", result.getEscalatedReason());
    }

    @Test
    void escalateDispute_WhenDisputeNotFound_ThrowsException() {
        when(disputeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                disputeCommandService.escalateDispute(999L, 4L, "Need review"));
    }

    @Test
    void submitSatisfaction_WhenValid_SubmitsRating() throws JsonProcessingException {
        Dispute resolvedDispute = new Dispute();
        resolvedDispute.setId(1L);
        resolvedDispute.setApplicantId(1L);
        resolvedDispute.setRespondentId(2L);
        resolvedDispute.setDisputeStatus(Dispute.DisputeStatus.RESOLVED);

        when(disputeRepository.findById(1L)).thenReturn(Optional.of(resolvedDispute));
        doReturn("[]").when(objectMapper).writeValueAsString(any());
        when(disputeRepository.save(any(Dispute.class))).thenReturn(resolvedDispute);

        Dispute result = disputeCommandService.submitSatisfaction(1L, 1L, 5, "Great service");

        assertEquals(5, result.getSatisfaction());
        assertEquals("Great service", result.getSatisfactionRemark());
    }

    @Test
    void submitSatisfaction_WhenNoPermission_ThrowsException() {
        Dispute resolvedDispute = new Dispute();
        resolvedDispute.setId(1L);
        resolvedDispute.setApplicantId(1L);
        resolvedDispute.setRespondentId(2L);
        resolvedDispute.setDisputeStatus(Dispute.DisputeStatus.RESOLVED);

        when(disputeRepository.findById(1L)).thenReturn(Optional.of(resolvedDispute));

        assertThrows(BusinessException.class, () ->
                disputeCommandService.submitSatisfaction(1L, 999L, 5, "Great"));
    }

    @Test
    void submitSatisfaction_WhenWrongStatus_ThrowsException() {
        Dispute pendingDispute = new Dispute();
        pendingDispute.setId(1L);
        pendingDispute.setApplicantId(1L);
        pendingDispute.setRespondentId(2L);
        pendingDispute.setDisputeStatus(Dispute.DisputeStatus.PENDING);

        when(disputeRepository.findById(1L)).thenReturn(Optional.of(pendingDispute));

        assertThrows(BusinessException.class, () ->
                disputeCommandService.submitSatisfaction(1L, 1L, 5, "Great"));
    }

    @Test
    void submitSatisfaction_WhenDisputeNotFound_ThrowsException() {
        when(disputeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                disputeCommandService.submitSatisfaction(999L, 1L, 5, "Great"));
    }

    @Test
    void markAsUrgent_WhenValid_MarksUrgent() throws JsonProcessingException {
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));
        doReturn("[]").when(objectMapper).writeValueAsString(any());
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        Dispute result = disputeCommandService.markAsUrgent(1L, true);

        assertTrue(result.getIsUrgent());
        assertEquals(4, result.getPriority());
        verify(disputeRepository, times(1)).save(any(Dispute.class));
    }

    @Test
    void closeDispute_WhenValid_ClosesDispute() throws JsonProcessingException {
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));
        doReturn("[]").when(objectMapper).writeValueAsString(any());
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        Dispute result = disputeCommandService.closeDispute(1L, 2L, Dispute.CloseType.USER_WITHDRAW.getValue(), "User withdrew");

        assertEquals(Dispute.DisputeStatus.CLOSED, result.getDisputeStatus());
        assertNotNull(result.getCloseTime());
        assertEquals(Dispute.CloseType.USER_WITHDRAW.getValue(), result.getCloseType());
        verify(disputeRepository, times(1)).save(any(Dispute.class));
    }
}
