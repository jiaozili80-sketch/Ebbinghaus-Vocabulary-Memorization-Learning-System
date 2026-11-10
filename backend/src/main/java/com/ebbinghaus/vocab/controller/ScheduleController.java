package com.ebbinghaus.vocab.controller;

import com.ebbinghaus.vocab.common.ApiResult;
import com.ebbinghaus.vocab.domain.dto.CheckinRequest;
import com.ebbinghaus.vocab.domain.dto.DailyGoalRequest;
import com.ebbinghaus.vocab.domain.entity.CheckinRecord;
import com.ebbinghaus.vocab.domain.entity.DailyGoal;
import com.ebbinghaus.vocab.domain.vo.CalendarDayVO;
import com.ebbinghaus.vocab.domain.vo.ScheduleOverviewVO;
import com.ebbinghaus.vocab.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/overview")
    public ApiResult<ScheduleOverviewVO> overview() {
        return ApiResult.success(scheduleService.overview());
    }

    @GetMapping("/goal")
    public ApiResult<DailyGoal> goal() {
        return ApiResult.success(scheduleService.getGoal());
    }

    @PutMapping("/goal")
    public ApiResult<DailyGoal> updateGoal(@Valid @RequestBody DailyGoalRequest request) {
        return ApiResult.success(scheduleService.updateGoal(request));
    }

    @PostMapping("/checkin")
    public ApiResult<CheckinRecord> checkin(@Valid @RequestBody CheckinRequest request) {
        return ApiResult.success(scheduleService.checkin(request));
    }

    @GetMapping("/calendar")
    public ApiResult<List<CalendarDayVO>> calendar(@RequestParam(required = false) Integer year,
                                                   @RequestParam(required = false) Integer month) {
        return ApiResult.success(scheduleService.calendar(year, month));
    }
}
