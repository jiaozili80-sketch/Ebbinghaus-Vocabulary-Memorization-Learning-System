import request from './request';
import type { ApiResult } from './auth';

export interface StudyPlan {
  id: number;
  bookId: number;
  bookName: string;
  category: string;
  wordCount: number;
  planWordCount: number;
  reviewMultiplier: number;
  dailyReviewCount: number;
  dailyTotalCount: number;
  createTime: string;
  updateTime: string;
}

export interface StudyPlanForm {
  bookId: number;
  planWordCount: number;
  reviewMultiplier: number;
}

export function getStudyPlan() {
  return request.get<ApiResult<StudyPlan | null>>('/study-plan');
}

export function saveStudyPlan(data: StudyPlanForm) {
  return request.post<ApiResult<StudyPlan>>('/study-plan', data);
}

export function deleteStudyPlan() {
  return request.delete<ApiResult<null>>('/study-plan');
}
