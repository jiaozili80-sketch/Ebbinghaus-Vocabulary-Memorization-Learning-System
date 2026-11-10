package com.ebbinghaus.vocab.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DailyGoalRequest {

    @NotNull(message = "每日目标词数不能为空")
    @Min(value = 1, message = "每日目标词数至少为1")
    @Max(value = 500, message = "每日目标词数不能超过500")
    private Integer targetWordCount;

    @NotNull(message = "每日目标学习时长不能为空")
    @Min(value = 1, message = "每日目标学习时长至少为1分钟")
    @Max(value = 600, message = "每日目标学习时长不能超过600分钟")
    private Integer targetStudyMinutes;
}
