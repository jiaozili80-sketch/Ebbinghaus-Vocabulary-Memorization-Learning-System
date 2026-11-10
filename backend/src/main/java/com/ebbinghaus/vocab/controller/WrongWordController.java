package com.ebbinghaus.vocab.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ebbinghaus.vocab.common.ApiResult;
import com.ebbinghaus.vocab.domain.vo.StudyWordVO;
import com.ebbinghaus.vocab.domain.vo.WrongWordVO;
import com.ebbinghaus.vocab.service.WrongWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/wrong-words")
@RequiredArgsConstructor
public class WrongWordController {

    private final WrongWordService wrongWordService;

    @GetMapping("/page")
    public ApiResult<IPage<WrongWordVO>> page(@RequestParam(defaultValue = "1") long current,
                                              @RequestParam(defaultValue = "10") long size,
                                              @RequestParam(required = false) Long bookId,
                                              @RequestParam(required = false) String keyword) {
        return ApiResult.success(wrongWordService.page(current, size, bookId, keyword));
    }

    @GetMapping("/export")
    public ApiResult<List<WrongWordVO>> export(@RequestParam(required = false) Long bookId,
                                               @RequestParam(required = false) String keyword) {
        return ApiResult.success(wrongWordService.exportAll(bookId, keyword));
    }

    @GetMapping("/reinforce")
    public ApiResult<List<StudyWordVO>> reinforce(@RequestParam(required = false) Long bookId,
                                                  @RequestParam(required = false) Integer limit) {
        return ApiResult.success(wrongWordService.reinforce(bookId, limit));
    }

    @PostMapping("/{id}/mastered")
    public ApiResult<Void> markMastered(@PathVariable Long id) {
        wrongWordService.markMastered(id);
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> remove(@PathVariable Long id) {
        wrongWordService.remove(id);
        return ApiResult.success();
    }
}
