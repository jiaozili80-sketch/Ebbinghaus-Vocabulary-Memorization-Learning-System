package com.ebbinghaus.vocab.controller;

import com.ebbinghaus.vocab.common.ApiResult;
import com.ebbinghaus.vocab.domain.dto.StudySubmitRequest;
import com.ebbinghaus.vocab.domain.vo.StudyOverviewVO;
import com.ebbinghaus.vocab.domain.vo.StudySubmitVO;
import com.ebbinghaus.vocab.domain.vo.StudyWordVO;
import com.ebbinghaus.vocab.service.StudyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    @GetMapping("/overview")
    public ApiResult<StudyOverviewVO> overview(@RequestParam(required = false) Long bookId) {
        return ApiResult.success(studyService.overview(bookId));
    }

    @GetMapping("/today")
    public ApiResult<List<StudyWordVO>> todayTasks(@RequestParam(required = false) Long bookId,
                                                   @RequestParam(required = false) Integer limit) {
        return ApiResult.success(studyService.todayTasks(bookId, limit));
    }

    @PostMapping("/new")
    public ApiResult<List<StudyWordVO>> startNew(@RequestParam Long bookId,
                                                 @RequestParam(required = false) Integer limit) {
        return ApiResult.success(studyService.startNew(bookId, limit));
    }

    @PostMapping("/submit")
    public ApiResult<StudySubmitVO> submit(@Valid @RequestBody StudySubmitRequest request) {
        return ApiResult.success(studyService.submit(request));
    }
}
