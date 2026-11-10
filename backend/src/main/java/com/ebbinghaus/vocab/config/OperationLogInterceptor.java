package com.ebbinghaus.vocab.config;

import com.ebbinghaus.vocab.context.LoginUserContext;
import com.ebbinghaus.vocab.domain.entity.OperationLog;
import com.ebbinghaus.vocab.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogInterceptor implements HandlerInterceptor {

    private final OperationLogMapper operationLogMapper;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if ("GET".equalsIgnoreCase(request.getMethod()) || request.getRequestURI().contains("/logs/page")) {
            return;
        }
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            return;
        }
        OperationLog operationLog = new OperationLog();
        operationLog.setUserId(userId);
        operationLog.setModule(resolveModule(request.getRequestURI()));
        operationLog.setOperation(request.getMethod());
        operationLog.setRequestUri(request.getRequestURI());
        operationLog.setRequestMethod(request.getMethod());
        operationLog.setIp(request.getRemoteAddr());
        operationLog.setCreateTime(LocalDateTime.now());
        try {
            operationLogMapper.insert(operationLog);
        } catch (Exception logException) {
            log.warn("Failed to save operation log", logException);
        }
    }

    private String resolveModule(String uri) {
        if (uri.contains("/word-books")) return "词库管理";
        if (uri.contains("/words")) return "单词管理";
        if (uri.contains("/announcements")) return "公告管理";
        if (uri.contains("/admin/users")) return "用户管理";
        if (uri.contains("/study")) return "学习背诵";
        if (uri.contains("/schedule")) return "学习日程";
        return "系统操作";
    }
}
