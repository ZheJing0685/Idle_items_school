package com.idleitems.school.module.notification.service;

/**
 * 邮件发送服务接口
 */
public interface EmailService {

    /**
     * 发送密码重置验证码邮件
     *
     * @param to      收件人邮箱
     * @param code    验证码
     */
    void sendPasswordResetCode(String to, String code);
}
