package com.ebbinghaus.vocab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ebbinghaus.vocab.domain.dto.WordRequest;
import com.ebbinghaus.vocab.domain.entity.Word;
import com.ebbinghaus.vocab.domain.entity.WordBook;
import com.ebbinghaus.vocab.mapper.WordBookMapper;
import com.ebbinghaus.vocab.mapper.WordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WordService {

    private final WordMapper wordMapper;
    private final WordBookMapper wordBookMapper;

    public IPage<Word> page(long current, long size, Long bookId, String keyword, Integer status) {
        LambdaQueryWrapper<Word> queryWrapper = new LambdaQueryWrapper<Word>()
                .eq(Word::getDeleted, 0)
                .orderByDesc(Word::getCreateTime);
        if (bookId != null) {
            queryWrapper.eq(Word::getBookId, bookId);
        }
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like(Word::getSpelling, keyword)
                    .or()
                    .like(Word::getMeaning, keyword));
        }
        if (status != null) {
            queryWrapper.eq(Word::getStatus, status);
        }
        return wordMapper.selectPage(new Page<>(current, size), queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public Word create(WordRequest request) {
        ensureWordBookExists(request.getBookId());
        Word word = new Word();
        fillWord(word, request);
        word.setDifficulty(normalizeDifficulty(request.getDifficulty()));
        word.setStatus(normalizeStatus(request.getStatus()));
        word.setCreateTime(LocalDateTime.now());
        word.setUpdateTime(LocalDateTime.now());
        word.setDeleted(0);
        wordMapper.insert(word);
        refreshWordCount(request.getBookId());
        return word;
    }

    @Transactional(rollbackFor = Exception.class)
    public Word update(Long id, WordRequest request) {
        ensureWordBookExists(request.getBookId());
        Word word = wordMapper.selectById(id);
        if (word == null || Integer.valueOf(1).equals(word.getDeleted())) {
            throw new IllegalArgumentException("单词不存在");
        }
        Long oldBookId = word.getBookId();
        fillWord(word, request);
        word.setDifficulty(normalizeDifficulty(request.getDifficulty()));
        word.setStatus(normalizeStatus(request.getStatus()));
        word.setUpdateTime(LocalDateTime.now());
        wordMapper.updateById(word);
        refreshWordCount(oldBookId);
        refreshWordCount(request.getBookId());
        return word;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Word word = wordMapper.selectById(id);
        if (word == null) {
            throw new IllegalArgumentException("单词不存在");
        }
        Long bookId = word.getBookId();
        wordMapper.deleteById(id);
        refreshWordCount(bookId);
    }

    private void ensureWordBookExists(Long bookId) {
        WordBook wordBook = wordBookMapper.selectById(bookId);
        if (wordBook == null || Integer.valueOf(1).equals(wordBook.getDeleted())) {
            throw new IllegalArgumentException("所属词库不存在");
        }
    }

    private void fillWord(Word word, WordRequest request) {
        word.setBookId(request.getBookId());
        word.setSpelling(request.getSpelling());
        word.setPhonetic(request.getPhonetic());
        word.setPartOfSpeech(request.getPartOfSpeech());
        word.setMeaning(request.getMeaning());
        word.setExampleSentence(request.getExampleSentence());
        word.setExampleTranslation(request.getExampleTranslation());
        word.setAudioUrl(request.getAudioUrl());
    }

    private int normalizeDifficulty(Integer difficulty) {
        if (difficulty == null) {
            return 1;
        }
        if (difficulty < 1 || difficulty > 5) {
            throw new IllegalArgumentException("单词难度必须在 1 到 5 之间");
        }
        return difficulty;
    }

    private int normalizeStatus(Integer status) {
        if (status == null) {
            return 1;
        }
        if (!Integer.valueOf(0).equals(status) && !Integer.valueOf(1).equals(status)) {
            throw new IllegalArgumentException("单词状态不合法");
        }
        return status;
    }

    private void refreshWordCount(Long bookId) {
        Long count = wordMapper.selectCount(new LambdaQueryWrapper<Word>()
                .eq(Word::getBookId, bookId)
                .eq(Word::getDeleted, 0));
        WordBook update = new WordBook();
        update.setId(bookId);
        update.setWordCount(count.intValue());
        update.setUpdateTime(LocalDateTime.now());
        wordBookMapper.updateById(update);
    }
}
