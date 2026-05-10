package com.idleitems.school.service;

public interface PasswordResetService {

    /**
     * 发送密码重置验证码
     */
    void sendResetCode(String email);

    /**
     * 验证验证码
     */
    boolean verifyCode(String email, String code);

    /**
     * 重置密码
     */
    void resetPassword(String email, String code, String newPassword);
}
