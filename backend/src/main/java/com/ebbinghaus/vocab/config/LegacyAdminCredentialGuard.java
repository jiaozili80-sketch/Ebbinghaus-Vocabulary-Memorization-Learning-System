package com.ebbinghaus.vocab.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ebbinghaus.vocab.domain.entity.User;
import com.ebbinghaus.vocab.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(100)
public class LegacyAdminCredentialGuard implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        User legacyAdmin = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, "admin")
                .eq(User::getRole, "ADMIN")
                .eq(User::getDeleted, 0)
                .last("LIMIT 1"));
        if (legacyAdmin != null && passwordEncoder.matches("admin123", legacyAdmin.getPassword())) {
            throw new IllegalStateException(
                    "检测到旧版默认管理员密码。请设置 ADMIN_BOOTSTRAP_ENABLED=true 和强 ADMIN_BOOTSTRAP_PASSWORD 后重新启动"
            );
        }
    }
}
