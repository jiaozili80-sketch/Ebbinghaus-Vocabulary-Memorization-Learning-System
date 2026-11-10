package com.ebbinghaus.vocab.domain.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardVO {

    private Long userCount;
    private Long activeUserCount;
    private Long wordBookCount;
    private Long wordCount;
    private Long todayStudyCount;
    private Long todayCheckinCount;
    private Long wrongWordCount;
    private Long reviewPlanCount;
}
