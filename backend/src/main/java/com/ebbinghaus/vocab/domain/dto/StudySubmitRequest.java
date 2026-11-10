package com.ebbinghaus.vocab.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class StudySubmitRequest {

    @NotNull(message = "单词ID不能为空")
    private Long wordId;

    @NotNull(message = "熟悉度不能为空")
    @Min(value = 1, message = "熟悉度参数错误")
    @Max(value = 3, message = "熟悉度参数错误")
    private Integer familiarity;

    @PositiveOrZero(message = "学习时长不能为负数")
    private Integer duration;
}
