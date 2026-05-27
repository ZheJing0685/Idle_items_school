package com.idleitems.school.controller;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.entity.SystemConfig;
import com.idleitems.school.entity.User;
import com.idleitems.school.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "配置管理", description = "系统配置相关接口")
@RestController
@RequestMapping(ApiPaths.Config.BASE)
@RequiredArgsConstructor
@RequireRole({User.Role.ADMIN})
public class ConfigController {

    private final ConfigService configService;

    @Operation(summary = "获取所有配置")
    @GetMapping
    public Result<Map<String, Object>> getAllConfigs() {
        return Result.success(configService.getAllConfigs());
    }

    @Operation(summary = "获取指定配置值")
    @GetMapping("/{configKey}")
    public Result<Object> getConfig(@PathVariable String configKey) {
        return Result.success(configService.getConfig(configKey));
    }

    @Operation(summary = "获取指定分组的配置")
    @GetMapping("/group/{groupName}")
    public Result<Map<String, Object>> getConfigsByGroup(@PathVariable String groupName) {
        return Result.success(configService.getConfigsByGroup(groupName));
    }

    @Operation(summary = "保存或更新配置")
    @PostMapping
    public Result<SystemConfig> saveConfig(
            @RequestParam String configKey,
            @RequestParam String configValue,
            @RequestParam(required = false) String description) {
        return Result.success(configService.saveConfig(configKey, configValue, description));
    }

    @Operation(summary = "删除配置")
    @DeleteMapping("/{configKey}")
    public Result<Void> deleteConfig(@PathVariable String configKey) {
        configService.deleteConfig(configKey);
        return Result.success();
    }

    @Operation(summary = "清除配置缓存")
    @PostMapping("/cache/clear")
    public Result<Void> clearConfigCache() {
        configService.clearConfigCache();
        return Result.success();
    }

    @Operation(summary = "重新加载配置缓存")
    @PostMapping("/cache/reload")
    public Result<Void> reloadConfigCache() {
        configService.reloadConfigCache();
        return Result.success();
    }
}