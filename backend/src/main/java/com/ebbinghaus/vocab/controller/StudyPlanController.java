package com.ebbinghaus.vocab.controller;

import com.ebbinghaus.vocab.common.ApiResult;
import com.ebbinghaus.vocab.domain.dto.StudyPlanRequest;
import com.ebbinghaus.vocab.domain.vo.StudyPlanVO;
import com.ebbinghaus.vocab.service.StudyPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/study-plan")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @GetMapping
    public ApiResult<StudyPlanVO> current() {
        return ApiResult.success(studyPlanService.current());
    }

    @PostMapping
    public ApiResult<StudyPlanVO> save(@Valid @RequestBody StudyPlanRequest request) {
        return ApiResult.success(studyPlanService.save(request));
    }

    @DeleteMapping
    public ApiResult<Void> delete() {
        studyPlanService.delete();
        return ApiResult.success();
    }
}
