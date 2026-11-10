package com.ebbinghaus.vocab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ebbinghaus.vocab.context.LoginUserContext;
import com.ebbinghaus.vocab.domain.dto.StudyPlanRequest;
import com.ebbinghaus.vocab.domain.entity.StudyPlan;
import com.ebbinghaus.vocab.domain.entity.WordBook;
import com.ebbinghaus.vocab.domain.vo.StudyPlanVO;
import com.ebbinghaus.vocab.mapper.StudyPlanMapper;
import com.ebbinghaus.vocab.mapper.WordBookMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StudyPlanService {

    private final StudyPlanMapper studyPlanMapper;
    private final WordBookMapper wordBookMapper;

    public StudyPlanVO current() {
        Long userId = currentUserId();
        StudyPlan plan = studyPlanMapper.selectOne(new LambdaQueryWrapper<StudyPlan>()
                .eq(StudyPlan::getUserId, userId)
                .eq(StudyPlan::getDeleted, 0)
                .orderByDesc(StudyPlan::getUpdateTime)
                .last("LIMIT 1"));
        if (plan == null) {
            return null;
        }
        return toVO(plan);
    }

    @Transactional(rollbackFor = Exception.class)
    public StudyPlanVO save(StudyPlanRequest request) {
        Long userId = currentUserId();
        WordBook book = wordBookMapper.selectById(request.getBookId());
        if (book == null || Integer.valueOf(1).equals(book.getDeleted())) {
            throw new IllegalArgumentException("词库不存在");
        }
        if (Integer.valueOf(0).equals(book.getStatus())) {
            throw new IllegalArgumentException("词库已停用");
        }
        if (Integer.valueOf(2).equals(book.getType()) && !userId.equals(book.getUserId())) {
            throw new IllegalArgumentException("无权使用该私人词库");
        }
        StudyPlan plan = studyPlanMapper.selectOne(new LambdaQueryWrapper<StudyPlan>()
                .eq(StudyPlan::getUserId, userId)
                .eq(StudyPlan::getDeleted, 0)
                .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();
        boolean insert = plan == null;
        if (insert) {
            plan = new StudyPlan();
            plan.setUserId(userId);
            plan.setCreateTime(now);
            plan.setDeleted(0);
        }
        fillPlan(plan, request);
        plan.setUpdateTime(now);
        if (insert) {
            studyPlanMapper.insert(plan);
        } else {
            studyPlanMapper.updateById(plan);
        }
        return StudyPlanVO.of(plan, book);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete() {
        Long userId = currentUserId();
        StudyPlan plan = studyPlanMapper.selectOne(new LambdaQueryWrapper<StudyPlan>()
                .eq(StudyPlan::getUserId, userId)
                .eq(StudyPlan::getDeleted, 0)
                .last("LIMIT 1"));
        if (plan != null) {
            studyPlanMapper.deleteById(plan.getId());
        }
    }

    private void fillPlan(StudyPlan plan, StudyPlanRequest request) {
        int dailyReviewCount = request.getPlanWordCount() * request.getReviewMultiplier();
        plan.setBookId(request.getBookId());
        plan.setPlanWordCount(request.getPlanWordCount());
        plan.setReviewMultiplier(request.getReviewMultiplier());
        plan.setDailyReviewCount(dailyReviewCount);
        plan.setDailyTotalCount(request.getPlanWordCount() + dailyReviewCount);
    }

    private StudyPlanVO toVO(StudyPlan plan) {
        WordBook book = wordBookMapper.selectById(plan.getBookId());
        return StudyPlanVO.of(plan, book);
    }

    private Long currentUserId() {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("请先登录");
        }
        return userId;
    }
}
