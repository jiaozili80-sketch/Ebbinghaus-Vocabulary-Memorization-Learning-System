package com.ebbinghaus.vocab.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CheckinRequest {

    @Min(value = 0, message = "学习时长不能小于0")
    @Max(value = 86400, message = "学习时长不能超过一天")
    private Integer studyDuration;

    private String remark;
}
