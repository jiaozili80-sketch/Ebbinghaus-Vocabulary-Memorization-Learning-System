package com.ebbinghaus.vocab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ebbinghaus.vocab.context.LoginUserContext;
import com.ebbinghaus.vocab.domain.dto.AnnouncementRequest;
import com.ebbinghaus.vocab.domain.entity.Announcement;
import com.ebbinghaus.vocab.mapper.AnnouncementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    public List<Announcement> enabledList(String type) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, 1)
                .eq(Announcement::getDeleted, 0)
                .orderByDesc(Announcement::getCreateTime);
        if (StringUtils.hasText(type)) {
            queryWrapper.eq(Announcement::getType, type);
        }
        return announcementMapper.selectList(queryWrapper);
    }

    public IPage<Announcement> page(long current, long size, String keyword, String type, Integer status) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getDeleted, 0)
                .orderByDesc(Announcement::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like(Announcement::getTitle, keyword)
                    .or()
                    .like(Announcement::getContent, keyword));
        }
        if (StringUtils.hasText(type)) {
            queryWrapper.eq(Announcement::getType, type);
        }
        if (status != null) {
            queryWrapper.eq(Announcement::getStatus, status);
        }
        return announcementMapper.selectPage(new Page<>(current, size), queryWrapper);
    }

    public Announcement create(AnnouncementRequest request) {
        Announcement announcement = new Announcement();
        fill(announcement, request);
        announcement.setCreateBy(LoginUserContext.getUserId());
        announcement.setCreateTime(LocalDateTime.now());
        announcement.setUpdateTime(LocalDateTime.now());
        announcement.setDeleted(0);
        announcementMapper.insert(announcement);
        return announcement;
    }

    public Announcement update(Long id, AnnouncementRequest request) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null || Integer.valueOf(1).equals(announcement.getDeleted())) {
            throw new IllegalArgumentException("公告不存在");
        }
        fill(announcement, request);
        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);
        return announcement;
    }

    public void delete(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new IllegalArgumentException("公告不存在");
        }
        announcementMapper.deleteById(id);
    }

    private void fill(Announcement announcement, AnnouncementRequest request) {
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setType(request.getType());
        announcement.setStatus(request.getStatus());
    }
}
