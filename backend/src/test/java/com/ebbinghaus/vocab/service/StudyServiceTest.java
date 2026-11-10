package com.ebbinghaus.vocab.service;

import com.ebbinghaus.vocab.context.LoginUserContext;
import com.ebbinghaus.vocab.domain.dto.StudySubmitRequest;
import com.ebbinghaus.vocab.domain.entity.ReviewPlan;
import com.ebbinghaus.vocab.domain.entity.StudyRecord;
import com.ebbinghaus.vocab.domain.entity.User;
import com.ebbinghaus.vocab.domain.entity.Word;
import com.ebbinghaus.vocab.domain.entity.WordBook;
import com.ebbinghaus.vocab.mapper.ReviewPlanMapper;
import com.ebbinghaus.vocab.mapper.StudyPlanMapper;
import com.ebbinghaus.vocab.mapper.StudyRecordMapper;
import com.ebbinghaus.vocab.mapper.UserMapper;
import com.ebbinghaus.vocab.mapper.WordBookMapper;
import com.ebbinghaus.vocab.mapper.WordMapper;
import com.ebbinghaus.vocab.mapper.WrongWordMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyServiceTest {

    @Mock private WordMapper wordMapper;
    @Mock private ReviewPlanMapper reviewPlanMapper;
    @Mock private WrongWordMapper wrongWordMapper;
    @Mock private StudyRecordMapper studyRecordMapper;
    @Mock private StudyPlanMapper studyPlanMapper;
    @Mock private ReviewScheduleService reviewScheduleService;
    @Mock private UserMapper userMapper;
    @Mock private WordBookMapper wordBookMapper;

    private StudyService studyService;

    @BeforeEach
    void setUp() {
        studyService = new StudyService(
                wordMapper,
                reviewPlanMapper,
                wrongWordMapper,
                studyRecordMapper,
                studyPlanMapper,
                reviewScheduleService,
                userMapper,
                wordBookMapper
        );
        LoginUserContext.setUserId(7L);

        Word word = new Word();
        word.setId(11L);
        word.setBookId(3L);
        word.setStatus(1);
        word.setDeleted(0);
        when(wordMapper.selectById(11L)).thenReturn(word);

        WordBook book = new WordBook();
        book.setId(3L);
        book.setType(1);
        book.setStatus(1);
        book.setDeleted(0);
        when(wordBookMapper.selectById(3L)).thenReturn(book);

        when(reviewScheduleService.calculateNextStage(any(), any())).thenReturn(1);
        when(reviewScheduleService.isMastered(1, 3)).thenReturn(false);
        when(reviewScheduleService.calculateNextReviewTime(any(), any())).thenReturn(LocalDateTime.now().plusMinutes(30));
        when(studyRecordMapper.selectOne(any())).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
        LoginUserContext.clear();
    }

    @Test
    void countsOnlyTheFirstSubmissionAsANewWord() {
        ReviewPlan plan = planWithFirstStudyTime(null);
        when(reviewPlanMapper.selectOne(any())).thenReturn(plan);

        User user = new User();
        user.setId(7L);
        user.setTotalWords(0);
        user.setMasteredWords(0);
        when(userMapper.selectById(7L)).thenReturn(user);

        studyService.submit(request());

        ArgumentCaptor<StudyRecord> recordCaptor = ArgumentCaptor.forClass(StudyRecord.class);
        org.mockito.Mockito.verify(studyRecordMapper).insert(recordCaptor.capture());
        assertEquals(1, recordCaptor.getValue().getNewWordCount());
        assertEquals(0, recordCaptor.getValue().getReviewWordCount());
        assertEquals(1, user.getTotalWords());
    }

    @Test
    void countsLaterSubmissionsAsReviews() {
        ReviewPlan plan = planWithFirstStudyTime(LocalDateTime.now().minusDays(1));
        when(reviewPlanMapper.selectOne(any())).thenReturn(plan);

        studyService.submit(request());

        ArgumentCaptor<StudyRecord> recordCaptor = ArgumentCaptor.forClass(StudyRecord.class);
        org.mockito.Mockito.verify(studyRecordMapper).insert(recordCaptor.capture());
        assertEquals(0, recordCaptor.getValue().getNewWordCount());
        assertEquals(1, recordCaptor.getValue().getReviewWordCount());
    }

    private ReviewPlan planWithFirstStudyTime(LocalDateTime firstStudyTime) {
        ReviewPlan plan = new ReviewPlan();
        plan.setId(5L);
        plan.setUserId(7L);
        plan.setWordId(11L);
        plan.setStage(0);
        plan.setStatus(0);
        plan.setDeleted(0);
        plan.setCreateTime(LocalDateTime.now().minusDays(1));
        plan.setFirstStudyTime(firstStudyTime);
        return plan;
    }

    private StudySubmitRequest request() {
        StudySubmitRequest request = new StudySubmitRequest();
        request.setWordId(11L);
        request.setFamiliarity(3);
        request.setDuration(20);
        return request;
    }
}
