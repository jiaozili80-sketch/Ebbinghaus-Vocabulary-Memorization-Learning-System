package com.ebbinghaus.vocab.domain.vo;

import com.ebbinghaus.vocab.domain.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserVO {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String role;
    private Integer status;
    private Integer totalWords;
    private Integer masteredWords;
    private Integer continuousCheckinDays;
    private LocalDateTime createTime;

    public static AdminUserVO from(User user) {
        return AdminUserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .totalWords(user.getTotalWords())
                .masteredWords(user.getMasteredWords())
                .continuousCheckinDays(user.getContinuousCheckinDays())
                .createTime(user.getCreateTime())
                .build();
    }
}
