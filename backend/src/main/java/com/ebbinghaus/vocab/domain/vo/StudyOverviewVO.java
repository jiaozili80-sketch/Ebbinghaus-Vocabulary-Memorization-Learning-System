package com.ebbinghaus.vocab.domain.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudyOverviewVO {

    private Long newWordCount;
    private Long reviewWordCount;
    private Long masteredWordCount;
    private Long wrongWordCount;
}
