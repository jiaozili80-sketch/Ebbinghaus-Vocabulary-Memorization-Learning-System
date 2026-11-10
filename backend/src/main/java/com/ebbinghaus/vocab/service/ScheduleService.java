package com.ebbinghaus.vocab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ebbinghaus.vocab.context.LoginUserContext;
import com.ebbinghaus.vocab.domain.dto.CheckinRequest;
import com.ebbinghaus.vocab.domain.dto.DailyGoalRequest;
import com.ebbinghaus.vocab.domain.entity.CheckinRecord;
import com.ebbinghaus.vocab.domain.entity.DailyGoal;
import com.ebbinghaus.vocab.domain.entity.StudyRecord;
import com.ebbinghaus.vocab.domain.entity.User;
import com.ebbinghaus.vocab.domain.vo.CalendarDayVO;
import com.ebbinghaus.vocab.domain.vo.ScheduleOverviewVO;
import com.ebbinghaus.vocab.mapper.CheckinRecordMapper;
import com.ebbinghaus.vocab.mapper.DailyGoalMapper;
import com.ebbinghaus.vocab.mapper.StudyRecordMapper;
import com.ebbinghaus.vocab.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final CheckinRecordMapper checkinRecordMapper;
    private final DailyGoalMapper dailyGoalMapper;
    private final StudyRecordMapper studyRecordMapper;
    private final UserMapper userMapper;

    public ScheduleOverviewVO overview() {
        Long userId = currentUserId();
        LocalDate today = LocalDate.now();
        DailyGoal goal = getOrCreateGoal(userId);
        StudyRecord todayRecord = findStudyRecord(userId, today);
        CheckinRecord checkin = findCheckin(userId, today);
        int todayWordCount = todayRecord == null ? 0 : todayRecord.getNewWordCount() + todayRecord.getReviewWordCount();
        int todayDuration = todayRecord == null ? 0 : todayRecord.getStudyDuration();
        User user = userMapper.selectById(userId);
        return ScheduleOverviewVO.builder()
                .checkedInToday(checkin != null)
                .continuousCheckinDays(user == null ? 0 : user.getContinuousCheckinDays())
                .todayWordCount(todayWordCount)
                .todayStudyDuration(todayDuration)
                .targetWordCount(goal.getTargetWordCount())
                .targetStudyMinutes(goal.getTargetStudyMinutes())
                .wordProgress(percent(todayWordCount, goal.getTargetWordCount()))
                .durationProgress(percent(todayDuration / 60.0, goal.getTargetStudyMinutes()))
                .build();
    }

    public DailyGoal getGoal() {
        return getOrCreateGoal(currentUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    public DailyGoal updateGoal(DailyGoalRequest request) {
        Long userId = currentUserId();
        DailyGoal goal = getOrCreateGoal(userId);
        goal.setTargetWordCount(request.getTargetWordCount());
        goal.setTargetStudyMinutes(request.getTargetStudyMinutes());
        goal.setUpdateTime(LocalDateTime.now());
        dailyGoalMapper.updateById(goal);
        return goal;
    }

    @Transactional(rollbackFor = Exception.class)
    public CheckinRecord checkin(CheckinRequest request) {
        Long userId = currentUserId();
        LocalDate today = LocalDate.now();
        CheckinRecord exists = findCheckin(userId, today);
        if (exists != null) {
            throw new IllegalArgumentException("今日已经打卡");
        }
        ScheduleOverviewVO overview = overview();
        boolean completed = overview.getTodayWordCount() >= overview.getTargetWordCount()
                || overview.getTodayStudyDuration() >= overview.getTargetStudyMinutes() * 60;
        CheckinRecord record = new CheckinRecord();
        record.setUserId(userId);
        record.setCheckinDate(today);
        record.setStudyDuration(request.getStudyDuration() == null ? overview.getTodayStudyDuration() : request.getStudyDuration());
        record.setCompletedTarget(completed ? 1 : 0);
        record.setRemark(request.getRemark());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        checkinRecordMapper.insert(record);
        refreshContinuousDays(userId);
        return record;
    }

    public List<CalendarDayVO> calendar(Integer year, Integer month) {
        Long userId = currentUserId();
        YearMonth yearMonth = YearMonth.of(year == null ? LocalDate.now().getYear() : year,
                month == null ? LocalDate.now().getMonthValue() : month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        Map<LocalDate, CheckinRecord> checkinMap = checkinRecordMapper.selectList(new LambdaQueryWrapper<CheckinRecord>()
                        .eq(CheckinRecord::getUserId, userId)
                        .between(CheckinRecord::getCheckinDate, start, end))
                .stream().collect(Collectors.toMap(CheckinRecord::getCheckinDate, Function.identity()));
        Map<LocalDate, StudyRecord> studyMap = studyRecordMapper.selectList(new LambdaQueryWrapper<StudyRecord>()
                        .eq(StudyRecord::getUserId, userId)
                        .between(StudyRecord::getStudyDate, start, end))
                .stream().collect(Collectors.toMap(StudyRecord::getStudyDate, Function.identity()));
        return start.datesUntil(end.plusDays(1)).map(date -> {
            StudyRecord study = studyMap.get(date);
            return CalendarDayVO.builder()
                    .date(date)
                    .checkedIn(checkinMap.containsKey(date))
                    .newWordCount(study == null ? 0 : study.getNewWordCount())
                    .reviewWordCount(study == null ? 0 : study.getReviewWordCount())
                    .masteredWordCount(study == null ? 0 : study.getMasteredWordCount())
                    .wrongWordCount(study == null ? 0 : study.getWrongWordCount())
                    .studyDuration(study == null ? 0 : study.getStudyDuration())
                    .build();
        }).toList();
    }

    private DailyGoal getOrCreateGoal(Long userId) {
        DailyGoal goal = dailyGoalMapper.selectOne(new LambdaQueryWrapper<DailyGoal>()
                .eq(DailyGoal::getUserId, userId)
                .last("LIMIT 1"));
        if (goal != null) {
            return goal;
        }
        goal = new DailyGoal();
        goal.setUserId(userId);
        goal.setTargetWordCount(30);
        goal.setTargetStudyMinutes(30);
        goal.setCreateTime(LocalDateTime.now());
        goal.setUpdateTime(LocalDateTime.now());
        dailyGoalMapper.insert(goal);
        return goal;
    }

    private CheckinRecord findCheckin(Long userId, LocalDate date) {
        return checkinRecordMapper.selectOne(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getUserId, userId)
                .eq(CheckinRecord::getCheckinDate, date)
                .last("LIMIT 1"));
    }

    private StudyRecord findStudyRecord(Long userId, LocalDate date) {
        return studyRecordMapper.selectOne(new LambdaQueryWrapper<StudyRecord>()
                .eq(StudyRecord::getUserId, userId)
                .eq(StudyRecord::getStudyDate, date)
                .last("LIMIT 1"));
    }

    private void refreshContinuousDays(Long userId) {
        LocalDate cursor = LocalDate.now();
        int days = 0;
        while (findCheckin(userId, cursor) != null) {
            days++;
            cursor = cursor.minusDays(1);
        }
        User user = new User();
        user.setId(userId);
        user.setContinuousCheckinDays(days);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    private double percent(double value, double target) {
        if (target <= 0) {
            return 0;
        }
        return Math.min(100, Math.round(value * 1000.0 / target) / 10.0);
    }

    private Long currentUserId() {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("请先登录");
        }
        return userId;
    }
}
