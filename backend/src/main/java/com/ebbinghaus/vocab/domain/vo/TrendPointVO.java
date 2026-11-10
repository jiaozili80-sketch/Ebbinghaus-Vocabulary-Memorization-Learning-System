package com.ebbinghaus.vocab.domain.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrendPointVO {

    private String date;
    private Integer newWordCount;
    private Integer reviewWordCount;
    private Integer masteredWordCount;
    private Integer wrongWordCount;
    private Integer studyDuration;
}
