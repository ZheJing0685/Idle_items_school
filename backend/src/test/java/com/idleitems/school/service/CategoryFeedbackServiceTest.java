package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.module.category.entity.CategoryFeedback;
import com.idleitems.school.module.category.repository.CategoryFeedbackRepository;
import com.idleitems.school.module.category.service.CategoryFeedbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryFeedbackServiceTest {

    @Mock
    private CategoryFeedbackRepository categoryFeedbackRepository;

    @InjectMocks
    private CategoryFeedbackService categoryFeedbackService;

    private CategoryFeedback testFeedback;

    @BeforeEach
    void setUp() {
        testFeedback = new CategoryFeedback();
        testFeedback.setId(1L);
        testFeedback.setUserId(1L);
        testFeedback.setFeedbackType(CategoryFeedback.FeedbackType.MISSING);
        testFeedback.setCategoryId(1L);
        testFeedback.setDescription("缺少分类");
        testFeedback.setStatus(CategoryFeedback.FeedbackStatus.PENDING);
    }

    @Test
    void submitFeedback_WhenValidRequest_SavesFeedback() {
        categoryFeedbackService.submitFeedback(1L, "MISSING", 1L, "缺少分类");

        verify(categoryFeedbackRepository, times(1)).save(any(CategoryFeedback.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getMyFeedbacks_DelegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CategoryFeedback> mockPage = mock(Page.class);
        when(categoryFeedbackRepository.findByUserIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(mockPage);

        Page<CategoryFeedback> result = categoryFeedbackService.getMyFeedbacks(1L, pageable);

        assertSame(mockPage, result);
        verify(categoryFeedbackRepository, times(1)).findByUserIdOrderByCreatedAtDesc(1L, pageable);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllFeedbacks_WithStatusFilter_FiltersByStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CategoryFeedback> mockPage = mock(Page.class);
        when(categoryFeedbackRepository.findByStatus(CategoryFeedback.FeedbackStatus.PENDING, pageable)).thenReturn(mockPage);

        Page<CategoryFeedback> result = categoryFeedbackService.getAllFeedbacks("PENDING", pageable);

        assertSame(mockPage, result);
        verify(categoryFeedbackRepository, times(1)).findByStatus(CategoryFeedback.FeedbackStatus.PENDING, pageable);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllFeedbacks_WithoutStatusFilter_ReturnsAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CategoryFeedback> mockPage = mock(Page.class);
        when(categoryFeedbackRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(mockPage);

        Page<CategoryFeedback> result = categoryFeedbackService.getAllFeedbacks(null, pageable);

        assertSame(mockPage, result);
        verify(categoryFeedbackRepository, times(1)).findAllByOrderByCreatedAtDesc(pageable);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllFeedbacks_WithEmptyStatus_ReturnsAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CategoryFeedback> mockPage = mock(Page.class);
        when(categoryFeedbackRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(mockPage);

        Page<CategoryFeedback> result = categoryFeedbackService.getAllFeedbacks("", pageable);

        assertSame(mockPage, result);
        verify(categoryFeedbackRepository, times(1)).findAllByOrderByCreatedAtDesc(pageable);
    }

    @Test
    void reviewFeedback_WhenValidAction_UpdatesAndSaves() {
        when(categoryFeedbackRepository.findById(1L)).thenReturn(java.util.Optional.of(testFeedback));
        when(categoryFeedbackRepository.save(any(CategoryFeedback.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryFeedback result = categoryFeedbackService.reviewFeedback(1L, "ACCEPTED", "通过", 2L);

        assertEquals(CategoryFeedback.FeedbackStatus.ACCEPTED, result.getStatus());
        assertEquals("通过", result.getAdminReply());
        assertEquals(2L, result.getReviewedBy());
        assertNotNull(result.getReviewedAt());
        verify(categoryFeedbackRepository, times(1)).findById(1L);
        verify(categoryFeedbackRepository, times(1)).save(any(CategoryFeedback.class));
    }

    @Test
    void reviewFeedback_WhenFeedbackNotFound_ThrowsException() {
        when(categoryFeedbackRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        assertThrows(BusinessException.class, () ->
                categoryFeedbackService.reviewFeedback(999L, "ACCEPTED", "通过", 2L));
    }

    @Test
    void reviewFeedback_WhenInvalidAction_ThrowsException() {
        when(categoryFeedbackRepository.findById(1L)).thenReturn(java.util.Optional.of(testFeedback));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                categoryFeedbackService.reviewFeedback(1L, "INVALID_ACTION", "回复", 2L));
        assertTrue(ex.getMessage().contains("Invalid action"));
    }
}
