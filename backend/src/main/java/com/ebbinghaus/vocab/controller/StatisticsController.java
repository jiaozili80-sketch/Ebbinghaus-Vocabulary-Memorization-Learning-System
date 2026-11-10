package com.ebbinghaus.vocab.controller;

import com.ebbinghaus.vocab.common.ApiResult;
import com.ebbinghaus.vocab.domain.vo.StatisticsOverviewVO;
import com.ebbinghaus.vocab.domain.vo.TrendPointVO;
import com.ebbinghaus.vocab.domain.vo.WeakWordAnalysisVO;
import com.ebbinghaus.vocab.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/overview")
    public ApiResult<StatisticsOverviewVO> overview() {
        return ApiResult.success(statisticsService.overview());
    }

    @GetMapping("/trend")
    public ApiResult<List<TrendPointVO>> trend(@RequestParam(required = false) Integer days) {
        return ApiResult.success(statisticsService.trend(days));
    }

    @GetMapping("/weak-analysis")
    public ApiResult<List<WeakWordAnalysisVO>> weakAnalysis() {
        return ApiResult.success(statisticsService.weakAnalysis());
    }
}
