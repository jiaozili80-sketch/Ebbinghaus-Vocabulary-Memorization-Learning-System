package com.ebbinghaus.vocab.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("word")
public class Word {

    private Long id;
    private Long bookId;
    private String spelling;
    private String phonetic;
    private String partOfSpeech;
    private String meaning;
    private String exampleSentence;
    private String exampleTranslation;
    private String audioUrl;
    private Integer difficulty;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
