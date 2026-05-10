package com.idleitems.school.dto.admin;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;

    private String role;

    private String status;

    private String nickname;

    private String studentId;

    private Integer gender;

    private String bio;

    private String schoolName;
}
