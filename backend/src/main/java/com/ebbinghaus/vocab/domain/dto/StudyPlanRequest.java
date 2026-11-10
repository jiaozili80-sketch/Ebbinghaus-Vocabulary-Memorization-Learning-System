package com.ebbinghaus.vocab.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudyPlanRequest {

    @NotNull(message = "请选择词库")
    private Long bookId;

    @NotNull(message = "每日新词不能为空")
    @Min(value = 1, message = "每日新词至少为1")
    @Max(value = 500, message = "每日新词不能超过500")
    private Integer planWordCount;

    @NotNull(message = "复习倍数不能为空")
    @Min(value = 1, message = "复习倍数至少为1")
    @Max(value = 10, message = "复习倍数不能超过10")
    private Integer reviewMultiplier;
}
