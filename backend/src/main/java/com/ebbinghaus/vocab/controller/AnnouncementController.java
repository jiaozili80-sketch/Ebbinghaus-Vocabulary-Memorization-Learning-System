package com.ebbinghaus.vocab.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ebbinghaus.vocab.common.ApiResult;
import com.ebbinghaus.vocab.domain.dto.AnnouncementRequest;
import com.ebbinghaus.vocab.domain.entity.Announcement;
import com.ebbinghaus.vocab.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ApiResult<List<Announcement>> enabledList(@RequestParam(required = false) String type) {
        return ApiResult.success(announcementService.enabledList(type));
    }

    @GetMapping("/page")
    public ApiResult<IPage<Announcement>> page(@RequestParam(defaultValue = "1") long current,
                                               @RequestParam(defaultValue = "10") long size,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String type,
                                               @RequestParam(required = false) Integer status) {
        return ApiResult.success(announcementService.page(current, size, keyword, type, status));
    }

    @PostMapping
    public ApiResult<Announcement> create(@Valid @RequestBody AnnouncementRequest request) {
        return ApiResult.success(announcementService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResult<Announcement> update(@PathVariable Long id, @Valid @RequestBody AnnouncementRequest request) {
        return ApiResult.success(announcementService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return ApiResult.success();
    }
}
