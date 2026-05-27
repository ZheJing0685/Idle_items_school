package com.idleitems.school.util;

import java.util.Set;

/**
 * 敏感词基础过滤工具
 * 用于物品发布时的内容安全检查
 */
public final class SensitiveWordFilter {

    private SensitiveWordFilter() {}

    /**
     * 基础敏感词库（实际项目中应从配置或数据库加载）
     */
    private static final Set<String> SENSITIVE_WORDS = Set.of(
            // 违禁品
            "枪支", "弹药", "毒品", "管制刀具", "窃听器",
            // 诈骗相关
            "刷单", "代练", "外挂", "黑产",
            // 违法交易
            "身份证买卖", "银行卡出售", "手机卡出售",
            // 其他违规
            "色情", "赌博", "传销"
    );

    /**
     * 检查文本是否包含敏感词
     *
     * @param text 待检查文本
     * @return 包含的敏感词列表，为空表示安全
     */
    public static java.util.List<String> findSensitiveWords(String text) {
        if (text == null || text.isEmpty()) {
            return java.util.List.of();
        }
        String lowerText = text.toLowerCase();
        return SENSITIVE_WORDS.stream()
                .filter(lowerText::contains)
                .toList();
    }

    /**
     * 检查文本是否安全（不包含敏感词）
     *
     * @param text 待检查文本
     * @return true=安全, false=包含敏感词
     */
    public static boolean isSafe(String text) {
        return findSensitiveWords(text).isEmpty();
    }

    /**
     * 获取安全提示信息
     *
     * @param words 发现的敏感词
     * @return 提示信息
     */
    public static String getWarningMessage(java.util.List<String> words) {
        if (words.isEmpty()) return "";
        return "内容包含敏感词: " + String.join("、", words) + "，请修改后重新提交";
    }
}
