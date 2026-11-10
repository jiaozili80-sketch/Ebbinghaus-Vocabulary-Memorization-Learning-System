package com.ebbinghaus.vocab.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ebbinghaus.vocab.common.ApiResult;
import com.ebbinghaus.vocab.domain.dto.WordRequest;
import com.ebbinghaus.vocab.domain.entity.Word;
import com.ebbinghaus.vocab.service.WordService;
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

@RestController
@RequestMapping("/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    @GetMapping("/page")
    public ApiResult<IPage<Word>> page(@RequestParam(defaultValue = "1") long current,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) Long bookId,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) Integer status) {
        return ApiResult.success(wordService.page(current, size, bookId, keyword, status));
    }

    @PostMapping
    public ApiResult<Word> create(@Valid @RequestBody WordRequest request) {
        return ApiResult.success(wordService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResult<Word> update(@PathVariable Long id, @Valid @RequestBody WordRequest request) {
        return ApiResult.success(wordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        wordService.delete(id);
        return ApiResult.success();
    }
}
