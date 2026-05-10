package com.idleitems.school.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.dto.UserStatsDTO;
import com.idleitems.school.entity.User;
import com.idleitems.school.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(ApiPaths.User.BASE)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping(ApiPaths.User.PROFILE_PATH)
    public Result<User> getProfile(@RequestAttribute("userId") Long userId) {
        User user = userService.getUserById(userId);
        return Result.success(user);
    }

    @PutMapping(ApiPaths.User.UPDATE_PATH)
    public Result<User> updateProfile(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, Object> updates) {
        User updatedUser = userService.updateUser(userId, updates);
        return Result.success("更新成功", updatedUser);
    }

    @GetMapping(ApiPaths.User.STATS_PATH)
    public Result<UserStatsDTO> getUserStats(@RequestAttribute("userId") Long userId) {
        UserStatsDTO stats = userService.getUserStats(userId);
        return Result.success(stats);
    }
}
