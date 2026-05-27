package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.ReviewRepository;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("UserService 单元测试")
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
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setNickname("测试用户");
        testUser.setRole(User.Role.STUDENT);
        testUser.setStatus(User.UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("测试用户注册 - 成功")
    void testRegisterSuccess() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User user = new User();
        user.setUsername("newuser");
        user.setPassword("password123");
        user.setEmail("new@example.com");

        User result = userService.register(user);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("测试根据用户名查找用户")
    void testFindByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("测试根据邮箱查找用户")
    void testFindByEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("测试根据ID查找用户")
    void testFindById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = userService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("测试检查用户名是否存在 - 存在")
    void testExistsByUsernameExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        boolean result = userService.existsByUsername("testuser");

        assertTrue(result);
        verify(userRepository, times(1)).existsByUsername("testuser");
    }

    @Test
    @DisplayName("测试检查用户名是否存在 - 不存在")
    void testExistsByUsernameNotExists() {
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        boolean result = userService.existsByUsername("nonexistent");

        assertFalse(result);
        verify(userRepository, times(1)).existsByUsername("nonexistent");
    }

    @Test
    @DisplayName("测试检查邮箱是否存在")
    void testExistsByEmail() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        boolean result = userService.existsByEmail("test@example.com");

        assertTrue(result);
        verify(userRepository, times(1)).existsByEmail("test@example.com");
    }

    @Test
    @DisplayName("测试检查手机号是否存在")
    void testExistsByPhone() {
        when(userRepository.existsByPhone("13800138000")).thenReturn(true);

        boolean result = userService.existsByPhone("13800138000");

        assertTrue(result);
        verify(userRepository, times(1)).existsByPhone("13800138000");
    }

    @Test
    @DisplayName("测试更新用户信息")
    void testUpdateUser() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        testUser.setNickname("新昵称");
        User result = userService.save(testUser);

        assertNotNull(result);
        assertEquals("新昵称", result.getNickname());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("测试根据ID查找用户 - 用户不存在")
    void testFindByIdNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> {
            userService.findById(999L);
        });

        verify(userRepository, times(1)).findById(999L);
    }
}
