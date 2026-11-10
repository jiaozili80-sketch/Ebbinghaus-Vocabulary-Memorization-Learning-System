package com.ebbinghaus.vocab.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ebbinghaus.vocab.domain.entity.User;
import com.ebbinghaus.vocab.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.admin.bootstrap.enabled", havingValue = "true")
@Order(0)
public class AdminInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.bootstrap.username:admin}")
    private String adminUsername;

    @Value("${app.admin.bootstrap.password:}")
    private String adminPassword;

    @Value("${app.admin.bootstrap.email:}")
    private String adminEmail;

    @Override
    public void run(String... args) {
        if (!StringUtils.hasText(adminPassword) || adminPassword.length() < 12) {
            throw new IllegalStateException("启用管理员初始化时，ADMIN_BOOTSTRAP_PASSWORD 必须至少为 12 位");
        }
        User existingAdmin = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, adminUsername)
                .eq(User::getDeleted, 0)
                .last("LIMIT 1"));
        if (existingAdmin != null) {
            if (passwordEncoder.matches("admin123", existingAdmin.getPassword())) {
                existingAdmin.setPassword(passwordEncoder.encode(adminPassword));
                existingAdmin.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(existingAdmin);
            }
            return;
        }

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setNickname("系统管理员");
        admin.setEmail(StringUtils.hasText(adminEmail) ? adminEmail : null);
        admin.setRole("ADMIN");
        admin.setStatus(1);
        admin.setTotalWords(0);
        admin.setMasteredWords(0);
        admin.setContinuousCheckinDays(0);
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        admin.setDeleted(0);
        userMapper.insert(admin);
    }
}
