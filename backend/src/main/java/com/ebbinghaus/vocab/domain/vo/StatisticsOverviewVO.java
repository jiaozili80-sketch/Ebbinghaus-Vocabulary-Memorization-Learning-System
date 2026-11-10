package com.ebbinghaus.vocab.domain.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatisticsOverviewVO {

    private Long learnedWordCount;
    private Long masteredWordCount;
    private Long reviewPlanCount;
    private Long wrongWordCount;
    private Long dueReviewCount;
    private Double masteryRate;
    private Double forgettingRate;
    private Double reviewCompletionRate;
}
