package com.idleitems.school.module.notification.service.impl;

import com.idleitems.school.module.notification.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@idle-items.school}")
    private String fromAddress;

    @Value("${app.mail.sender-name:闲置物品校园交易平台}")
    private String senderName;

    @Override
    @Async("emailExecutor")
    public void sendPasswordResetCode(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress, senderName);
            helper.setTo(to);
            helper.setSubject("【闲置物品校园交易平台】密码重置验证码");

            String htmlContent = buildResetCodeEmail(code);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("密码重置验证码邮件已发送至: {}***", to.substring(0, Math.min(3, to.length())));
        } catch (MessagingException e) {
            log.error("邮件发送失败（消息构建异常）: {}", e.getMessage(), e);
            throw new RuntimeException("邮件发送失败，请稍后重试", e);
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage(), e);
            throw new RuntimeException("邮件发送失败，请稍后重试", e);
        }
    }

    private String buildResetCodeEmail(String code) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #f5f5f5; padding: 20px;">
                  <div style="max-width: 480px; margin: 0 auto; background: white; border-radius: 12px; padding: 32px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);">
                    <h2 style="color: #4f46e5; margin-bottom: 8px;">密码重置验证码</h2>
                    <p style="color: #666; font-size: 14px;">您正在重置密码，请使用以下验证码完成操作：</p>
                    <div style="background: #f0f0ff; border-radius: 8px; padding: 16px; text-align: center; margin: 24px 0;">
                      <span style="font-size: 32px; font-weight: 700; color: #4f46e5; letter-spacing: 6px;">%s</span>
                    </div>
                    <p style="color: #999; font-size: 13px;">验证码 <b>5 分钟</b>内有效。如非本人操作，请忽略此邮件。</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;">
                    <p style="color: #bbb; font-size: 12px;">闲置物品校园交易平台</p>
                  </div>
                </body>
                </html>
                """.formatted(code);
    }
}
