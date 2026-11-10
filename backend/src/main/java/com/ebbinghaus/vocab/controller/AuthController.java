package com.ebbinghaus.vocab.controller;

import com.ebbinghaus.vocab.common.ApiResult;
import com.ebbinghaus.vocab.context.LoginUserContext;
import com.ebbinghaus.vocab.domain.dto.LoginRequest;
import com.ebbinghaus.vocab.domain.dto.RegisterRequest;
import com.ebbinghaus.vocab.domain.vo.LoginVO;
import com.ebbinghaus.vocab.domain.vo.UserInfoVO;
import com.ebbinghaus.vocab.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResult<UserInfoVO> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResult.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResult<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return ApiResult.success(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResult<Void> logout() {
        authService.logout(LoginUserContext.getUserId());
        return ApiResult.success();
    }

    @GetMapping("/me")
    public ApiResult<UserInfoVO> me() {
        return ApiResult.success(authService.getUserInfo(LoginUserContext.getUserId()));
    }
}
