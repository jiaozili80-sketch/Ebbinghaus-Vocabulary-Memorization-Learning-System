package com.ebbinghaus.vocab.domain.vo;

import com.ebbinghaus.vocab.domain.entity.ReviewPlan;
import com.ebbinghaus.vocab.domain.entity.Word;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StudyWordVO {

    private Long wordId;
    private Long planId;
    private Long bookId;
    private String spelling;
    private String phonetic;
    private String partOfSpeech;
    private String meaning;
    private String exampleSentence;
    private String exampleTranslation;
    private String audioUrl;
    private Integer difficulty;
    private Integer stage;
    private Integer status;
    private LocalDateTime nextReviewTime;

    public static StudyWordVO of(Word word, ReviewPlan plan) {
        return StudyWordVO.builder()
                .wordId(word.getId())
                .planId(plan == null ? null : plan.getId())
                .bookId(word.getBookId())
                .spelling(word.getSpelling())
                .phonetic(word.getPhonetic())
                .partOfSpeech(word.getPartOfSpeech())
                .meaning(word.getMeaning())
                .exampleSentence(word.getExampleSentence())
                .exampleTranslation(word.getExampleTranslation())
                .audioUrl(word.getAudioUrl())
                .difficulty(word.getDifficulty())
                .stage(plan == null ? 0 : plan.getStage())
                .status(plan == null ? 0 : plan.getStatus())
                .nextReviewTime(plan == null ? null : plan.getNextReviewTime())
                .build();
    }
}
