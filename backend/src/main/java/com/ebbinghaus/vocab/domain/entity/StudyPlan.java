package com.ebbinghaus.vocab.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("study_plan")
public class StudyPlan {

    private Long id;
    private Long userId;
    private Long bookId;
    private Integer planWordCount;
    private Integer reviewMultiplier;
    private Integer dailyReviewCount;
    private Integer dailyTotalCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
