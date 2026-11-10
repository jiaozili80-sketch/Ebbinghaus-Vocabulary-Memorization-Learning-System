package com.ebbinghaus.vocab.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CalendarDayVO {

    private LocalDate date;
    private Boolean checkedIn;
    private Integer newWordCount;
    private Integer reviewWordCount;
    private Integer masteredWordCount;
    private Integer wrongWordCount;
    private Integer studyDuration;
}
