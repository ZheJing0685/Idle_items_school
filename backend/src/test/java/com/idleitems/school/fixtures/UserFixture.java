package com.idleitems.school.fixtures;

import com.idleitems.school.entity.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 用户测试数据工厂
 */
public class UserFixture {
    
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public static User createBuyer() {
        User user = new User();
        user.setUsername("buyer_" + System.currentTimeMillis());
        user.setPassword(passwordEncoder.encode("TestPassword@123"));
        user.setEmail("buyer_" + System.currentTimeMillis() + "@test.com");
        user.setPhone("13800138000");
        user.setNickname("测试买家");
        user.setRole(User.Role.STUDENT);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setVerified(true);
        return user;
    }
    
    public static User createSeller() {
        User user = new User();
        user.setUsername("seller_" + System.currentTimeMillis());
        user.setPassword(passwordEncoder.encode("TestPassword@123"));
        user.setEmail("seller_" + System.currentTimeMillis() + "@test.com");
        user.setPhone("13800138001");
        user.setNickname("测试卖家");
        user.setRole(User.Role.STUDENT);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setVerified(true);
        return user;
    }
    
    public static User createAdmin() {
        User user = new User();
        user.setUsername("admin_" + System.currentTimeMillis());
        user.setPassword(passwordEncoder.encode("AdminPassword@123"));
        user.setEmail("admin_" + System.currentTimeMillis() + "@test.com");
        user.setPhone("13800138002");
        user.setNickname("测试管理员");
        user.setRole(User.Role.ADMIN);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setVerified(true);
        return user;
    }
    
    public static User createWithCustomData(String username, String email, String phone) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("TestPassword@123"));
        user.setEmail(email);
        user.setPhone(phone);
        user.setNickname("测试用户");
        user.setRole(User.Role.STUDENT);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setVerified(true);
        return user;
    }
}
