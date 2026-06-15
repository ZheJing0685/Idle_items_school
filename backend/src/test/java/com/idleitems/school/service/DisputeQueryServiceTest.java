package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.module.dispute.entity.Dispute;
import com.idleitems.school.module.dispute.repository.DisputeRepository;
import com.idleitems.school.module.dispute.service.DisputeQueryService;
import com.idleitems.school.module.order.entity.Order;
import com.idleitems.school.module.order.repository.OrderRepository;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisputeQueryServiceTest {

    @Mock
    private DisputeRepository disputeRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DisputeQueryService disputeQueryService;

    private Order testOrder;
    private Dispute testDispute;
    private User testAdmin;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = Pageable.unpaged();

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

        testAdmin = new User();
        testAdmin.setId(3L);
        testAdmin.setRole(User.Role.ADMIN);
    }

    @Test
    void getMyDisputes_WhenStatusProvided_ReturnsFilteredByApplicantAndStatus() {
        Page<Dispute> expectedPage = new PageImpl<>(List.of(testDispute));
        when(disputeRepository.findByApplicantIdAndDisputeStatusOrderByCreatedAtDesc(1L, Dispute.DisputeStatus.PENDING, pageable))
                .thenReturn(expectedPage);

        Page<Dispute> result = disputeQueryService.getMyDisputes(1L, Dispute.DisputeStatus.PENDING, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(testDispute, result.getContent().get(0));
        verify(disputeRepository, times(1))
                .findByApplicantIdAndDisputeStatusOrderByCreatedAtDesc(1L, Dispute.DisputeStatus.PENDING, pageable);
        verify(disputeRepository, never())
                .findByApplicantIdOrRespondentIdOrderByCreatedAtDesc(anyLong(), anyLong(), any());
    }

    @Test
    void getMyDisputes_WhenStatusNull_ReturnsByApplicantOrRespondent() {
        Page<Dispute> expectedPage = new PageImpl<>(List.of(testDispute));
        when(disputeRepository.findByApplicantIdOrRespondentIdOrderByCreatedAtDesc(1L, 1L, pageable))
                .thenReturn(expectedPage);

        Page<Dispute> result = disputeQueryService.getMyDisputes(1L, null, pageable);

        assertEquals(1, result.getContent().size());
        verify(disputeRepository, times(1))
                .findByApplicantIdOrRespondentIdOrderByCreatedAtDesc(1L, 1L, pageable);
    }

    @Test
    void getDisputeById_WhenOwnDispute_ReturnsDispute() {
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));

        Dispute result = disputeQueryService.getDisputeById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getDisputeById_WhenNotParticipantAndNotAdmin_ThrowsException() {
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                disputeQueryService.getDisputeById(1L, 5L));
    }

    @Test
    void getDisputeById_WhenUserIsAdmin_ReturnsDispute() {
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));
        when(userRepository.findById(3L)).thenReturn(Optional.of(testAdmin));

        Dispute result = disputeQueryService.getDisputeById(1L, 3L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getDisputeById_WhenUserIdNull_ReturnsDisputeWithoutPermissionCheck() {
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));

        Dispute result = disputeQueryService.getDisputeById(1L, null);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void getDisputeById_WhenNotFound_ThrowsException() {
        when(disputeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                disputeQueryService.getDisputeById(999L, 1L));
    }

    @Test
    void getAllDisputes_WhenStatusProvided_ReturnsFiltered() {
        Page<Dispute> expectedPage = new PageImpl<>(List.of(testDispute));
        when(disputeRepository.findByDisputeStatusOrderByCreatedAtDesc(Dispute.DisputeStatus.PENDING, pageable))
                .thenReturn(expectedPage);

        Page<Dispute> result = disputeQueryService.getAllDisputes(Dispute.DisputeStatus.PENDING, pageable);

        assertEquals(1, result.getContent().size());
        verify(disputeRepository, times(1))
                .findByDisputeStatusOrderByCreatedAtDesc(Dispute.DisputeStatus.PENDING, pageable);
    }

    @Test
    void getAllDisputes_WhenStatusNull_ReturnsAll() {
        Page<Dispute> expectedPage = new PageImpl<>(List.of(testDispute));
        when(disputeRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<Dispute> result = disputeQueryService.getAllDisputes(null, pageable);

        assertEquals(1, result.getContent().size());
        verify(disputeRepository, times(1)).findAll(pageable);
    }

    @Test
    void getDisputeStats_ReturnsAllCounts() {
        when(disputeRepository.count()).thenReturn(10L);
        when(disputeRepository.countByDisputeStatus(Dispute.DisputeStatus.PENDING)).thenReturn(3L);
        when(disputeRepository.countByDisputeStatus(Dispute.DisputeStatus.ASSIGNED)).thenReturn(2L);
        when(disputeRepository.countByDisputeStatus(Dispute.DisputeStatus.PROCESSING)).thenReturn(1L);
        when(disputeRepository.countByIsEscalatedTrue()).thenReturn(1L);
        when(disputeRepository.countByDisputeStatus(Dispute.DisputeStatus.RESOLVED)).thenReturn(2L);
        when(disputeRepository.countByDisputeStatus(Dispute.DisputeStatus.CLOSED)).thenReturn(1L);
        when(disputeRepository.countUrgentPending()).thenReturn(1L);

        Map<String, Object> stats = disputeQueryService.getDisputeStats();

        assertEquals(10L, stats.get("total"));
        assertEquals(3L, stats.get("pending"));
        assertEquals(2L, stats.get("assigned"));
        assertEquals(1L, stats.get("processing"));
        assertEquals(1L, stats.get("escalated"));
        assertEquals(2L, stats.get("resolved"));
        assertEquals(1L, stats.get("closed"));
        assertEquals(1L, stats.get("urgent"));
        verify(disputeRepository, times(1)).count();
        verify(disputeRepository, times(5)).countByDisputeStatus(any());
    }

    @Test
    void canCreateDispute_WhenEligible_ReturnsCanDisputeTrue() {
        testOrder.setOrderStatus(Order.OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(disputeRepository.existsByOrderIdAndDisputeStatusIn(eq(1L), anyList())).thenReturn(false);

        Map<String, Object> result = disputeQueryService.canCreateDispute(1L, 1L);

        assertTrue((Boolean) result.get("canDispute"));
        assertEquals("SHIPPED", result.get("orderStatus"));
        assertEquals(new BigDecimal("100.00"), result.get("orderAmount"));
    }

    @Test
    void canCreateDispute_WhenOrderNotFound_ReturnsCannotDispute() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        Map<String, Object> result = disputeQueryService.canCreateDispute(999L, 1L);

        assertFalse((Boolean) result.get("canDispute"));
        assertEquals("Order not found", result.get("reason"));
    }

    @Test
    void canCreateDispute_WhenNotParticipant_ReturnsCannotDispute() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        Map<String, Object> result = disputeQueryService.canCreateDispute(1L, 5L);

        assertFalse((Boolean) result.get("canDispute"));
        assertEquals("Not order participant", result.get("reason"));
    }

    @Test
    void canCreateDispute_WhenInvalidOrderStatus_ReturnsCannotDispute() {
        testOrder.setOrderStatus(Order.OrderStatus.PENDING_PAYMENT);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        Map<String, Object> result = disputeQueryService.canCreateDispute(1L, 1L);

        assertFalse((Boolean) result.get("canDispute"));
        assertEquals("Invalid order status", result.get("reason"));
    }

    @Test
    void canCreateDispute_WhenActiveDisputeExists_ReturnsCannotDispute() {
        testOrder.setOrderStatus(Order.OrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(disputeRepository.existsByOrderIdAndDisputeStatusIn(eq(1L), anyList())).thenReturn(true);

        Map<String, Object> result = disputeQueryService.canCreateDispute(1L, 1L);

        assertFalse((Boolean) result.get("canDispute"));
        assertEquals("Active dispute exists", result.get("reason"));
    }

    @Test
    void getActiveDisputeByOrder_WhenActiveExists_ReturnsFirst() {
        when(disputeRepository.findActiveByOrderId(1L)).thenReturn(List.of(testDispute));

        Dispute result = disputeQueryService.getActiveDisputeByOrder(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getActiveDisputeByOrder_WhenNoActiveDisputes_ReturnsNull() {
        when(disputeRepository.findActiveByOrderId(1L)).thenReturn(List.of());

        Dispute result = disputeQueryService.getActiveDisputeByOrder(1L, 1L);

        assertNull(result);
    }

    @Test
    void getActiveDisputeByOrder_WhenNotParticipant_ReturnsNull() {
        when(disputeRepository.findActiveByOrderId(1L)).thenReturn(List.of(testDispute));

        Dispute result = disputeQueryService.getActiveDisputeByOrder(1L, 5L);

        assertNull(result);
    }
}
