package com.idleitems.school.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.idleitems.school.entity.Dispute;
import com.idleitems.school.entity.Order;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.DisputeRepository;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.service.impl.DisputeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.databind.type.TypeFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DisputeService 单元测试")
class DisputeServiceTest {

    @Mock
    private DisputeRepository disputeRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private DisputeServiceImpl disputeService;

    private User testUser;
    private User testUser2;
    private Order testOrder;
    private Dispute testDispute;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole(User.Role.STUDENT);

        testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setUsername("testuser2");
        testUser2.setRole(User.Role.STUDENT);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNo("ORD20240101120000ABC12345");
        testOrder.setBuyerId(1L);
        testOrder.setSellerId(2L);
        testOrder.setItemId(10L);
        testOrder.setItemTitle("测试物品");
        testOrder.setPrice(new BigDecimal("99.99"));
        testOrder.setOrderStatus(Order.OrderStatus.SHIPPED);

        testDispute = new Dispute();
        testDispute.setId(1L);
        testDispute.setDisputeNo("DS1234567890");
        testDispute.setOrderId(1L);
        testDispute.setApplicantId(1L);
        testDispute.setRespondentId(2L);
        testDispute.setDisputeType(1);
        testDispute.setReason("商品质量问题");
        testDispute.setDescription("商品与描述不符");
        testDispute.setDisputeStatus(Dispute.DisputeStatus.PENDING);
        testDispute.setIsUrgent(false);
        testDispute.setPriority(1);
        testDispute.setIsEscalated(false);
        testDispute.setProcessLogs("[]");
    }

    private void mockObjectMapperForAddProcessLog() throws JsonProcessingException {
        lenient().when(objectMapper.getTypeFactory()).thenReturn(TypeFactory.defaultInstance());
        lenient().when(objectMapper.readValue(anyString(), any(JavaType.class)))
                .thenReturn(new ArrayList<>());
        lenient().when(objectMapper.writeValueAsString(any())).thenReturn("[]");
    }

    @Test
    @DisplayName("测试创建纠纷 - 成功")
    void testCreateDispute_Success() throws JsonProcessingException {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(disputeRepository.existsByOrderIdAndDisputeStatusIn(eq(1L), anyCollection())).thenReturn(false);
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        Dispute result = disputeService.createDispute(
                1L, 1L, 1, "商品质量问题", "商品与描述不符", null, "退款", new BigDecimal("99.99"));

        assertNotNull(result);
        assertEquals("商品质量问题", result.getReason());
        verify(orderRepository, times(1)).findById(1L);
        verify(disputeRepository, times(1)).existsByOrderIdAndDisputeStatusIn(eq(1L), anyCollection());
        verify(disputeRepository, times(1)).save(any(Dispute.class));
        verify(notificationService, times(1)).createNotification(
                eq(2L), eq(3), anyString(), anyString(), eq(1L), eq("DISPUTE"));
    }

    @Test
    @DisplayName("测试创建纠纷 - 订单不存在")
    void testCreateDispute_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> {
            disputeService.createDispute(
                    1L, 999L, 1, "商品质量问题", "商品与描述不符", null, "退款", new BigDecimal("99.99"));
        });
    }

    @Test
    @DisplayName("测试创建纠纷 - 无权发起纠纷")
    void testCreateDispute_NotAuthorized() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(Exception.class, () -> {
            disputeService.createDispute(
                    3L, 1L, 1, "商品质量问题", "商品与描述不符", null, "退款", new BigDecimal("99.99"));
        });
    }

    @Test
    @DisplayName("测试获取我的纠纷列表")
    void testGetMyDisputes() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Dispute> disputePage = new PageImpl<>(Arrays.asList(testDispute));
        when(disputeRepository.findByApplicantIdOrRespondentIdOrderByCreatedAtDesc(1L, 1L, pageable))
                .thenReturn(disputePage);

        Page<Dispute> result = disputeService.getMyDisputes(1L, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(disputeRepository, times(1)).findByApplicantIdOrRespondentIdOrderByCreatedAtDesc(1L, 1L, pageable);
    }

    @Test
    @DisplayName("测试获取纠纷详情 - 成功")
    void testGetDisputeById_Success() {
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));

        Dispute result = disputeService.getDisputeById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(disputeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("测试获取纠纷详情 - 纠纷不存在")
    void testGetDisputeById_NotFound() {
        when(disputeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> {
            disputeService.getDisputeById(999L, 1L);
        });
    }

    @Test
    @DisplayName("测试获取纠纷详情 - 无权查看")
    void testGetDisputeById_NotAuthorized() {
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));
        when(userRepository.findById(3L)).thenReturn(Optional.of(testUser));

        assertThrows(Exception.class, () -> {
            disputeService.getDisputeById(1L, 3L);
        });
    }

    @Test
    @DisplayName("测试回复纠纷 - 成功")
    void testReplyDispute_Success() throws JsonProcessingException {
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));
        mockObjectMapperForAddProcessLog();
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        Dispute result = disputeService.replyDispute(1L, 1L, "测试回复内容");

        assertNotNull(result);
        verify(disputeRepository, times(1)).findById(1L);
        verify(disputeRepository, times(1)).save(any(Dispute.class));
    }

    @Test
    @DisplayName("测试处理纠纷 - 同意退款")
    void testHandleDispute_ApproveRefund() throws JsonProcessingException {
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        mockObjectMapperForAddProcessLog();
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        Dispute result = disputeService.handleDispute(
                1L, 1L, "APPROVE_REFUND", "RESOLVED", new BigDecimal("99.99"), "同意退款");

        assertNotNull(result);
        verify(disputeRepository, times(1)).findById(1L);
        verify(disputeRepository, times(1)).save(any(Dispute.class));
    }

    @Test
    @DisplayName("测试处理纠纷 - 关闭纠纷")
    void testHandleDispute_Close() throws JsonProcessingException {
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));
        mockObjectMapperForAddProcessLog();
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        Dispute result = disputeService.handleDispute(
                1L, 1L, "CLOSE", "CLOSED", null, "管理员关闭");

        assertNotNull(result);
        verify(disputeRepository, times(1)).findById(1L);
        verify(disputeRepository, times(1)).save(any(Dispute.class));
    }

    @Test
    @DisplayName("测试分配纠纷 - 成功")
    void testAssignDispute_Success() throws JsonProcessingException {
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));
        mockObjectMapperForAddProcessLog();
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        Dispute result = disputeService.assignDispute(1L, 1L, 2);

        assertNotNull(result);
        verify(disputeRepository, times(1)).findById(1L);
        verify(disputeRepository, times(1)).save(any(Dispute.class));
    }

    @Test
    @DisplayName("测试开始处理纠纷 - 成功")
    void testStartProcess_Success() throws JsonProcessingException {
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));
        mockObjectMapperForAddProcessLog();
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        Dispute result = disputeService.startProcess(1L, 1L);

        assertNotNull(result);
        verify(disputeRepository, times(1)).findById(1L);
        verify(disputeRepository, times(1)).save(any(Dispute.class));
    }

    @Test
    @DisplayName("测试升级纠纷 - 成功")
    void testEscalateDispute_Success() throws JsonProcessingException {
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));
        mockObjectMapperForAddProcessLog();
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        Dispute result = disputeService.escalateDispute(1L, 1L, "需要更高级别处理");

        assertNotNull(result);
        verify(disputeRepository, times(1)).findById(1L);
        verify(disputeRepository, times(1)).save(any(Dispute.class));
    }

    @Test
    @DisplayName("测试提交满意度评价 - 成功")
    void testSubmitSatisfaction_Success() throws JsonProcessingException {
        testDispute.setDisputeStatus(Dispute.DisputeStatus.RESOLVED);
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));
        mockObjectMapperForAddProcessLog();
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        Dispute result = disputeService.submitSatisfaction(1L, 1L, 5, "服务很好");

        assertNotNull(result);
        verify(disputeRepository, times(1)).findById(1L);
        verify(disputeRepository, times(1)).save(any(Dispute.class));
    }

    @Test
    @DisplayName("测试提交满意度评价 - 无权评价")
    void testSubmitSatisfaction_NotAuthorized() {
        testDispute.setDisputeStatus(Dispute.DisputeStatus.RESOLVED);
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));

        assertThrows(Exception.class, () -> {
            disputeService.submitSatisfaction(1L, 3L, 5, "服务很好");
        });
    }

    @Test
    @DisplayName("测试提交满意度评价 - 纠纷未解决")
    void testSubmitSatisfaction_DisputeNotResolved() {
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));

        assertThrows(Exception.class, () -> {
            disputeService.submitSatisfaction(1L, 1L, 5, "服务很好");
        });
    }

    @Test
    @DisplayName("测试检查是否可以创建纠纷 - 可以创建")
    void testCanCreateDispute_CanDispute() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(disputeRepository.existsByOrderIdAndDisputeStatusIn(eq(1L), anyCollection())).thenReturn(false);

        Map<String, Object> result = disputeService.canCreateDispute(1L, 1L);

        assertNotNull(result);
        assertTrue((Boolean) result.get("canDispute"));
        assertEquals("SHIPPED", result.get("orderStatus"));
    }

    @Test
    @DisplayName("测试检查是否可以创建纠纷 - 订单不存在")
    void testCanCreateDispute_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        Map<String, Object> result = disputeService.canCreateDispute(999L, 1L);

        assertNotNull(result);
        assertFalse((Boolean) result.get("canDispute"));
        assertEquals("订单不存在", result.get("reason"));
    }

    @Test
    @DisplayName("测试检查是否可以创建纠纷 - 已有未处理纠纷")
    void testCanCreateDispute_HasActiveDispute() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(disputeRepository.existsByOrderIdAndDisputeStatusIn(eq(1L), anyCollection())).thenReturn(true);

        Map<String, Object> result = disputeService.canCreateDispute(1L, 1L);

        assertNotNull(result);
        assertFalse((Boolean) result.get("canDispute"));
        assertEquals("该订单已有未处理的纠纷", result.get("reason"));
    }

    @Test
    @DisplayName("测试获取订单的活跃纠纷")
    void testGetActiveDisputeByOrder() {
        when(disputeRepository.findActiveByOrderId(1L)).thenReturn(Arrays.asList(testDispute));

        Dispute result = disputeService.getActiveDisputeByOrder(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("测试获取订单的活跃纠纷 - 无活跃纠纷")
    void testGetActiveDisputeByOrder_NoActiveDispute() {
        when(disputeRepository.findActiveByOrderId(999L)).thenReturn(Collections.emptyList());

        Dispute result = disputeService.getActiveDisputeByOrder(999L, 1L);

        assertNull(result);
    }

    @Test
    @DisplayName("测试标记为紧急 - 成功")
    void testMarkAsUrgent_Success() throws JsonProcessingException {
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));
        mockObjectMapperForAddProcessLog();
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        Dispute result = disputeService.markAsUrgent(1L, true);

        assertNotNull(result);
        verify(disputeRepository, times(1)).findById(1L);
        verify(disputeRepository, times(1)).save(any(Dispute.class));
    }

    @Test
    @DisplayName("测试标记为非紧急")
    void testMarkAsNotUrgent() throws JsonProcessingException {
        testDispute.setIsUrgent(true);
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));
        mockObjectMapperForAddProcessLog();
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        Dispute result = disputeService.markAsUrgent(1L, false);

        assertNotNull(result);
        verify(disputeRepository, times(1)).findById(1L);
        verify(disputeRepository, times(1)).save(any(Dispute.class));
    }

    @Test
    @DisplayName("测试关闭纠纷 - 成功")
    void testCloseDispute_Success() throws JsonProcessingException {
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));
        mockObjectMapperForAddProcessLog();
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        Dispute result = disputeService.closeDispute(1L, 1L, 1, "用户主动关闭");

        assertNotNull(result);
        verify(disputeRepository, times(1)).findById(1L);
        verify(disputeRepository, times(1)).save(any(Dispute.class));
    }

    @Test
    @DisplayName("测试获取纠纷统计")
    void testGetDisputeStats() {
        when(disputeRepository.count()).thenReturn(10L);
        when(disputeRepository.countByDisputeStatus(Dispute.DisputeStatus.PENDING)).thenReturn(3L);
        when(disputeRepository.countByDisputeStatus(Dispute.DisputeStatus.ASSIGNED)).thenReturn(2L);
        when(disputeRepository.countByDisputeStatus(Dispute.DisputeStatus.PROCESSING)).thenReturn(1L);
        when(disputeRepository.countByIsEscalatedTrue()).thenReturn(1L);
        when(disputeRepository.countByDisputeStatus(Dispute.DisputeStatus.RESOLVED)).thenReturn(2L);
        when(disputeRepository.countByDisputeStatus(Dispute.DisputeStatus.CLOSED)).thenReturn(1L);
        when(disputeRepository.countUrgentPending()).thenReturn(1L);
        when(disputeRepository.countByPriorityAndDisputeStatus(3, Dispute.DisputeStatus.PENDING)).thenReturn(1L);
        when(disputeRepository.countByPriorityAndDisputeStatus(2, Dispute.DisputeStatus.PENDING)).thenReturn(1L);
        when(disputeRepository.countByPriorityAndDisputeStatus(1, Dispute.DisputeStatus.PENDING)).thenReturn(1L);

        Map<String, Object> result = disputeService.getDisputeStats();

        assertNotNull(result);
        assertEquals(10L, result.get("total"));
        assertEquals(3L, result.get("pending"));
        assertEquals(2L, result.get("assigned"));
        assertEquals(1L, result.get("processing"));
        assertEquals(1L, result.get("escalated"));
        assertEquals(2L, result.get("resolved"));
        assertEquals(1L, result.get("closed"));
        assertEquals(1L, result.get("urgent"));
    }

    @Test
    @DisplayName("测试获取所有纠纷 - 带状态过滤")
    void testGetAllDisputes_WithStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Dispute> disputePage = new PageImpl<>(Arrays.asList(testDispute));
        when(disputeRepository.findByDisputeStatusOrderByCreatedAtDesc(Dispute.DisputeStatus.PENDING, pageable))
                .thenReturn(disputePage);

        Page<Dispute> result = disputeService.getAllDisputes(Dispute.DisputeStatus.PENDING, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("测试获取所有纠纷 - 无状态过滤")
    void testGetAllDisputes_NoStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Dispute> disputePage = new PageImpl<>(Arrays.asList(testDispute));
        when(disputeRepository.findAll(pageable)).thenReturn(disputePage);

        Page<Dispute> result = disputeService.getAllDisputes(null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("测试获取我的纠纷列表 - 带状态过滤")
    void testGetMyDisputes_WithStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Dispute> disputePage = new PageImpl<>(Arrays.asList(testDispute));
        when(disputeRepository.findByApplicantIdAndDisputeStatusOrderByCreatedAtDesc(1L, Dispute.DisputeStatus.PENDING, pageable))
                .thenReturn(disputePage);

        Page<Dispute> result = disputeService.getMyDisputes(1L, Dispute.DisputeStatus.PENDING, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }
}