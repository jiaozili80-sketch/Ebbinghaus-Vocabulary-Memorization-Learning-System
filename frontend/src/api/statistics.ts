import request from './request';
import type { ApiResult } from './auth';

export interface StatisticsOverview {
  learnedWordCount: number;
  masteredWordCount: number;
  reviewPlanCount: number;
  wrongWordCount: number;
  dueReviewCount: number;
  masteryRate: number;
  forgettingRate: number;
  reviewCompletionRate: number;
}

export interface TrendPoint {
  date: string;
  newWordCount: number;
  reviewWordCount: number;
  masteredWordCount: number;
  wrongWordCount: number;
  studyDuration: number;
}

export interface WeakWordAnalysis {
  category: string;
  count: number;
}

export function getStatisticsOverview() {
  return request.get<ApiResult<StatisticsOverview>>('/statistics/overview');
}

export function getStatisticsTrend(days = 7) {
  return request.get<ApiResult<TrendPoint[]>>('/statistics/trend', { params: { days } });
}

export function getWeakAnalysis() {
  return request.get<ApiResult<WeakWordAnalysis[]>>('/statistics/weak-analysis');
}
