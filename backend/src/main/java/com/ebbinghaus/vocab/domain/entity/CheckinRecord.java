package com.ebbinghaus.vocab.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("checkin_record")
public class CheckinRecord {

    private Long id;
    private Long userId;
    private LocalDate checkinDate;
    private Integer studyDuration;
    private Integer completedTarget;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
