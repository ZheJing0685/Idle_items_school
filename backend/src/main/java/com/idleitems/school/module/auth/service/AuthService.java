package com.idleitems.school.module.auth.service;

import com.idleitems.school.module.auth.dto.LoginRequest;
import com.idleitems.school.module.auth.dto.RegisterRequest;
import com.idleitems.school.module.user.entity.User;

import java.util.Map;

public interface AuthService {
    
    Map<String, Object> login(LoginRequest loginRequest);
    
    User register(RegisterRequest registerRequest);
    
    User getCurrentUser(String userId);
    
    Map<String, Object> refreshToken(String refreshToken);
    
    boolean validateToken(String token);
    
    String getUserIdFromToken(String token);
    
    /**
     * 用户登出，使当前Token失效
     * @param token 当前有效的JWT Token
     */
    void logout(String token);
    
    /**
     * 修改密码，使该用户所有Token失效
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
}
