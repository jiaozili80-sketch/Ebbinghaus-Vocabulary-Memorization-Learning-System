import request from './request';
import type { ApiResult } from './auth';

export interface StudyOverview {
  newWordCount: number;
  reviewWordCount: number;
  masteredWordCount: number;
  wrongWordCount: number;
}

export interface StudyWord {
  wordId: number;
  planId?: number;
  bookId: number;
  spelling: string;
  phonetic?: string;
  partOfSpeech?: string;
  meaning: string;
  exampleSentence?: string;
  exampleTranslation?: string;
  audioUrl?: string;
  difficulty: number;
  stage: number;
  status: number;
  nextReviewTime?: string;
}

export interface StudySubmitResult {
  wordId: number;
  stage: number;
  status: number;
  mastered: boolean;
  nextReviewTime?: string;
}

export function getStudyOverview(bookId?: number) {
  return request.get<ApiResult<StudyOverview>>('/study/overview', { params: { bookId } });
}

export function getTodayTasks(params: { bookId?: number; limit?: number }) {
  return request.get<ApiResult<StudyWord[]>>('/study/today', { params });
}

export function startNewStudy(params: { bookId: number; limit?: number }) {
  return request.post<ApiResult<StudyWord[]>>('/study/new', null, { params });
}

export function submitStudy(data: { wordId: number; familiarity: number; duration?: number }) {
  return request.post<ApiResult<StudySubmitResult>>('/study/submit', data);
}
