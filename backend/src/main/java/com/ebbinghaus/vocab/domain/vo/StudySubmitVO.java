package com.ebbinghaus.vocab.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StudySubmitVO {

    private Long wordId;
    private Integer stage;
    private Integer status;
    private Boolean mastered;
    private LocalDateTime nextReviewTime;
}
