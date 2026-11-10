package com.ebbinghaus.vocab.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ebbinghaus.vocab.common.ApiResult;
import com.ebbinghaus.vocab.domain.entity.OperationLog;
import com.ebbinghaus.vocab.domain.vo.AdminDashboardVO;
import com.ebbinghaus.vocab.domain.vo.AdminUserVO;
import com.ebbinghaus.vocab.mapper.OperationLogMapper;
import com.ebbinghaus.vocab.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final OperationLogMapper operationLogMapper;

    @GetMapping("/dashboard")
    public ApiResult<AdminDashboardVO> dashboard() {
        return ApiResult.success(adminService.dashboard());
    }

    @GetMapping("/users/page")
    public ApiResult<IPage<AdminUserVO>> userPage(@RequestParam(defaultValue = "1") long current,
                                                  @RequestParam(defaultValue = "10") long size,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) Integer status) {
        return ApiResult.success(adminService.userPage(current, size, keyword, status));
    }

    @PostMapping("/users/{id}/status")
    public ApiResult<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminService.updateUserStatus(id, status);
        return ApiResult.success();
    }

    @GetMapping("/logs/page")
    public ApiResult<IPage<OperationLog>> logPage(@RequestParam(defaultValue = "1") long current,
                                                  @RequestParam(defaultValue = "10") long size) {
        return ApiResult.success(operationLogMapper.selectPage(
                new Page<>(current, size),
                new LambdaQueryWrapper<OperationLog>().orderByDesc(OperationLog::getCreateTime)
        ));
    }
}
