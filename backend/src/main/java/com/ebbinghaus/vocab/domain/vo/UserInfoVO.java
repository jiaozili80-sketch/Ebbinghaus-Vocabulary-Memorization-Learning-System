package com.ebbinghaus.vocab.domain.vo;

import com.ebbinghaus.vocab.domain.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String role;
    private Integer totalWords;
    private Integer masteredWords;
    private Integer continuousCheckinDays;

    public static UserInfoVO from(User user) {
        return UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .role(user.getRole())
                .totalWords(user.getTotalWords())
                .masteredWords(user.getMasteredWords())
                .continuousCheckinDays(user.getContinuousCheckinDays())
                .build();
    }
}
