package com.ebbinghaus.vocab.domain.vo;

import com.ebbinghaus.vocab.domain.entity.Word;
import com.ebbinghaus.vocab.domain.entity.WrongWord;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WrongWordVO {

    private Long id;
    private Long wordId;
    private Long bookId;
    private String spelling;
    private String phonetic;
    private String partOfSpeech;
    private String meaning;
    private String exampleSentence;
    private String exampleTranslation;
    private String audioUrl;
    private Integer difficulty;
    private Integer wrongCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static WrongWordVO of(WrongWord wrongWord, Word word) {
        return WrongWordVO.builder()
                .id(wrongWord.getId())
                .wordId(wrongWord.getWordId())
                .bookId(word.getBookId())
                .spelling(word.getSpelling())
                .phonetic(word.getPhonetic())
                .partOfSpeech(word.getPartOfSpeech())
                .meaning(word.getMeaning())
                .exampleSentence(word.getExampleSentence())
                .exampleTranslation(word.getExampleTranslation())
                .audioUrl(word.getAudioUrl())
                .difficulty(word.getDifficulty())
                .wrongCount(wrongWord.getWrongCount())
                .status(wrongWord.getStatus())
                .createTime(wrongWord.getCreateTime())
                .updateTime(wrongWord.getUpdateTime())
                .build();
    }
}
