package com.idleitems.school.module.notification.service.impl;

import com.idleitems.school.module.notification.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

/**
 * 邮件服务空实现（未配置邮件服务器时使用）
 * 仅打印日志，不实际发送邮件
 */
@Slf4j
@Service
@ConditionalOnMissingBean(EmailService.class)
public class NoOpEmailServiceImpl implements EmailService {

    @Override
    public void sendPasswordResetCode(String to, String code) {
        log.warn("[邮件未配置] 密码重置验证码: {} -> {}", to, code);
        log.warn("请配置 MAIL_USERNAME / MAIL_PASSWORD 环境变量以启用邮件发送");
    }
}
