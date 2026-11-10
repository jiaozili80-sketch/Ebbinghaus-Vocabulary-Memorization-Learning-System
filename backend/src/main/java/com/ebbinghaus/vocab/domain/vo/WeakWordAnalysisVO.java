package com.ebbinghaus.vocab.domain.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeakWordAnalysisVO {

    private String category;
    private Long count;
}
