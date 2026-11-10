package com.ebbinghaus.vocab.domain.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScheduleOverviewVO {

    private Boolean checkedInToday;
    private Integer continuousCheckinDays;
    private Integer todayWordCount;
    private Integer todayStudyDuration;
    private Integer targetWordCount;
    private Integer targetStudyMinutes;
    private Double wordProgress;
    private Double durationProgress;
}
