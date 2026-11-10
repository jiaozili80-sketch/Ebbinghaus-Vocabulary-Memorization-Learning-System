import request from './request';
import type { ApiResult } from './auth';

export interface ScheduleOverview {
  checkedInToday: boolean;
  continuousCheckinDays: number;
  todayWordCount: number;
  todayStudyDuration: number;
  targetWordCount: number;
  targetStudyMinutes: number;
  wordProgress: number;
  durationProgress: number;
}

export interface DailyGoal {
  id: number;
  userId: number;
  targetWordCount: number;
  targetStudyMinutes: number;
}

export interface CalendarDay {
  date: string;
  checkedIn: boolean;
  newWordCount: number;
  reviewWordCount: number;
  masteredWordCount: number;
  wrongWordCount: number;
  studyDuration: number;
}

export function getScheduleOverview() {
  return request.get<ApiResult<ScheduleOverview>>('/schedule/overview');
}

export function getDailyGoal() {
  return request.get<ApiResult<DailyGoal>>('/schedule/goal');
}

export function updateDailyGoal(data: { targetWordCount: number; targetStudyMinutes: number }) {
  return request.put<ApiResult<DailyGoal>>('/schedule/goal', data);
}

export function checkin(data: { studyDuration?: number; remark?: string }) {
  return request.post<ApiResult<null>>('/schedule/checkin', data);
}

export function getCalendar(params: { year: number; month: number }) {
  return request.get<ApiResult<CalendarDay[]>>('/schedule/calendar', { params });
}
