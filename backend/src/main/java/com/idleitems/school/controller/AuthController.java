package com.idleitems.school.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.dto.LoginRequest;
import com.idleitems.school.dto.RegisterRequest;
import com.idleitems.school.entity.User;
import com.idleitems.school.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(ApiPaths.Auth.BASE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(ApiPaths.Auth.REGISTER_PATH)
    public Result<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return Result.success("注册成功", user);
    }

    @PostMapping(ApiPaths.Auth.LOGIN_PATH)
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> data = authService.login(request);
        return Result.success("登录成功", data);
    }

    @GetMapping(ApiPaths.Auth.ME_PATH)
    public Result<User> getCurrentUser(@RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            throw new SecurityException("登录已过期，请重新登录");
        }
        User user = authService.getCurrentUser(userId.toString());
        return Result.success(user);
    }

    @PostMapping(ApiPaths.Auth.REFRESH_PATH)
    public Result<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        Map<String, Object> data = authService.refreshToken(request.get("refreshToken"));
        return Result.success("令牌刷新成功", data);
    }
}
