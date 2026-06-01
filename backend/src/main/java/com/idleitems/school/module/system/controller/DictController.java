package com.idleitems.school.module.system.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.module.system.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "字典管理", description = "数据字典相关接口")
@RestController
@RequestMapping(ApiPaths.Dict.BASE)
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @Operation(summary = "获取所有字典数据")
    @GetMapping("/all")
    public Result<Map<String, List<Map<String, Object>>>> getAllDicts() {
        return Result.success(dictService.getAllDicts());
    }

    @Operation(summary = "获取指定类型的字典数据")
    @GetMapping("/{typeCode}")
    public Result<List<Map<String, Object>>> getDictByType(@PathVariable String typeCode) {
        return Result.success(dictService.getDictByType(typeCode));
    }

    @Operation(summary = "获取字典项标签")
    @GetMapping("/label")
    public Result<String> getDictLabel(
            @RequestParam String typeCode,
            @RequestParam String itemValue) {
        return Result.success(dictService.getDictLabel(typeCode, itemValue));
    }

    @Operation(summary = "获取字典选项列表（用于下拉框）")
    @GetMapping("/{typeCode}/options")
    public Result<List<Map<String, String>>> getDictOptions(@PathVariable String typeCode) {
        return Result.success(dictService.getDictOptions(typeCode));
    }

    @Operation(summary = "清除字典缓存")
    @PostMapping("/cache/clear")
    public Result<Void> clearDictCache() {
        dictService.clearDictCache();
        return Result.success();
    }

    @Operation(summary = "重新加载指定类型的字典缓存")
    @PostMapping("/{typeCode}/cache/reload")
    public Result<Void> reloadDictCache(@PathVariable String typeCode) {
        dictService.reloadDictCache(typeCode);
        return Result.success();
    }
}