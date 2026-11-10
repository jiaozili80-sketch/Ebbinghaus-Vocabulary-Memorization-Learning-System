package com.ebbinghaus.vocab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ebbinghaus.vocab.context.LoginUserContext;
import com.ebbinghaus.vocab.domain.dto.StudySubmitRequest;
import com.ebbinghaus.vocab.domain.entity.ReviewPlan;
import com.ebbinghaus.vocab.domain.entity.StudyRecord;
import com.ebbinghaus.vocab.domain.entity.StudyPlan;
import com.ebbinghaus.vocab.domain.entity.User;
import com.ebbinghaus.vocab.domain.entity.Word;
import com.ebbinghaus.vocab.domain.entity.WordBook;
import com.ebbinghaus.vocab.domain.entity.WrongWord;
import com.ebbinghaus.vocab.domain.vo.StudyOverviewVO;
import com.ebbinghaus.vocab.domain.vo.StudySubmitVO;
import com.ebbinghaus.vocab.domain.vo.StudyWordVO;
import com.ebbinghaus.vocab.mapper.ReviewPlanMapper;
import com.ebbinghaus.vocab.mapper.StudyRecordMapper;
import com.ebbinghaus.vocab.mapper.StudyPlanMapper;
import com.ebbinghaus.vocab.mapper.UserMapper;
import com.ebbinghaus.vocab.mapper.WordBookMapper;
import com.ebbinghaus.vocab.mapper.WordMapper;
import com.ebbinghaus.vocab.mapper.WrongWordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final WordMapper wordMapper;
    private final ReviewPlanMapper reviewPlanMapper;
    private final WrongWordMapper wrongWordMapper;
    private final StudyRecordMapper studyRecordMapper;
    private final StudyPlanMapper studyPlanMapper;
    private final ReviewScheduleService reviewScheduleService;
    private final UserMapper userMapper;
    private final WordBookMapper wordBookMapper;

    public StudyOverviewVO overview(Long bookId) {
        Long userId = currentUserId();
        bookId = resolveBookId(userId, bookId);
        if (bookId != null) {
            ensureBookAccessible(bookId, userId);
        }
        LocalDateTime now = LocalDateTime.now();
        List<Long> bookWordIds = findBookWordIds(bookId);
        if (bookWordIds != null && bookWordIds.isEmpty()) {
            return StudyOverviewVO.builder()
                    .newWordCount(0L)
                    .reviewWordCount(0L)
                    .masteredWordCount(0L)
                    .wrongWordCount(0L)
                    .build();
        }
        Long learnedCount = reviewPlanMapper.selectCount(new LambdaQueryWrapper<ReviewPlan>()
                .eq(ReviewPlan::getUserId, userId)
                .isNotNull(ReviewPlan::getFirstStudyTime)
                .in(bookWordIds != null, ReviewPlan::getWordId, bookWordIds)
                .eq(ReviewPlan::getDeleted, 0));
        Long totalWordCount = wordMapper.selectCount(new LambdaQueryWrapper<Word>()
                .eq(bookId != null, Word::getBookId, bookId)
                .eq(Word::getStatus, 1)
                .eq(Word::getDeleted, 0));
        Long reviewCount = reviewPlanMapper.selectCount(new LambdaQueryWrapper<ReviewPlan>()
                .eq(ReviewPlan::getUserId, userId)
                .eq(ReviewPlan::getStatus, 0)
                .le(ReviewPlan::getNextReviewTime, now)
                .in(bookWordIds != null, ReviewPlan::getWordId, bookWordIds)
                .eq(ReviewPlan::getDeleted, 0));
        Long masteredCount = reviewPlanMapper.selectCount(new LambdaQueryWrapper<ReviewPlan>()
                .eq(ReviewPlan::getUserId, userId)
                .eq(ReviewPlan::getStatus, 1)
                .in(bookWordIds != null, ReviewPlan::getWordId, bookWordIds)
                .eq(ReviewPlan::getDeleted, 0));
        Long wrongCount = wrongWordMapper.selectCount(new LambdaQueryWrapper<WrongWord>()
                .eq(WrongWord::getUserId, userId)
                .eq(WrongWord::getStatus, 1)
                .in(bookWordIds != null, WrongWord::getWordId, bookWordIds)
                .eq(WrongWord::getDeleted, 0));
        return StudyOverviewVO.builder()
                .newWordCount(Math.max(totalWordCount - learnedCount, 0))
                .reviewWordCount(reviewCount)
                .masteredWordCount(masteredCount)
                .wrongWordCount(wrongCount)
                .build();
    }

    public List<StudyWordVO> todayTasks(Long bookId, Integer limit) {
        Long userId = currentUserId();
        bookId = resolveBookId(userId, bookId);
        if (bookId != null) {
            ensureBookAccessible(bookId, userId);
        }
        int taskLimit = resolveTaskLimit(userId, bookId, limit, false);
        List<Long> bookWordIds = findBookWordIds(bookId);
        if (bookWordIds != null && bookWordIds.isEmpty()) {
            return List.of();
        }
        List<ReviewPlan> plans = reviewPlanMapper.selectList(new LambdaQueryWrapper<ReviewPlan>()
                .eq(ReviewPlan::getUserId, userId)
                .eq(ReviewPlan::getStatus, 0)
                .le(ReviewPlan::getNextReviewTime, LocalDateTime.now())
                .in(bookWordIds != null, ReviewPlan::getWordId, bookWordIds)
                .eq(ReviewPlan::getDeleted, 0)
                .orderByAsc(ReviewPlan::getNextReviewTime)
                .last("LIMIT " + taskLimit));
        if (plans.isEmpty()) {
            return List.of();
        }
        List<Long> wordIds = plans.stream().map(ReviewPlan::getWordId).toList();
        Map<Long, Word> wordMap = wordMapper.selectBatchIds(wordIds).stream()
                .collect(Collectors.toMap(Word::getId, Function.identity()));
        return plans.stream()
                .filter(plan -> wordMap.containsKey(plan.getWordId()))
                .map(plan -> StudyWordVO.of(wordMap.get(plan.getWordId()), plan))
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public List<StudyWordVO> startNew(Long bookId, Integer limit) {
        Long userId = currentUserId();
        bookId = resolveBookId(userId, bookId);
        if (bookId == null) {
            throw new IllegalArgumentException("请选择词库");
        }
        ensureBookAccessible(bookId, userId);
        int taskLimit = resolveTaskLimit(userId, bookId, limit, true);

        // 防重复：复用尚未提交过答案的 plan，避免中断学习后留下永远无法再次选择的单词。
        List<Long> bookWordIds = findBookWordIds(bookId);
        if (bookWordIds != null && bookWordIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<ReviewPlan> pendingQuery = new LambdaQueryWrapper<ReviewPlan>()
                .eq(ReviewPlan::getUserId, userId)
                .isNull(ReviewPlan::getFirstStudyTime)
                .eq(ReviewPlan::getDeleted, 0)
                .orderByAsc(ReviewPlan::getCreateTime);
        if (bookWordIds != null) {
            pendingQuery.in(ReviewPlan::getWordId, bookWordIds);
        }
        pendingQuery.last("LIMIT " + taskLimit);
        List<ReviewPlan> pendingPlans = reviewPlanMapper.selectList(pendingQuery);
        if (!pendingPlans.isEmpty()) {
            List<Long> wordIds = pendingPlans.stream().map(ReviewPlan::getWordId).toList();
            Map<Long, Word> wordMap = wordMapper.selectBatchIds(wordIds).stream()
                    .collect(Collectors.toMap(Word::getId, Function.identity()));
            return pendingPlans.stream()
                    .filter(plan -> wordMap.containsKey(plan.getWordId()))
                    .map(plan -> StudyWordVO.of(wordMap.get(plan.getWordId()), plan))
                    .toList();
        }

        List<Long> learnedWordIds = reviewPlanMapper.selectList(new LambdaQueryWrapper<ReviewPlan>()
                        .select(ReviewPlan::getWordId)
                        .eq(ReviewPlan::getUserId, userId)
                        .eq(ReviewPlan::getDeleted, 0))
                .stream()
                .map(ReviewPlan::getWordId)
                .toList();
        LambdaQueryWrapper<Word> queryWrapper = new LambdaQueryWrapper<Word>()
                .eq(Word::getBookId, bookId)
                .eq(Word::getStatus, 1)
                .eq(Word::getDeleted, 0)
                .orderByAsc(Word::getId)
                .last("LIMIT " + taskLimit);
        if (!learnedWordIds.isEmpty()) {
            queryWrapper.notIn(Word::getId, learnedWordIds);
        }
        List<Word> words = wordMapper.selectList(queryWrapper);
        LocalDateTime now = LocalDateTime.now();
        return words.stream().map(word -> {
            ReviewPlan plan = new ReviewPlan();
            plan.setUserId(userId);
            plan.setWordId(word.getId());
            plan.setStage(0);
            plan.setStatus(0);
            plan.setFamiliarity(1);
            plan.setNextReviewTime(null);
            plan.setLastReviewTime(null);
            plan.setCreateTime(now);
            plan.setUpdateTime(now);
            plan.setDeleted(0);
            reviewPlanMapper.insert(plan);
            return StudyWordVO.of(word, plan);
        }).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public StudySubmitVO submit(StudySubmitRequest request) {
        Long userId = currentUserId();
        Word word = wordMapper.selectById(request.getWordId());
        if (word == null || Integer.valueOf(1).equals(word.getDeleted()) || Integer.valueOf(0).equals(word.getStatus())) {
            throw new IllegalArgumentException("单词不存在");
        }
        ensureBookAccessible(word.getBookId(), userId);
        ReviewPlan plan = reviewPlanMapper.selectOne(new LambdaQueryWrapper<ReviewPlan>()
                .eq(ReviewPlan::getUserId, userId)
                .eq(ReviewPlan::getWordId, request.getWordId())
                .eq(ReviewPlan::getDeleted, 0)
                .last("LIMIT 1"));
        boolean newPlan = plan == null;
        boolean firstStudy = newPlan || plan.getFirstStudyTime() == null;
        if (newPlan) {
            plan = new ReviewPlan();
            plan.setUserId(userId);
            plan.setWordId(request.getWordId());
            plan.setStage(0);
            plan.setStatus(0);
            plan.setCreateTime(LocalDateTime.now());
            plan.setDeleted(0);
        }
        if (plan.getFirstStudyTime() == null) {
            plan.setFirstStudyTime(LocalDateTime.now());
        }

        boolean wasMastered = Integer.valueOf(1).equals(plan.getStatus());
        Integer nextStage = reviewScheduleService.calculateNextStage(plan.getStage(), request.getFamiliarity());
        boolean mastered = reviewScheduleService.isMastered(nextStage, request.getFamiliarity());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextReviewTime = mastered ? null : reviewScheduleService.calculateNextReviewTime(plan.getStage(), request.getFamiliarity());
        plan.setStage(nextStage);
        plan.setFamiliarity(request.getFamiliarity());
        plan.setStatus(mastered ? 1 : 0);
        plan.setLastReviewTime(now);
        plan.setNextReviewTime(nextReviewTime);
        plan.setUpdateTime(now);
        if (plan.getId() == null) {
            reviewPlanMapper.insert(plan);
        } else {
            reviewPlanMapper.updateById(plan);
        }

        if (request.getFamiliarity() <= 2) {
            collectWrongWord(userId, request.getWordId());
        }
        recordStudy(userId, request.getFamiliarity(), mastered, request.getDuration(), firstStudy);
        updateUserWordStats(userId, firstStudy, mastered && !wasMastered);

        return StudySubmitVO.builder()
                .wordId(request.getWordId())
                .stage(plan.getStage())
                .status(plan.getStatus())
                .mastered(mastered)
                .nextReviewTime(nextReviewTime)
                .build();
    }

    private void collectWrongWord(Long userId, Long wordId) {
        WrongWord wrongWord = wrongWordMapper.selectOne(new LambdaQueryWrapper<WrongWord>()
                .eq(WrongWord::getUserId, userId)
                .eq(WrongWord::getWordId, wordId)
                .eq(WrongWord::getDeleted, 0)
                .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();
        if (wrongWord == null) {
            wrongWord = new WrongWord();
            wrongWord.setUserId(userId);
            wrongWord.setWordId(wordId);
            wrongWord.setWrongCount(1);
            wrongWord.setStatus(1);
            wrongWord.setCreateTime(now);
            wrongWord.setUpdateTime(now);
            wrongWord.setDeleted(0);
            wrongWordMapper.insert(wrongWord);
            return;
        }
        wrongWord.setWrongCount(wrongWord.getWrongCount() + 1);
        wrongWord.setStatus(1);
        wrongWord.setUpdateTime(now);
        wrongWordMapper.updateById(wrongWord);
    }

    private void recordStudy(Long userId, Integer familiarity, boolean mastered, Integer duration, boolean newWord) {
        LocalDate today = LocalDate.now();
        StudyRecord record = studyRecordMapper.selectOne(new LambdaQueryWrapper<StudyRecord>()
                .eq(StudyRecord::getUserId, userId)
                .eq(StudyRecord::getStudyDate, today)
                .last("LIMIT 1"));
        boolean insert = record == null;
        if (insert) {
            record = new StudyRecord();
            record.setUserId(userId);
            record.setStudyDate(today);
            record.setNewWordCount(0);
            record.setReviewWordCount(0);
            record.setMasteredWordCount(0);
            record.setWrongWordCount(0);
            record.setStudyDuration(0);
            record.setCreateTime(LocalDateTime.now());
        }
        if (newWord) {
            record.setNewWordCount(record.getNewWordCount() + 1);
        } else {
            record.setReviewWordCount(record.getReviewWordCount() + 1);
        }
        if (mastered) {
            record.setMasteredWordCount(record.getMasteredWordCount() + 1);
        }
        if (familiarity <= 2) {
            record.setWrongWordCount(record.getWrongWordCount() + 1);
        }
        record.setStudyDuration(record.getStudyDuration() + (duration == null ? 0 : duration));
        record.setUpdateTime(LocalDateTime.now());
        if (insert) {
            studyRecordMapper.insert(record);
        } else {
            studyRecordMapper.updateById(record);
        }
    }

    private void updateUserWordStats(Long userId, boolean newLearned, boolean newMastered) {
        if (!newLearned && !newMastered) {
            return;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        if (newLearned) {
            user.setTotalWords((user.getTotalWords() == null ? 0 : user.getTotalWords()) + 1);
        }
        if (newMastered) {
            user.setMasteredWords((user.getMasteredWords() == null ? 0 : user.getMasteredWords()) + 1);
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    private List<Long> findBookWordIds(Long bookId) {
        if (bookId == null) {
            return null;
        }
        return wordMapper.selectList(new LambdaQueryWrapper<Word>()
                        .select(Word::getId)
                        .eq(Word::getBookId, bookId)
                        .eq(Word::getStatus, 1)
                        .eq(Word::getDeleted, 0))
                .stream()
                .map(Word::getId)
                .toList();
    }

    private Long resolveBookId(Long userId, Long requestedBookId) {
        if (requestedBookId != null) {
            return requestedBookId;
        }
        StudyPlan plan = findCurrentStudyPlan(userId);
        return plan == null ? null : plan.getBookId();
    }

    private int resolveTaskLimit(Long userId, Long bookId, Integer requestedLimit, boolean newWords) {
        if (requestedLimit != null && requestedLimit > 0) {
            return Math.min(requestedLimit, 500);
        }
        StudyPlan plan = findCurrentStudyPlan(userId);
        if (plan != null && plan.getBookId().equals(bookId)) {
            int planned = newWords ? plan.getPlanWordCount() : plan.getDailyReviewCount();
            return Math.max(1, Math.min(planned, 500));
        }
        return newWords ? 10 : 20;
    }

    private StudyPlan findCurrentStudyPlan(Long userId) {
        return studyPlanMapper.selectOne(new LambdaQueryWrapper<StudyPlan>()
                .eq(StudyPlan::getUserId, userId)
                .eq(StudyPlan::getDeleted, 0)
                .orderByDesc(StudyPlan::getUpdateTime)
                .last("LIMIT 1"));
    }

    private void ensureBookAccessible(Long bookId, Long userId) {
        WordBook book = wordBookMapper.selectById(bookId);
        if (book == null || Integer.valueOf(1).equals(book.getDeleted()) || Integer.valueOf(0).equals(book.getStatus())) {
            throw new IllegalArgumentException("词库不存在或已停用");
        }
        if (Integer.valueOf(2).equals(book.getType()) && !userId.equals(book.getUserId())) {
            throw new IllegalArgumentException("无权使用该私人词库");
        }
    }

    private Long currentUserId() {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("请先登录");
        }
        return userId;
    }
}
