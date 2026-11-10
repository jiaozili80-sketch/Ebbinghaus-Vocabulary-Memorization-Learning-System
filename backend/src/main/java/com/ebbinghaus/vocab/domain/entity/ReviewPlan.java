package com.ebbinghaus.vocab.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review_plan")
public class ReviewPlan {

    private Long id;
    private Long userId;
    private Long wordId;
    private Integer stage;
    private Integer status;
    private Integer familiarity;
    private LocalDateTime nextReviewTime;
    private LocalDateTime lastReviewTime;
    private LocalDateTime firstStudyTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
