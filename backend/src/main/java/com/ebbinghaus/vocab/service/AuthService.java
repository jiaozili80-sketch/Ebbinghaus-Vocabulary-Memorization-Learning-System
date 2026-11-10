package com.ebbinghaus.vocab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ebbinghaus.vocab.domain.dto.LoginRequest;
import com.ebbinghaus.vocab.domain.dto.RegisterRequest;
import com.ebbinghaus.vocab.domain.entity.User;
import com.ebbinghaus.vocab.domain.vo.LoginVO;
import com.ebbinghaus.vocab.domain.vo.UserInfoVO;
import com.ebbinghaus.vocab.mapper.UserMapper;
import com.ebbinghaus.vocab.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String LOGIN_TOKEN_KEY_PREFIX = "login:token:";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    public UserInfoVO register(RegisterRequest request) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .eq(User::getDeleted, 0));
        if (count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole("USER");
        user.setStatus(1);
        user.setTotalWords(0);
        user.setMasteredWords(0);
        user.setContinuousCheckinDays(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDeleted(0);
        userMapper.insert(user);
        return UserInfoVO.from(user);
    }

    public LoginVO login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .eq(User::getDeleted, 0)
                .last("LIMIT 1"));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (Integer.valueOf(0).equals(user.getStatus())) {
            throw new IllegalArgumentException("账号已被封禁，请联系管理员");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        cacheLoginToken(user.getId(), token);

        return LoginVO.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpiration() / 1000)
                .user(UserInfoVO.from(user))
                .build();
    }

    public void logout(Long userId) {
        if (userId != null) {
            stringRedisTemplate.delete(LOGIN_TOKEN_KEY_PREFIX + userId);
        }
    }

    private void cacheLoginToken(Long userId, String token) {
        stringRedisTemplate.opsForValue().set(
                LOGIN_TOKEN_KEY_PREFIX + userId,
                token,
                Duration.ofMillis(jwtUtil.getExpiration())
        );
    }

    public UserInfoVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new IllegalArgumentException("用户不存在");
        }
        return UserInfoVO.from(user);
    }
}
