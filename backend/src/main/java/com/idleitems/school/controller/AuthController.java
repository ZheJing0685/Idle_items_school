package com.idleitems.school.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.dto.LoginRequest;
import com.idleitems.school.dto.RegisterRequest;
import com.idleitems.school.entity.User;
import com.idleitems.school.service.AuthService;
import com.idleitems.school.service.PasswordResetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(ApiPaths.Auth.BASE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

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

    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.sendResetCode(request.getEmail());
        return Result.success("验证码已发送到您的邮箱", null);
    }

    @PostMapping("/verify-code")
    public Result<Void> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        boolean valid = passwordResetService.verifyCode(request.getEmail(), request.getCode());
        if (!valid) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }
        return Result.success("验证码验证成功", null);
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
        return Result.success("密码重置成功，请重新登录", null);
    }

    @Data
    public static class ForgotPasswordRequest {
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
    }

    @Data
    public static class VerifyCodeRequest {
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
        @NotBlank(message = "验证码不能为空")
        @Size(min = 6, max = 6, message = "验证码为6位数字")
        private String code;
    }

    @Data
    public static class ResetPasswordRequest {
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
        @NotBlank(message = "验证码不能为空")
        private String code;
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度为6-20位")
        private String newPassword;
    }
}
