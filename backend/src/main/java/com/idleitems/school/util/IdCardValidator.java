package com.idleitems.school.util;

/**
 * 身份证号校验工具
 * 支持18位身份证号格式校验和校验位验证
 */
public final class IdCardValidator {

    private IdCardValidator() {}

    private static final int[] WEIGHT = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final char[] CHECK_CODE = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    /**
     * 校验身份证号是否合法
     *
     * @param idCard 身份证号
     * @return true=合法
     */
    public static boolean isValid(String idCard) {
        if (idCard == null) return false;

        // 统一转大写
        idCard = idCard.trim().toUpperCase();

        // 18位格式：前17位数字 + 最后一位校验码（数字或X）
        if (!idCard.matches("^\\d{17}[\\dX]$")) {
            return false;
        }

        // 校验位验证
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (idCard.charAt(i) - '0') * WEIGHT[i];
        }
        char expectedCheckCode = CHECK_CODE[sum % 11];
        return expectedCheckCode == idCard.charAt(17);
    }

    /**
     * 脱敏显示身份证号（中间隐藏）
     * 例：110101199001011234 → 110101********1234
     *
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    public static String mask(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }
}
