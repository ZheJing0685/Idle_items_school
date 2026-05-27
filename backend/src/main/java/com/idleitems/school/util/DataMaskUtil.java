package com.idleitems.school.util;

/**
 * 数据脱敏工具类
 * 用于对敏感数据进行脱敏处理
 */
public class DataMaskUtil {

    /**
     * 手机号脱敏
     * 例如：13812345678 -> 138****5678
     *
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 身份证号脱敏
     * 例如：110101199901011234 -> 1101****1234
     *
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 邮箱脱敏
     * 例如：zhangsan@example.com -> zh****@example.com
     *
     * @param email 邮箱
     * @return 脱敏后的邮箱
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) {
            return email;
        }
        return email.substring(0, 2) + "****" + email.substring(atIndex);
    }

    /**
     * 姓名脱敏
     * 例如：张三 -> 张*  张三丰 -> 张*丰
     *
     * @param name 姓名
     * @return 脱敏后的姓名
     */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() <= 1) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(name.charAt(0));
        for (int i = 1; i < name.length() - 1; i++) {
            sb.append("*");
        }
        sb.append(name.charAt(name.length() - 1));
        return sb.toString();
    }

    /**
     * 银行卡号脱敏
     * 例如：6222021234567890123 -> 6222****0123
     *
     * @param bankCard 银行卡号
     * @return 脱敏后的银行卡号
     */
    public static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.length() < 8) {
            return bankCard;
        }
        return bankCard.substring(0, 4) + "****" + bankCard.substring(bankCard.length() - 4);
    }

    /**
     * 地址脱敏
     * 例如：北京市海淀区中关村大街1号 -> 北京市海淀区****
     *
     * @param address 地址
     * @return 脱敏后的地址
     */
    public static String maskAddress(String address) {
        if (address == null || address.length() < 6) {
            return address;
        }
        return address.substring(0, 6) + "****";
    }

    /**
     * 联系信息脱敏（微信号/QQ号）
     * 例如：wechat12345 -> wec****45
     *
     * @param info 联系信息
     * @return 脱敏后的联系信息
     */
    public static String maskContactInfo(String info) {
        if (info == null || info.length() < 5) {
            return info;
        }
        int keepPrefix = Math.min(3, info.length() - 4);
        return info.substring(0, keepPrefix) + "****" + info.substring(info.length() - 2);
    }
}
