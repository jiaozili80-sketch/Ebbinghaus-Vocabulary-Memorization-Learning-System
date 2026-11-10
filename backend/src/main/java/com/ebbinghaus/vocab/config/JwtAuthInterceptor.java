package com.ebbinghaus.vocab.config;

import com.ebbinghaus.vocab.context.LoginUserContext;
import com.ebbinghaus.vocab.domain.entity.User;
import com.ebbinghaus.vocab.mapper.UserMapper;
import com.ebbinghaus.vocab.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final String LOGIN_TOKEN_KEY_PREFIX = "login:token:";

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        try {
            String token = authorization.substring(7);
            Long userId = jwtUtil.getUserId(token);
            User user = userMapper.selectById(userId);
            if (user == null || Integer.valueOf(1).equals(user.getDeleted()) || Integer.valueOf(0).equals(user.getStatus())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            if (!isCurrentLoginToken(userId, token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            LoginUserContext.setUserId(userId);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    private boolean isCurrentLoginToken(Long userId, String token) {
        try {
            String cachedToken = stringRedisTemplate.opsForValue().get(LOGIN_TOKEN_KEY_PREFIX + userId);
            return token.equals(cachedToken);
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUserContext.clear();
    }
}
