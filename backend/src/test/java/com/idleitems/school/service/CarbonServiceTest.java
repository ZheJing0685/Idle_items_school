package com.idleitems.school.service;

import com.idleitems.school.common.event.OrderCompletedEvent;
import com.idleitems.school.module.carbon.entity.CarbonRecord;
import com.idleitems.school.module.carbon.repository.CarbonRecordRepository;
import com.idleitems.school.module.carbon.service.CarbonService;
import com.idleitems.school.module.category.entity.Category;
import com.idleitems.school.module.category.repository.CategoryRepository;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.order.entity.Order;
import com.idleitems.school.module.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarbonServiceTest {

    @Mock
    private CarbonRecordRepository carbonRecordRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private CarbonService carbonService;

    private OrderCompletedEvent testEvent;
    private Order testOrder;
    private Item testItem;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testEvent = new OrderCompletedEvent("source", 1L, 2L, 1L, "ORD001");
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setItemId(10L);
        testOrder.setBuyerId(1L);
        testOrder.setSellerId(2L);
        testOrder.setOrderNo("ORD001");

        testItem = new Item();
        testItem.setId(10L);
        testItem.setCategoryId(5L);

        testCategory = new Category();
        testCategory.setId(5L);
        testCategory.setCarbonSavingKg(new BigDecimal("15.00"));
    }

    @Test
    void recordCarbonSaving_WhenValid_SavesRecord() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(testItem));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(testCategory));
        when(carbonRecordRepository.save(any(CarbonRecord.class))).thenAnswer(i -> i.getArgument(0));

        carbonService.recordCarbonSaving(testEvent);

        ArgumentCaptor<CarbonRecord> captor = ArgumentCaptor.forClass(CarbonRecord.class);
        verify(carbonRecordRepository).save(captor.capture());
        CarbonRecord saved = captor.getValue();
        assertEquals(1L, saved.getOrderId());
        assertEquals(10L, saved.getItemId());
        assertEquals(1L, saved.getBuyerId());
        assertEquals(2L, saved.getSellerId());
        assertEquals(5L, saved.getCategoryId());
        assertEquals(0, new BigDecimal("15.00").compareTo(saved.getCarbonSavingKg()));
    }

    @Test
    void recordCarbonSaving_WhenOrderNotFound_LogsWarning() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        carbonService.recordCarbonSaving(testEvent);

        verify(carbonRecordRepository, never()).save(any(CarbonRecord.class));
    }

    @Test
    void recordCarbonSaving_WhenItemNotFound_LogsWarning() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());

        carbonService.recordCarbonSaving(testEvent);

        ArgumentCaptor<CarbonRecord> captor = ArgumentCaptor.forClass(CarbonRecord.class);
        verify(carbonRecordRepository).save(captor.capture());
        assertNull(captor.getValue().getCategoryId());
        assertEquals(0, BigDecimal.ZERO.compareTo(captor.getValue().getCarbonSavingKg()));
    }

    @Test
    void recordCarbonSaving_WhenExceptionCaught_DoesNotThrow() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(testItem));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(testCategory));
        when(carbonRecordRepository.save(any(CarbonRecord.class))).thenThrow(new RuntimeException("DB error"));

        assertDoesNotThrow(() -> carbonService.recordCarbonSaving(testEvent));
    }

    @Test
    void getMonthlyStats_ReturnsStatsWithTreeEquivalent() {
        BigDecimal monthlySaving = new BigDecimal("100.00");
        when(carbonRecordRepository.findTotalByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(monthlySaving);
        when(carbonRecordRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5L);
        when(carbonRecordRepository.countDistinctBuyerIdByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(3L);

        CarbonService.MonthlyStats stats = carbonService.getMonthlyStats();

        assertEquals(0, monthlySaving.compareTo(stats.getMonthlySavingKg()));
        assertEquals(5L, stats.getTransactionCount());
        assertEquals(3L, stats.getParticipantCount());
        assertEquals(5L, stats.getTreeEquivalent());
    }

    @Test
    void getMonthlyStats_WhenZeroSaving_ReturnsZeroTreeEquivalent() {
        when(carbonRecordRepository.findTotalByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(carbonRecordRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(carbonRecordRepository.countDistinctBuyerIdByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);

        CarbonService.MonthlyStats stats = carbonService.getMonthlyStats();

        assertEquals(0L, stats.getTreeEquivalent());
        assertEquals(0, BigDecimal.ZERO.compareTo(stats.getMonthlySavingKg()));
    }

    @Test
    void getTotalSavingKg_ReturnsTotal() {
        BigDecimal expectedTotal = new BigDecimal("500.00");
        when(carbonRecordRepository.findTotalAllTime()).thenReturn(expectedTotal);

        BigDecimal result = carbonService.getTotalSavingKg();

        assertEquals(0, expectedTotal.compareTo(result));
        verify(carbonRecordRepository, times(1)).findTotalAllTime();
    }
}
