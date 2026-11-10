package com.ebbinghaus.vocab.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ebbinghaus.vocab.common.ApiResult;
import com.ebbinghaus.vocab.domain.dto.WordBookRequest;
import com.ebbinghaus.vocab.domain.entity.WordBook;
import com.ebbinghaus.vocab.service.WordBookService;
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

import java.util.List;

@RestController
@RequestMapping("/word-books")
@RequiredArgsConstructor
public class WordBookController {

    private final WordBookService wordBookService;

    @GetMapping
    public ApiResult<List<WordBook>> list(@RequestParam(required = false) String category) {
        return ApiResult.success(wordBookService.listEnabled(category));
    }

    @GetMapping("/page")
    public ApiResult<IPage<WordBook>> page(@RequestParam(defaultValue = "1") long current,
                                           @RequestParam(defaultValue = "10") long size,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String category,
                                           @RequestParam(required = false) Integer status) {
        return ApiResult.success(wordBookService.page(current, size, keyword, category, status));
    }

    @PostMapping
    public ApiResult<WordBook> create(@Valid @RequestBody WordBookRequest request) {
        return ApiResult.success(wordBookService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResult<WordBook> update(@PathVariable Long id, @Valid @RequestBody WordBookRequest request) {
        return ApiResult.success(wordBookService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        wordBookService.delete(id);
        return ApiResult.success();
    }
}
