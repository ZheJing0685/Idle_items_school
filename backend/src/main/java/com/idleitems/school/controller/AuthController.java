package com.idleitems.school.controller;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.dto.ChangePasswordRequest;
import com.idleitems.school.dto.ForgotPasswordRequest;
import com.idleitems.school.dto.LoginRequest;
import com.idleitems.school.dto.RegisterRequest;
import com.idleitems.school.dto.ResetPasswordRequest;
import com.idleitems.school.dto.UserDTO;
import com.idleitems.school.dto.VerifyCodeRequest;
import com.idleitems.school.entity.User;
import com.idleitems.school.service.AuthService;
import com.idleitems.school.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;

@Tag(name = "认证管理", description = "用户注册、登录、Token刷新等认证相关接口")
@RestController
@RequestMapping(ApiPaths.Auth.BASE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @Operation(summary = "用户注册", description = "提供用户名、密码、邮箱等信息注册新用户，注册成功后自动登录")
    @PostMapping(ApiPaths.Auth.REGISTER_PATH)
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        // 注册成功后自动登录，返回Token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(request.getUsername());
        loginRequest.setPassword(request.getPassword());
        Map<String, Object> data = authService.login(loginRequest);
        return Result.success("注册成功", data);
    }

    @Operation(summary = "用户登录", description = "使用用户名和密码进行登录，返回JWT Token和刷新Token")
    @PostMapping(ApiPaths.Auth.LOGIN_PATH)
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> data = authService.login(request);
        return Result.success("登录成功", data);
    }

    @Operation(summary = "获取当前用户信息", description = "根据JWT Token获取当前登录用户的详细信息")
    @GetMapping(ApiPaths.Auth.ME_PATH)
    public Result<UserDTO> getCurrentUser(@RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }
        User user = authService.getCurrentUser(userId.toString());
        return Result.success(UserDTO.fromEntity(user));
    }

    @Operation(summary = "刷新Token", description = "使用刷新令牌获取新的JWT Token")
    @PostMapping(ApiPaths.Auth.REFRESH_PATH)
    public Result<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        Map<String, Object> data = authService.refreshToken(request.get("refreshToken"));
        return Result.success("令牌刷新成功", data);
    }

    @Operation(summary = "忘记密码", description = "通过邮箱发送密码重置验证码")
    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.sendResetCode(request.getEmail());
        return Result.success("验证码已发送到您的邮箱", null);
    }

    @Operation(summary = "验证重置码", description = "验证密码重置验证码是否正确")
    @PostMapping("/verify-code")
    public Result<Void> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        if (!passwordResetService.verifyCode(request.getEmail(), request.getCode())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码错误或已过期");
        }
        return Result.success("验证码验证成功", null);
    }

    @Operation(summary = "重置密码", description = "使用验证码和新密码重置用户密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
        return Result.success("密码重置成功，请重新登录", null);
    }

    @Operation(summary = "修改密码", description = "登录用户修改密码（需提供旧密码验证）")
    @PostMapping(ApiPaths.Auth.CHANGE_PASSWORD_PATH)
    public Result<Void> changePassword(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.success("密码修改成功，请重新登录", null);
    }

    @Operation(summary = "用户登出", description = "使当前Token失效，安全退出登录")
    @PostMapping(ApiPaths.Auth.LOGOUT_PATH)
    public Result<Void> logout(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            authService.logout(token);
        }
        return Result.success("登出成功", null);
    }

}
