package com.ebbinghaus.vocab.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class ReviewScheduleService {

    private final List<Integer> intervalMinutes;

    public ReviewScheduleService(@Value("${app.review.intervals-minutes}") String intervals) {
        this.intervalMinutes = Arrays.stream(intervals.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();
    }

    public LocalDateTime calculateNextReviewTime(Integer currentStage, Integer familiarity) {
        int nextStage = calculateNextStage(currentStage, familiarity);
        int interval = intervalMinutes.get(Math.min(nextStage, intervalMinutes.size() - 1));
        return LocalDateTime.now().plusMinutes(interval);
    }

    public Integer calculateNextStage(Integer currentStage, Integer familiarity) {
        int stage = currentStage == null ? 0 : currentStage;
        if (familiarity == null || familiarity <= 1) {
            return 0;
        }
        if (familiarity == 2) {
            return Math.max(0, stage - 1);
        }
        return Math.min(stage + 1, intervalMinutes.size() - 1);
    }

    public boolean isMastered(Integer stage, Integer familiarity) {
        return familiarity != null && familiarity >= 3 && stage != null && stage >= intervalMinutes.size() - 1;
    }
}
