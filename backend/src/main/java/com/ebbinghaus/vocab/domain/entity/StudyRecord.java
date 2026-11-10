package com.ebbinghaus.vocab.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("study_record")
public class StudyRecord {

    private Long id;
    private Long userId;
    private LocalDate studyDate;
    private Integer newWordCount;
    private Integer reviewWordCount;
    private Integer masteredWordCount;
    private Integer wrongWordCount;
    private Integer studyDuration;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
