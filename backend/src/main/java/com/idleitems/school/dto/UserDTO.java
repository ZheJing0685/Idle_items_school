package com.idleitems.school.dto;

import com.idleitems.school.entity.User;
import com.idleitems.school.util.DataMaskUtil;
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
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String nickname;
    private String avatar;
    private User.Role role;
    private User.UserStatus status;
    private Boolean verified;
    private String studentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginTime;
    private Integer loginCount;
    private Integer creditScore;
    private Integer totalTransactions;
    private Integer totalSales;
    private Integer totalPurchases;
    private Integer gender;
    private LocalDate birthday;
    private String bio;
    private String schoolName;

    /**
     * 从实体转换为DTO，对敏感数据进行脱敏
     *
     * @param user 用户实体
     * @return 脱敏后的UserDTO
     */
    public static UserDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(DataMaskUtil.maskEmail(user.getEmail()))
                .phone(DataMaskUtil.maskPhone(user.getPhone()))
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
     *
     * @param user 用户实体
     * @return 完整信息的UserDTO
     */
    public static UserDTO fromEntityWithoutMask(User user) {
        if (user == null) {
            return null;
        }
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
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
}
