package com.ebbinghaus.vocab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ebbinghaus.vocab.domain.entity.CheckinRecord;
import com.ebbinghaus.vocab.domain.entity.ReviewPlan;
import com.ebbinghaus.vocab.domain.entity.StudyRecord;
import com.ebbinghaus.vocab.domain.entity.User;
import com.ebbinghaus.vocab.domain.entity.Word;
import com.ebbinghaus.vocab.domain.entity.WordBook;
import com.ebbinghaus.vocab.domain.entity.WrongWord;
import com.ebbinghaus.vocab.domain.vo.AdminDashboardVO;
import com.ebbinghaus.vocab.domain.vo.AdminUserVO;
import com.ebbinghaus.vocab.mapper.CheckinRecordMapper;
import com.ebbinghaus.vocab.mapper.ReviewPlanMapper;
import com.ebbinghaus.vocab.mapper.StudyRecordMapper;
import com.ebbinghaus.vocab.mapper.UserMapper;
import com.ebbinghaus.vocab.mapper.WordBookMapper;
import com.ebbinghaus.vocab.mapper.WordMapper;
import com.ebbinghaus.vocab.mapper.WrongWordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserMapper userMapper;
    private final WordBookMapper wordBookMapper;
    private final WordMapper wordMapper;
    private final StudyRecordMapper studyRecordMapper;
    private final CheckinRecordMapper checkinRecordMapper;
    private final WrongWordMapper wrongWordMapper;
    private final ReviewPlanMapper reviewPlanMapper;

    public AdminDashboardVO dashboard() {
        return AdminDashboardVO.builder()
                .userCount(userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getDeleted, 0)))
                .activeUserCount(userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, 1).eq(User::getDeleted, 0)))
                .wordBookCount(wordBookMapper.selectCount(new LambdaQueryWrapper<WordBook>().eq(WordBook::getDeleted, 0)))
                .wordCount(wordMapper.selectCount(new LambdaQueryWrapper<Word>().eq(Word::getDeleted, 0)))
                .todayStudyCount(studyRecordMapper.selectCount(new LambdaQueryWrapper<StudyRecord>().eq(StudyRecord::getStudyDate, LocalDate.now())))
                .todayCheckinCount(checkinRecordMapper.selectCount(new LambdaQueryWrapper<CheckinRecord>().eq(CheckinRecord::getCheckinDate, LocalDate.now())))
                .wrongWordCount(wrongWordMapper.selectCount(new LambdaQueryWrapper<WrongWord>().eq(WrongWord::getStatus, 1).eq(WrongWord::getDeleted, 0)))
                .reviewPlanCount(reviewPlanMapper.selectCount(new LambdaQueryWrapper<ReviewPlan>().eq(ReviewPlan::getDeleted, 0)))
                .build();
    }

    public IPage<AdminUserVO> userPage(long current, long size, String keyword, Integer status) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getDeleted, 0)
                .orderByDesc(User::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }
        if (status != null) {
            queryWrapper.eq(User::getStatus, status);
        }
        return userMapper.selectPage(new Page<>(current, size), queryWrapper)
                .convert(AdminUserVO::from);
    }

    public void updateUserStatus(Long id, Integer status) {
        if (!Integer.valueOf(0).equals(status) && !Integer.valueOf(1).equals(status)) {
            throw new IllegalArgumentException("用户状态不合法");
        }
        User user = userMapper.selectById(id);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }
}
