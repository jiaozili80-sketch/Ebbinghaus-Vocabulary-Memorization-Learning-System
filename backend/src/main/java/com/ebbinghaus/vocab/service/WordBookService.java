package com.ebbinghaus.vocab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ebbinghaus.vocab.context.LoginUserContext;
import com.ebbinghaus.vocab.domain.dto.WordBookRequest;
import com.ebbinghaus.vocab.domain.entity.WordBook;
import com.ebbinghaus.vocab.mapper.WordBookMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WordBookService {

    private final WordBookMapper wordBookMapper;

    public List<WordBook> listEnabled(String category) {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("请先登录");
        }
        LambdaQueryWrapper<WordBook> queryWrapper = new LambdaQueryWrapper<WordBook>()
                .eq(WordBook::getStatus, 1)
                .eq(WordBook::getDeleted, 0)
                .and(wrapper -> wrapper.eq(WordBook::getType, 1)
                        .or(privateBook -> privateBook.eq(WordBook::getType, 2)
                                .eq(WordBook::getUserId, userId)))
                .orderByDesc(WordBook::getCreateTime);
        if (StringUtils.hasText(category)) {
            queryWrapper.eq(WordBook::getCategory, category);
        }
        return wordBookMapper.selectList(queryWrapper);
    }

    public IPage<WordBook> page(long current, long size, String keyword, String category, Integer status) {
        LambdaQueryWrapper<WordBook> queryWrapper = new LambdaQueryWrapper<WordBook>()
                .eq(WordBook::getDeleted, 0)
                .orderByDesc(WordBook::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like(WordBook::getName, keyword)
                    .or()
                    .like(WordBook::getDescription, keyword));
        }
        if (StringUtils.hasText(category)) {
            queryWrapper.eq(WordBook::getCategory, category);
        }
        if (status != null) {
            queryWrapper.eq(WordBook::getStatus, status);
        }
        return wordBookMapper.selectPage(new Page<>(current, size), queryWrapper);
    }

    public WordBook create(WordBookRequest request) {
        WordBook wordBook = new WordBook();
        fillWordBook(wordBook, request);
        wordBook.setWordCount(0);
        if (Integer.valueOf(2).equals(request.getType())) {
            wordBook.setUserId(LoginUserContext.getUserId());
        }
        wordBook.setCreateTime(LocalDateTime.now());
        wordBook.setUpdateTime(LocalDateTime.now());
        wordBook.setDeleted(0);
        wordBookMapper.insert(wordBook);
        return wordBook;
    }

    public WordBook update(Long id, WordBookRequest request) {
        WordBook wordBook = wordBookMapper.selectById(id);
        if (wordBook == null || Integer.valueOf(1).equals(wordBook.getDeleted())) {
            throw new IllegalArgumentException("词库不存在");
        }
        fillWordBook(wordBook, request);
        if (Integer.valueOf(2).equals(request.getType()) && wordBook.getUserId() == null) {
            wordBook.setUserId(LoginUserContext.getUserId());
        }
        if (Integer.valueOf(1).equals(request.getType())) {
            wordBook.setUserId(null);
        }
        wordBook.setUpdateTime(LocalDateTime.now());
        wordBookMapper.updateById(wordBook);
        return wordBook;
    }

    public void delete(Long id) {
        WordBook wordBook = wordBookMapper.selectById(id);
        if (wordBook == null) {
            throw new IllegalArgumentException("词库不存在");
        }
        wordBookMapper.deleteById(id);
    }

    private void fillWordBook(WordBook wordBook, WordBookRequest request) {
        if (!Integer.valueOf(1).equals(request.getType()) && !Integer.valueOf(2).equals(request.getType())) {
            throw new IllegalArgumentException("词库类型不合法");
        }
        if (!Integer.valueOf(0).equals(request.getStatus()) && !Integer.valueOf(1).equals(request.getStatus())) {
            throw new IllegalArgumentException("词库状态不合法");
        }
        wordBook.setName(request.getName());
        wordBook.setCategory(request.getCategory());
        wordBook.setDescription(request.getDescription());
        wordBook.setType(request.getType());
        wordBook.setStatus(request.getStatus());
    }
}
