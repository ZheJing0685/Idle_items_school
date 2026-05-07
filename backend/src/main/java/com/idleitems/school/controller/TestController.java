package com.idleitems.school.controller;

import com.idleitems.school.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@Slf4j
@Profile("dev")
public class TestController {

    @GetMapping("/exception")
    public Result<Void> testException() {
        log.info("测试中文日志输出");
        throw new IllegalArgumentException("测试中文异常信息");
    }

    @GetMapping("/log")
    public Result<String> testLog() {
        log.debug("调试信息：中文");
        log.info("信息：中文");
        log.warn("警告：中文");
        log.error("错误：中文");
        return Result.success("测试成功");
    }

    @GetMapping("/system")
    public Result<String> testSystemEncoding() {
        log.info("系统编码: {}", System.getProperty("file.encoding"));
        log.info("JNU编码: {}", System.getProperty("sun.jnu.encoding"));
        log.info("中文测试: 你好，世界！");
        return Result.success("系统编码测试成功");
    }
}
