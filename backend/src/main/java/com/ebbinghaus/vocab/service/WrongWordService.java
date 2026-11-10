package com.ebbinghaus.vocab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ebbinghaus.vocab.context.LoginUserContext;
import com.ebbinghaus.vocab.domain.entity.ReviewPlan;
import com.ebbinghaus.vocab.domain.entity.Word;
import com.ebbinghaus.vocab.domain.entity.WrongWord;
import com.ebbinghaus.vocab.domain.vo.StudyWordVO;
import com.ebbinghaus.vocab.domain.vo.WrongWordVO;
import com.ebbinghaus.vocab.mapper.ReviewPlanMapper;
import com.ebbinghaus.vocab.mapper.WordMapper;
import com.ebbinghaus.vocab.mapper.WrongWordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WrongWordService {

    private final WrongWordMapper wrongWordMapper;
    private final WordMapper wordMapper;
    private final ReviewPlanMapper reviewPlanMapper;

    public IPage<WrongWordVO> page(long current, long size, Long bookId, String keyword) {
        Long userId = currentUserId();
        List<Long> matchedWordIds = findMatchedWordIds(bookId, keyword);
        if (matchedWordIds != null && matchedWordIds.isEmpty()) {
            Page<WrongWordVO> emptyPage = new Page<>(current, size, 0);
            emptyPage.setRecords(List.of());
            return emptyPage;
        }
        LambdaQueryWrapper<WrongWord> queryWrapper = new LambdaQueryWrapper<WrongWord>()
                .eq(WrongWord::getUserId, userId)
                .eq(WrongWord::getStatus, 1)
                .eq(WrongWord::getDeleted, 0)
                .in(matchedWordIds != null, WrongWord::getWordId, matchedWordIds)
                .orderByDesc(WrongWord::getWrongCount)
                .orderByDesc(WrongWord::getUpdateTime);
        IPage<WrongWord> wrongPage = wrongWordMapper.selectPage(new Page<>(current, size), queryWrapper);
        List<WrongWordVO> records = buildVOList(wrongPage.getRecords());
        Page<WrongWordVO> result = new Page<>(wrongPage.getCurrent(), wrongPage.getSize(), wrongPage.getTotal());
        result.setRecords(records);
        return result;
    }

    public List<WrongWordVO> exportAll(Long bookId, String keyword) {
        Long userId = currentUserId();
        List<Long> matchedWordIds = findMatchedWordIds(bookId, keyword);
        if (matchedWordIds != null && matchedWordIds.isEmpty()) {
            return List.of();
        }
        List<WrongWord> wrongWords = wrongWordMapper.selectList(new LambdaQueryWrapper<WrongWord>()
                .eq(WrongWord::getUserId, userId)
                .eq(WrongWord::getStatus, 1)
                .eq(WrongWord::getDeleted, 0)
                .in(matchedWordIds != null, WrongWord::getWordId, matchedWordIds)
                .orderByDesc(WrongWord::getWrongCount)
                .orderByDesc(WrongWord::getUpdateTime));
        return buildVOList(wrongWords);
    }

    public List<StudyWordVO> reinforce(Long bookId, Integer limit) {
        Long userId = currentUserId();
        int taskLimit = limit == null || limit <= 0 ? 20 : Math.min(limit, 100);
        List<Long> matchedWordIds = findMatchedWordIds(bookId, null);
        if (matchedWordIds != null && matchedWordIds.isEmpty()) {
            return List.of();
        }
        List<WrongWord> wrongWords = wrongWordMapper.selectList(new LambdaQueryWrapper<WrongWord>()
                .eq(WrongWord::getUserId, userId)
                .eq(WrongWord::getStatus, 1)
                .eq(WrongWord::getDeleted, 0)
                .in(matchedWordIds != null, WrongWord::getWordId, matchedWordIds)
                .orderByDesc(WrongWord::getWrongCount)
                .orderByDesc(WrongWord::getUpdateTime)
                .last("LIMIT " + taskLimit));
        if (wrongWords.isEmpty()) {
            return List.of();
        }
        List<Long> wordIds = wrongWords.stream().map(WrongWord::getWordId).toList();
        Map<Long, Word> wordMap = wordMapper.selectBatchIds(wordIds).stream()
                .collect(Collectors.toMap(Word::getId, Function.identity()));
        Map<Long, ReviewPlan> planMap = reviewPlanMapper.selectList(new LambdaQueryWrapper<ReviewPlan>()
                        .eq(ReviewPlan::getUserId, userId)
                        .in(ReviewPlan::getWordId, wordIds)
                        .eq(ReviewPlan::getDeleted, 0))
                .stream()
                .collect(Collectors.toMap(ReviewPlan::getWordId, Function.identity(), (a, b) -> a));
        return wrongWords.stream()
                .map(WrongWord::getWordId)
                .filter(wordMap::containsKey)
                .map(wordId -> StudyWordVO.of(wordMap.get(wordId), planMap.get(wordId)))
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        WrongWord wrongWord = getOwnWrongWord(id);
        wrongWord.setStatus(0);
        wrongWord.setUpdateTime(LocalDateTime.now());
        wrongWordMapper.updateById(wrongWord);
    }

    @Transactional(rollbackFor = Exception.class)
    public void markMastered(Long id) {
        WrongWord wrongWord = getOwnWrongWord(id);
        wrongWord.setStatus(0);
        wrongWord.setUpdateTime(LocalDateTime.now());
        wrongWordMapper.updateById(wrongWord);

        ReviewPlan plan = reviewPlanMapper.selectOne(new LambdaQueryWrapper<ReviewPlan>()
                .eq(ReviewPlan::getUserId, wrongWord.getUserId())
                .eq(ReviewPlan::getWordId, wrongWord.getWordId())
                .eq(ReviewPlan::getDeleted, 0)
                .last("LIMIT 1"));
        if (plan != null) {
            plan.setStatus(1);
            plan.setFamiliarity(3);
            plan.setNextReviewTime(null);
            plan.setUpdateTime(LocalDateTime.now());
            reviewPlanMapper.updateById(plan);
        }
    }

    private WrongWord getOwnWrongWord(Long id) {
        WrongWord wrongWord = wrongWordMapper.selectById(id);
        if (wrongWord == null || Integer.valueOf(1).equals(wrongWord.getDeleted())) {
            throw new IllegalArgumentException("生词记录不存在");
        }
        if (!wrongWord.getUserId().equals(currentUserId())) {
            throw new IllegalArgumentException("无权操作该生词记录");
        }
        return wrongWord;
    }

    private List<WrongWordVO> buildVOList(List<WrongWord> wrongWords) {
        if (wrongWords.isEmpty()) {
            return List.of();
        }
        List<Long> wordIds = wrongWords.stream().map(WrongWord::getWordId).toList();
        Map<Long, Word> wordMap = wordMapper.selectBatchIds(wordIds).stream()
                .collect(Collectors.toMap(Word::getId, Function.identity()));
        return wrongWords.stream()
                .filter(wrongWord -> wordMap.containsKey(wrongWord.getWordId()))
                .map(wrongWord -> WrongWordVO.of(wrongWord, wordMap.get(wrongWord.getWordId())))
                .toList();
    }

    private List<Long> findMatchedWordIds(Long bookId, String keyword) {
        if (bookId == null && !StringUtils.hasText(keyword)) {
            return null;
        }
        return wordMapper.selectList(new LambdaQueryWrapper<Word>()
                        .select(Word::getId)
                        .eq(bookId != null, Word::getBookId, bookId)
                        .and(StringUtils.hasText(keyword), wrapper -> wrapper.like(Word::getSpelling, keyword)
                                .or()
                                .like(Word::getMeaning, keyword))
                        .eq(Word::getStatus, 1)
                        .eq(Word::getDeleted, 0))
                .stream()
                .map(Word::getId)
                .toList();
    }

    private Long currentUserId() {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("请先登录");
        }
        return userId;
    }
}
