package com.ebbinghaus.vocab.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {

    private Long id;
    private String username;
    @JsonIgnore
    private String password;
    private String nickname;
    private String avatar;
    private String email;
    private String role;
    private Integer status;
    private Integer totalWords;
    private Integer masteredWords;
    private Integer continuousCheckinDays;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
