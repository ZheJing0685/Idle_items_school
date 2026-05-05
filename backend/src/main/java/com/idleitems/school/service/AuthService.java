package com.idleitems.school.service;

import com.idleitems.school.dto.LoginRequest;
import com.idleitems.school.dto.RegisterRequest;
import com.idleitems.school.entity.User;

import java.util.Map;

public interface AuthService {
    
    Map<String, Object> login(LoginRequest loginRequest);
    
    User register(RegisterRequest registerRequest);
    
    User getCurrentUser(String userId);
    
    Map<String, Object> refreshToken(String refreshToken);
    
    boolean validateToken(String token);
    
    String getUserIdFromToken(String token);
}
