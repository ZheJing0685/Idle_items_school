package com.idleitems.school.dto;

import com.idleitems.school.entity.User;
import com.idleitems.school.util.DataMaskUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息响应体")
public class UserDTO {
    @Schema(description = "用户ID")
    private Long id;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "邮箱地址")
    private String email;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "头像URL")
    private String avatar;
    @Schema(description = "用户角色", example = "STUDENT")
    private User.Role role;
    @Schema(description = "用户状态", example = "ACTIVE")
    private User.UserStatus status;
    @Schema(description = "是否实名认证")
    private Boolean verified;
    @Schema(description = "学号")
    private String studentId;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;
    @Schema(description = "登录次数")
    private Integer loginCount;
    @Schema(description = "信用分")
    private Integer creditScore;
    @Schema(description = "交易总数")
    private Integer totalTransactions;
    @Schema(description = "卖出总数")
    private Integer totalSales;
    @Schema(description = "购买总数")
    private Integer totalPurchases;
    @Schema(description = "性别（0-未知，1-男，2-女）")
    private Integer gender;
    @Schema(description = "生日")
    private LocalDate birthday;
    @Schema(description = "个人简介")
    private String bio;
    @Schema(description = "学校名称")
    private String schoolName;

    /**
     * 从实体转换为DTO
     *
     * @param user 用户实体
     * @return 脱敏后的 UserDTO
     */
    public static UserDTO fromEntity(User user) {
        return fromEntity(user, true);
    }

    /**
     * 从实体转换为DTO
     *
     * @param user 用户实体
     * @param mask 是否对敏感数据进行脱敏
     * @return UserDTO
     */
    public static UserDTO fromEntity(User user, boolean mask) {
        if (user == null) {
            return null;
        }
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(mask ? DataMaskUtil.maskEmail(user.getEmail()) : user.getEmail())
                .phone(mask ? DataMaskUtil.maskPhone(user.getPhone()) : user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .status(user.getStatus())
                .verified(user.getVerified())
                .studentId(user.getStudentId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginTime(user.getLastLoginTime())
                .loginCount(user.getLoginCount())
                .creditScore(user.getCreditScore())
                .totalTransactions(user.getTotalTransactions())
                .totalSales(user.getTotalSales())
                .totalPurchases(user.getTotalPurchases())
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .bio(user.getBio())
                .schoolName(user.getSchoolName())
                .build();
    }

    /**
     * 从实体转换为DTO，不进行脱敏（用于需要完整信息的场景，如管理员查看）
     */
    public static UserDTO fromEntityWithoutMask(User user) {
        return fromEntity(user, false);
    }
}
