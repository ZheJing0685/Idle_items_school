package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.order.entity.Order;
import com.idleitems.school.module.order.repository.OrderRepository;
import com.idleitems.school.module.order.repository.ReviewRepository;
import com.idleitems.school.module.user.dto.SellerProfileDTO;
import com.idleitems.school.module.user.dto.UpdateProfileRequest;
import com.idleitems.school.module.user.dto.UserStatsDTO;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.user.repository.UserRepository;
import com.idleitems.school.module.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setNickname("测试用户");
        testUser.setRole(User.Role.STUDENT);
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser.setVerified(false);
        testUser.setCreditScore(100);
        testUser.setLoginCount(0);
        testUser.setIsDeleted(false);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        User result = userService.getUserById(1L);
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        BusinessException ex = assertThrows(BusinessException.class, () -> userService.getUserById(999L));
        assertEquals("用户不存在", ex.getMessage());
        verify(userRepository).findById(999L);
    }

    @Test
    void findById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        User result = userService.findById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).findById(1L);
    }

    @Test
    void findById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> userService.findById(999L));
        verify(userRepository).findById(999L);
    }

    @Test
    void findByUsername_Found() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        Optional<User> result = userService.findByUsername("testuser");
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void findByUsername_NotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        Optional<User> result = userService.findByUsername("nonexistent");
        assertFalse(result.isPresent());
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void findByEmail_Found() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        Optional<User> result = userService.findByEmail("test@example.com");
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void findByEmail_NotFound() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());
        Optional<User> result = userService.findByEmail("missing@test.com");
        assertFalse(result.isPresent());
        verify(userRepository).findByEmail("missing@test.com");
    }

    @Test
    void findByPhone_Found() {
        when(userRepository.findByPhone("13800138000")).thenReturn(Optional.of(testUser));
        Optional<User> result = userService.findByPhone("13800138000");
        assertTrue(result.isPresent());
        assertEquals("13800138000", result.get().getPhone());
        verify(userRepository).findByPhone("13800138000");
    }

    @Test
    void findByPhone_NotFound() {
        when(userRepository.findByPhone("13900000000")).thenReturn(Optional.empty());
        Optional<User> result = userService.findByPhone("13900000000");
        assertFalse(result.isPresent());
        verify(userRepository).findByPhone("13900000000");
    }

    @Test
    void existsByUsername_True() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        assertTrue(userService.existsByUsername("testuser"));
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    void existsByUsername_False() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        assertFalse(userService.existsByUsername("newuser"));
        verify(userRepository).existsByUsername("newuser");
    }

    @Test
    void existsByEmail_True() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        assertTrue(userService.existsByEmail("test@example.com"));
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    void existsByEmail_False() {
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        assertFalse(userService.existsByEmail("new@test.com"));
        verify(userRepository).existsByEmail("new@test.com");
    }

    @Test
    void existsByPhone_True() {
        when(userRepository.existsByPhone("13800138000")).thenReturn(true);
        assertTrue(userService.existsByPhone("13800138000"));
        verify(userRepository).existsByPhone("13800138000");
    }

    @Test
    void existsByPhone_False() {
        when(userRepository.existsByPhone("13900000000")).thenReturn(false);
        assertFalse(userService.existsByPhone("13900000000"));
        verify(userRepository).existsByPhone("13900000000");
    }

    @Test
    void save_Success() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        User result = userService.save(testUser);
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).save(testUser);
    }

    @Test
    void register_Success() {
        User rawUser = new User();
        rawUser.setUsername("newuser");
        rawUser.setPassword("rawPassword");
        rawUser.setEmail("new@test.com");

        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.register(rawUser);

        assertEquals("encodedPassword", rawUser.getPassword());
        verify(passwordEncoder).encode("rawPassword");
        verify(userRepository).save(rawUser);
    }

    @Test
    void updateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("新昵称");
        request.setPhone("13900000001");
        request.setAvatar("http://avatar.new");
        request.setStudentId("2024001");
        request.setGender(1);
        request.setBio("新简介");
        request.setSchoolName("测试大学");
        request.setDepartment("计算机系");
        request.setMajor("软件工程");
        request.setGrade("大四");

        when(userRepository.existsByPhone("13900000001")).thenReturn(false);

        User result = userService.updateUser(1L, request);

        assertEquals("新昵称", result.getNickname());
        assertEquals("13900000001", result.getPhone());
        assertEquals("http://avatar.new", result.getAvatar());
        assertEquals("2024001", result.getStudentId());
        assertEquals(1, result.getGender());
        assertEquals("新简介", result.getBio());
        assertEquals("测试大学", result.getSchoolName());
        assertEquals("计算机系", result.getDepartment());
        assertEquals("软件工程", result.getMajor());
        assertEquals("大四", result.getGrade());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_PhoneConflict() {
        testUser.setPhone("13800138000");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByPhone("13900000001")).thenReturn(true);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setPhone("13900000001");

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.updateUser(1L, request));
        assertEquals("手机号已被使用", ex.getMessage());
        verify(userRepository).existsByPhone("13900000001");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_SamePhoneNoConflict() {
        testUser.setPhone("13800138000");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setPhone("13800138000");

        User result = userService.updateUser(1L, request);
        assertEquals("13800138000", result.getPhone());
        verify(userRepository, never()).existsByPhone(anyString());
    }

    @Test
    void updateUser_WithBirthday() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setBirthday("2000-01-15");

        User result = userService.updateUser(1L, request);
        assertEquals(LocalDate.of(2000, 1, 15), result.getBirthday());
    }

    @Test
    void updateUser_ClearBirthday() {
        testUser.setBirthday(LocalDate.of(2000, 1, 15));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setBirthday("");

        User result = userService.updateUser(1L, request);
        assertNull(result.getBirthday());
    }

    @Test
    void updateUser_NullBirthdayLeavesUnchanged() {
        testUser.setBirthday(LocalDate.of(2000, 1, 15));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileRequest request = new UpdateProfileRequest();

        User result = userService.updateUser(1L, request);
        assertEquals(LocalDate.of(2000, 1, 15), result.getBirthday());
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });

        User result = userService.createUser("newuser", "new@test.com", "password123", "13900000001",
                User.Role.STUDENT, User.UserStatus.ACTIVE, "昵称", "2024001");

        assertEquals("newuser", result.getUsername());
        assertEquals("new@test.com", result.getEmail());
        assertEquals("encoded", result.getPassword());
        assertEquals("13900000001", result.getPhone());
        assertEquals(User.Role.STUDENT, result.getRole());
        assertEquals(User.UserStatus.ACTIVE, result.getStatus());
        assertEquals("昵称", result.getNickname());
        assertEquals("2024001", result.getStudentId());
        assertFalse(result.getVerified());
        assertEquals(100, result.getCreditScore());
        assertEquals(0, result.getLoginCount());
        assertFalse(result.getIsDeleted());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_UsernameConflict() {
        when(userRepository.existsByUsername("existing")).thenReturn(true);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.createUser("existing", "e@t.com", "pwd", null, null, null, null, null));
        assertEquals("用户名已存在", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_EmailConflict() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.createUser("newuser", "dup@test.com", "pwd", null, null, null, null, null));
        assertEquals("邮箱已存在", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_PhoneConflict() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.existsByPhone("13900000001")).thenReturn(true);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.createUser("newuser", "new@test.com", "pwd", "13900000001", null, null, null, null));
        assertEquals("手机号已存在", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_DefaultRoleAndStatus() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pwd")).thenReturn("enc");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createUser("newuser", "new@test.com", "pwd", null, null, null, null, null);

        assertEquals(User.Role.STUDENT, result.getRole());
        assertEquals(User.UserStatus.ACTIVE, result.getStatus());
        assertEquals("newuser", result.getNickname());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserAdmin_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUserAdmin(1L, "new@test.com", "13900000001", User.Role.ADMIN,
                User.UserStatus.DISABLED, "adminNick", "2024002", 1, "bio", "school", "dept", "major", "grade");

        assertEquals("new@test.com", result.getEmail());
        assertEquals("13900000001", result.getPhone());
        assertEquals(User.Role.ADMIN, result.getRole());
        assertEquals(User.UserStatus.DISABLED, result.getStatus());
        assertEquals("adminNick", result.getNickname());
        assertEquals("2024002", result.getStudentId());
        assertEquals(1, result.getGender());
        assertEquals("bio", result.getBio());
        assertEquals("school", result.getSchoolName());
        assertEquals("dept", result.getDepartment());
        assertEquals("major", result.getMajor());
        assertEquals("grade", result.getGrade());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserAdmin_EmailConflict() {
        testUser.setEmail("old@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.updateUserAdmin(1L, "taken@test.com", null, null, null, null, null, null, null, null, null, null, null));
        assertEquals("邮箱已存在", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserAdmin_PhoneConflict() {
        testUser.setPhone("13800138000");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByPhone("13900000001")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.updateUserAdmin(1L, null, "13900000001", null, null, null, null, null, null, null, null, null, null));
        assertEquals("手机号已存在", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserAdmin_SameEmailNoConflict() {
        testUser.setEmail("same@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUserAdmin(1L, "same@test.com", null, null, null, null, null, null, null, null, null, null, null);

        assertEquals("same@test.com", result.getEmail());
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void deleteUsers_Success() {
        User user1 = new User();
        user1.setId(2L);
        user1.setRole(User.Role.STUDENT);
        User user2 = new User();
        user2.setId(3L);
        user2.setRole(User.Role.STUDENT);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(3L)).thenReturn(Optional.of(user2));
        doNothing().when(userRepository).deleteById(anyLong());

        userService.deleteUsers(Arrays.asList(2L, 3L));

        verify(userRepository).deleteById(2L);
        verify(userRepository).deleteById(3L);
    }

    @Test
    void deleteUsers_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.deleteUsers(Collections.singletonList(99L)));
        assertTrue(ex.getMessage().contains("用户不存在"));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteUsers_AdminNotAllowed() {
        User admin = new User();
        admin.setId(1L);
        admin.setRole(User.Role.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.deleteUsers(Collections.singletonList(1L)));
        assertEquals("不能删除管理员用户", ex.getMessage());
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void getUsersForExport_RoleAndStatus() {
        Page<User> page = new PageImpl<>(Collections.singletonList(testUser));
        when(userRepository.findByRoleAndStatus(User.Role.STUDENT, User.UserStatus.ACTIVE, Pageable.unpaged())).thenReturn(page);

        List<User> result = userService.getUsersForExport(null, User.Role.STUDENT, User.UserStatus.ACTIVE);

        assertEquals(1, result.size());
        verify(userRepository).findByRoleAndStatus(User.Role.STUDENT, User.UserStatus.ACTIVE, Pageable.unpaged());
        verify(userRepository, never()).findByRole(any(), any());
        verify(userRepository, never()).findByStatus(any(), any());
    }

    @Test
    void getUsersForExport_RoleOnly() {
        Page<User> page = new PageImpl<>(Collections.singletonList(testUser));
        when(userRepository.findByRole(User.Role.ADMIN, Pageable.unpaged())).thenReturn(page);

        List<User> result = userService.getUsersForExport(null, User.Role.ADMIN, null);

        assertEquals(1, result.size());
        verify(userRepository).findByRole(User.Role.ADMIN, Pageable.unpaged());
    }

    @Test
    void getUsersForExport_StatusOnly() {
        Page<User> page = new PageImpl<>(Collections.singletonList(testUser));
        when(userRepository.findByStatus(User.UserStatus.DISABLED, Pageable.unpaged())).thenReturn(page);

        List<User> result = userService.getUsersForExport(null, null, User.UserStatus.DISABLED);

        assertEquals(1, result.size());
        verify(userRepository).findByStatus(User.UserStatus.DISABLED, Pageable.unpaged());
    }

    @Test
    void getUsersForExport_NoFilter() {
        Page<User> page = new PageImpl<>(Collections.singletonList(testUser));
        when(userRepository.findAll(Pageable.unpaged())).thenReturn(page);

        List<User> result = userService.getUsersForExport(null, null, null);

        assertEquals(1, result.size());
        verify(userRepository).findAll(Pageable.unpaged());
    }

    @Test
    void getSellerProfile_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(itemRepository.countByUserIdAndStatus(1L, Item.ItemStatus.ON_SALE)).thenReturn(5L);
        when(orderRepository.countBySellerIdAndStatus(1L, Order.OrderStatus.COMPLETED)).thenReturn(3L);
        when(orderRepository.countByBuyerIdAndStatus(1L, Order.OrderStatus.COMPLETED)).thenReturn(2L);
        when(reviewRepository.getAverageRatingByUserId(1L)).thenReturn(BigDecimal.valueOf(4.5));
        when(reviewRepository.countByReviewedUserId(1L)).thenReturn(10L);

        SellerProfileDTO dto = userService.getSellerProfile(1L);

        assertEquals(1L, dto.getId());
        assertEquals("测试用户", dto.getNickname());
        assertEquals(5L, dto.getTotalItems());
        assertEquals(3L, dto.getSoldItems());
        assertEquals(5L, dto.getCompletedDeals());
        assertEquals(4.5, dto.getRating(), 0.001);
        assertEquals(10L, dto.getReviewCount());
        assertFalse(dto.getVerified());
        assertEquals(100, dto.getCreditScore());
        verify(userRepository).findById(1L);
        verify(itemRepository).countByUserIdAndStatus(1L, Item.ItemStatus.ON_SALE);
    }

    @Test
    void getSellerProfile_WithNullValues() {
        testUser.setNickname(null);
        testUser.setCreditScore(null);
        testUser.setVerified(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(itemRepository.countByUserIdAndStatus(1L, Item.ItemStatus.ON_SALE)).thenReturn(0L);
        when(orderRepository.countBySellerIdAndStatus(1L, Order.OrderStatus.COMPLETED)).thenReturn(null);
        when(orderRepository.countByBuyerIdAndStatus(1L, Order.OrderStatus.COMPLETED)).thenReturn(null);
        when(reviewRepository.getAverageRatingByUserId(1L)).thenReturn(null);
        when(reviewRepository.countByReviewedUserId(1L)).thenReturn(null);

        SellerProfileDTO dto = userService.getSellerProfile(1L);

        assertEquals("testuser", dto.getNickname());
        assertEquals(0L, dto.getSoldItems());
        assertEquals(0L, dto.getCompletedDeals());
        assertEquals(0.0, dto.getRating(), 0.001);
        assertEquals(0L, dto.getReviewCount());
        assertFalse(dto.getVerified());
        assertEquals(100, dto.getCreditScore());
    }

    @Test
    void getUserStats_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(itemRepository.countByUserId(1L)).thenReturn(10L);
        when(orderRepository.countBySellerIdAndStatus(1L, Order.OrderStatus.COMPLETED)).thenReturn(3L);
        when(orderRepository.countByBuyerIdAndStatus(1L, Order.OrderStatus.COMPLETED)).thenReturn(2L);
        when(reviewRepository.getAverageRatingByUserId(1L)).thenReturn(BigDecimal.valueOf(4.2));

        UserStatsDTO stats = userService.getUserStats(1L);

        assertEquals(10L, stats.getTotalItems());
        assertEquals(3L, stats.getSoldItems());
        assertEquals(5L, stats.getCompletedDeals());
        assertEquals(4.2, stats.getRating(), 0.001);
        verify(userRepository).findById(1L);
        verify(itemRepository).countByUserId(1L);
    }

    @Test
    void getUserStats_FallbackRating() {
        testUser.setCreditScore(80);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(itemRepository.countByUserId(1L)).thenReturn(0L);
        when(orderRepository.countBySellerIdAndStatus(1L, Order.OrderStatus.COMPLETED)).thenReturn(0L);
        when(orderRepository.countByBuyerIdAndStatus(1L, Order.OrderStatus.COMPLETED)).thenReturn(0L);
        when(reviewRepository.getAverageRatingByUserId(1L)).thenReturn(null);

        UserStatsDTO stats = userService.getUserStats(1L);

        assertEquals(80.0, stats.getRating(), 0.001);
    }

    @Test
    void getUserStats_NullCreditScoreFallback() {
        testUser.setCreditScore(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(itemRepository.countByUserId(1L)).thenReturn(0L);
        when(orderRepository.countBySellerIdAndStatus(1L, Order.OrderStatus.COMPLETED)).thenReturn(0L);
        when(orderRepository.countByBuyerIdAndStatus(1L, Order.OrderStatus.COMPLETED)).thenReturn(0L);
        when(reviewRepository.getAverageRatingByUserId(1L)).thenReturn(null);

        UserStatsDTO stats = userService.getUserStats(1L);

        assertEquals(100.0, stats.getRating(), 0.001);
    }

    @Test
    void enrichItemWithSellerInfo_Success() {
        Item item = new Item();
        item.setUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(reviewRepository.getAverageRatingByUserId(1L)).thenReturn(BigDecimal.valueOf(4.8));

        userService.enrichItemWithSellerInfo(item, 7);

        assertEquals("测试用户", item.getSellerNickname());
        assertFalse(item.isSellerVerified());
        assertEquals(4.8, item.getSellerRating(), 0.001);
        assertEquals(7, item.getSellerItemsCount().intValue());
        verify(userRepository).findById(1L);
        verify(reviewRepository).getAverageRatingByUserId(1L);
    }

    @Test
    void enrichItemWithSellerInfo_NoNickname() {
        testUser.setNickname(null);
        Item item = new Item();
        item.setUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(reviewRepository.getAverageRatingByUserId(1L)).thenReturn(null);

        userService.enrichItemWithSellerInfo(item, 3);

        assertEquals("testuser", item.getSellerNickname());
        assertEquals(0.0, item.getSellerRating(), 0.001);
        assertEquals(3, item.getSellerItemsCount().intValue());
    }

    @Test
    void enrichItemWithSellerInfo_NullItem() {
        userService.enrichItemWithSellerInfo(null, 0);
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void enrichItemWithSellerInfo_NullUserId() {
        Item item = new Item();
        userService.enrichItemWithSellerInfo(item, 0);
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void enrichItemWithSellerInfo_UserNotFound() {
        Item item = new Item();
        item.setUserId(99L);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        userService.enrichItemWithSellerInfo(item, 0);

        assertNull(item.getSellerNickname());
        verify(reviewRepository, never()).getAverageRatingByUserId(anyLong());
    }
}
