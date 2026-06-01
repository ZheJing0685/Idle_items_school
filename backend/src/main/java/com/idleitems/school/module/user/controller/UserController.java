package com.idleitems.school.module.user.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.module.user.dto.UpdateProfileRequest;
import com.idleitems.school.module.user.dto.UserDTO;
import com.idleitems.school.module.user.dto.UserStatsDTO;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理", description = "用户个人信息管理相关接口")
@RestController
@RequestMapping(ApiPaths.User.BASE)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取用户信息", description = "获取当前登录用户的个人资料信息")
    @GetMapping(ApiPaths.User.PROFILE_PATH)
    public Result<UserDTO> getProfile(@RequestAttribute("userId") Long userId) {
        User user = userService.getUserById(userId);
        return Result.success(UserDTO.fromEntityWithoutMask(user));
    }

    @Operation(summary = "更新用户信息", description = "更新当前登录用户的个人资料")
    @PutMapping(ApiPaths.User.UPDATE_PATH)
    public Result<UserDTO> updateProfile(
            @RequestAttribute("userId") Long userId,
            @RequestBody UpdateProfileRequest request) {
        User updatedUser = userService.updateUser(userId, request);
        return Result.success("更新成功", UserDTO.fromEntity(updatedUser));
    }

    @Operation(summary = "获取用户统计", description = "获取当前用户的物品数量、收藏数量等统计数据")
    @GetMapping(ApiPaths.User.STATS_PATH)
    public Result<UserStatsDTO> getUserStats(@RequestAttribute("userId") Long userId) {
        UserStatsDTO stats = userService.getUserStats(userId);
        return Result.success(stats);
    }
}
