package com.ebbinghaus.vocab.domain.vo;

import com.ebbinghaus.vocab.domain.entity.StudyPlan;
import com.ebbinghaus.vocab.domain.entity.WordBook;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StudyPlanVO {

    private Long id;
    private Long bookId;
    private String bookName;
    private String category;
    private Integer wordCount;
    private Integer planWordCount;
    private Integer reviewMultiplier;
    private Integer dailyReviewCount;
    private Integer dailyTotalCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static StudyPlanVO of(StudyPlan plan, WordBook book) {
        return StudyPlanVO.builder()
                .id(plan.getId())
                .bookId(plan.getBookId())
                .bookName(book == null ? "未知词库" : book.getName())
                .category(book == null ? "" : book.getCategory())
                .wordCount(book == null ? 0 : book.getWordCount())
                .planWordCount(plan.getPlanWordCount())
                .reviewMultiplier(plan.getReviewMultiplier())
                .dailyReviewCount(plan.getDailyReviewCount())
                .dailyTotalCount(plan.getDailyTotalCount())
                .createTime(plan.getCreateTime())
                .updateTime(plan.getUpdateTime())
                .build();
    }
}
