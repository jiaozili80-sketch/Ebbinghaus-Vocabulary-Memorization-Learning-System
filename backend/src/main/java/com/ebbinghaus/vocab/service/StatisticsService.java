package com.ebbinghaus.vocab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ebbinghaus.vocab.context.LoginUserContext;
import com.ebbinghaus.vocab.domain.entity.ReviewPlan;
import com.ebbinghaus.vocab.domain.entity.StudyRecord;
import com.ebbinghaus.vocab.domain.entity.Word;
import com.ebbinghaus.vocab.domain.entity.WordBook;
import com.ebbinghaus.vocab.domain.entity.WrongWord;
import com.ebbinghaus.vocab.domain.vo.StatisticsOverviewVO;
import com.ebbinghaus.vocab.domain.vo.TrendPointVO;
import com.ebbinghaus.vocab.domain.vo.WeakWordAnalysisVO;
import com.ebbinghaus.vocab.mapper.ReviewPlanMapper;
import com.ebbinghaus.vocab.mapper.StudyRecordMapper;
import com.ebbinghaus.vocab.mapper.WordBookMapper;
import com.ebbinghaus.vocab.mapper.WordMapper;
import com.ebbinghaus.vocab.mapper.WrongWordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StudyRecordMapper studyRecordMapper;
    private final ReviewPlanMapper reviewPlanMapper;
    private final WrongWordMapper wrongWordMapper;
    private final WordMapper wordMapper;
    private final WordBookMapper wordBookMapper;

    public StatisticsOverviewVO overview() {
        Long userId = currentUserId();
        Long learnedWordCount = reviewPlanMapper.selectCount(new LambdaQueryWrapper<ReviewPlan>()
                .eq(ReviewPlan::getUserId, userId)
                .isNotNull(ReviewPlan::getFirstStudyTime)
                .eq(ReviewPlan::getDeleted, 0));
        Long masteredWordCount = reviewPlanMapper.selectCount(new LambdaQueryWrapper<ReviewPlan>()
                .eq(ReviewPlan::getUserId, userId)
                .eq(ReviewPlan::getStatus, 1)
                .eq(ReviewPlan::getDeleted, 0));
        Long activePlanCount = reviewPlanMapper.selectCount(new LambdaQueryWrapper<ReviewPlan>()
                .eq(ReviewPlan::getUserId, userId)
                .eq(ReviewPlan::getStatus, 0)
                .eq(ReviewPlan::getDeleted, 0));
        Long dueReviewCount = reviewPlanMapper.selectCount(new LambdaQueryWrapper<ReviewPlan>()
                .eq(ReviewPlan::getUserId, userId)
                .eq(ReviewPlan::getStatus, 0)
                .le(ReviewPlan::getNextReviewTime, LocalDateTime.now())
                .eq(ReviewPlan::getDeleted, 0));
        Long wrongWordCount = wrongWordMapper.selectCount(new LambdaQueryWrapper<WrongWord>()
                .eq(WrongWord::getUserId, userId)
                .eq(WrongWord::getStatus, 1)
                .eq(WrongWord::getDeleted, 0));

        return StatisticsOverviewVO.builder()
                .learnedWordCount(learnedWordCount)
                .masteredWordCount(masteredWordCount)
                .reviewPlanCount(activePlanCount)
                .wrongWordCount(wrongWordCount)
                .dueReviewCount(dueReviewCount)
                .masteryRate(rate(masteredWordCount, learnedWordCount))
                .forgettingRate(rate(wrongWordCount, learnedWordCount))
                .reviewCompletionRate(rate(Math.max(activePlanCount - dueReviewCount, 0), activePlanCount))
                .build();
    }

    public List<TrendPointVO> trend(Integer days) {
        Long userId = currentUserId();
        int range = days == null || days <= 0 ? 7 : Math.min(days, 90);
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(range - 1L);
        List<StudyRecord> records = studyRecordMapper.selectList(new LambdaQueryWrapper<StudyRecord>()
                .eq(StudyRecord::getUserId, userId)
                .between(StudyRecord::getStudyDate, start, end)
                .orderByAsc(StudyRecord::getStudyDate));
        Map<LocalDate, StudyRecord> recordMap = records.stream()
                .collect(Collectors.toMap(StudyRecord::getStudyDate, Function.identity()));
        List<TrendPointVO> result = new ArrayList<>();
        for (int i = 0; i < range; i++) {
            LocalDate date = start.plusDays(i);
            StudyRecord record = recordMap.get(date);
            result.add(TrendPointVO.builder()
                    .date(date.toString())
                    .newWordCount(record == null ? 0 : record.getNewWordCount())
                    .reviewWordCount(record == null ? 0 : record.getReviewWordCount())
                    .masteredWordCount(record == null ? 0 : record.getMasteredWordCount())
                    .wrongWordCount(record == null ? 0 : record.getWrongWordCount())
                    .studyDuration(record == null ? 0 : record.getStudyDuration())
                    .build());
        }
        return result;
    }

    public List<WeakWordAnalysisVO> weakAnalysis() {
        Long userId = currentUserId();
        List<WrongWord> wrongWords = wrongWordMapper.selectList(new LambdaQueryWrapper<WrongWord>()
                .eq(WrongWord::getUserId, userId)
                .eq(WrongWord::getStatus, 1)
                .eq(WrongWord::getDeleted, 0));
        if (wrongWords.isEmpty()) {
            return List.of();
        }
        List<Long> wordIds = wrongWords.stream().map(WrongWord::getWordId).distinct().toList();
        List<Word> words = wordMapper.selectBatchIds(wordIds).stream()
                .filter(word -> Integer.valueOf(0).equals(word.getDeleted()))
                .filter(word -> Integer.valueOf(1).equals(word.getStatus()))
                .toList();
        List<Long> bookIds = words.stream().map(Word::getBookId).distinct().toList();
        if (bookIds.isEmpty()) {
            return List.of();
        }
        Map<Long, WordBook> bookMap = wordBookMapper.selectBatchIds(bookIds).stream()
                .filter(book -> Integer.valueOf(0).equals(book.getDeleted()))
                .filter(book -> Integer.valueOf(1).equals(book.getStatus()))
                .collect(Collectors.toMap(WordBook::getId, Function.identity()));
        return words.stream()
                .map(word -> bookMap.get(word.getBookId()))
                .filter(book -> book != null && book.getCategory() != null)
                .collect(Collectors.groupingBy(WordBook::getCategory, Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> WeakWordAnalysisVO.builder()
                        .category(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .toList();
    }

    private Double rate(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(numerator * 100.0 / denominator)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private Long currentUserId() {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("请先登录");
        }
        return userId;
    }
}
