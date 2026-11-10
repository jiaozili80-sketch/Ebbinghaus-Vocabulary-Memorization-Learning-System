package com.ebbinghaus.vocab.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("daily_goal")
public class DailyGoal {

    private Long id;
    private Long userId;
    private Integer targetWordCount;
    private Integer targetStudyMinutes;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
