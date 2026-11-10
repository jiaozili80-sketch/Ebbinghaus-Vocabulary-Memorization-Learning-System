package com.ebbinghaus.vocab.config;

import com.ebbinghaus.vocab.context.LoginUserContext;
import com.ebbinghaus.vocab.domain.entity.User;
import com.ebbinghaus.vocab.mapper.UserMapper;
import com.ebbinghaus.vocab.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthInterceptorTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserMapper userMapper;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private JwtAuthInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new JwtAuthInterceptor(jwtUtil, userMapper, redisTemplate);
        when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        when(jwtUtil.getUserId("test-token")).thenReturn(1L);

        User user = new User();
        user.setId(1L);
        user.setStatus(1);
        user.setDeleted(0);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @AfterEach
    void tearDown() {
        LoginUserContext.clear();
    }

    @Test
    void rejectsTokenWhenSessionWasRemoved() {
        when(valueOperations.get("login:token:1")).thenReturn(null);

        assertFalse(interceptor.preHandle(request, response, new Object()));
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void acceptsOnlyTheCurrentCachedToken() {
        when(valueOperations.get("login:token:1")).thenReturn("test-token");

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }
}
