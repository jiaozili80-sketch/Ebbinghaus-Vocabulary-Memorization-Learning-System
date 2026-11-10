package com.ebbinghaus.vocab.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度必须为4-20位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 30, message = "密码长度必须为6-30位")
    private String password;

    @Size(max = 30, message = "昵称不能超过30个字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;
}
